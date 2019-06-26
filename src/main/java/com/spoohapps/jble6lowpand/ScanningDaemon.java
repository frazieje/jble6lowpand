package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.Config;
import com.spoohapps.farcommon.config.ConfigBuilder;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.config.DaemonConfig;
import com.spoohapps.jble6lowpand.config.DefaultConfig;
import com.spoohapps.jble6lowpand.controller.Ble6LowpanController;
import com.spoohapps.jble6lowpand.controller.Ble6LowpanControllerBroadcaster;
import com.spoohapps.jble6lowpand.controller.RemoteBle6LowpanControllerBroadcaster;
import com.spoohapps.jble6lowpand.model.FileBasedKnownDeviceRepository;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import com.spoohapps.jble6lowpand.tasks.BleIpspConnector;
import com.spoohapps.jble6lowpand.tasks.BleIpspScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class ScanningDaemon implements Ble6LowpanController {
	
	private KnownDeviceRepository knownDevices;
	private DeviceService deviceService;

    private CopyOnWriteArraySet<EUI48Address> connectedDevices;

    private CopyOnWriteArraySet<EUI48Address> availableDevices;

    private ScheduledExecutorService scanningExecutorService;

    private Ble6LowpanControllerBroadcaster controllerService;

    private DaemonConfig config;

    private final Logger logger = LoggerFactory.getLogger(ScanningDaemon.class);

    public ScanningDaemon() {}

    public ScanningDaemon(String[] args) {
        initialize(args);
    }

    public ScanningDaemon(KnownDeviceRepository knownDeviceRepository, DeviceService deviceService, DaemonConfig config, Ble6LowpanControllerBroadcaster controllerService) {
        this.scanningExecutorService = Executors.newScheduledThreadPool(3);
        availableDevices = new CopyOnWriteArraySet<>();
        connectedDevices = new CopyOnWriteArraySet<>();
        this.knownDevices = knownDeviceRepository;
        this.deviceService = deviceService;
        this.config = config;
        this.controllerService = controllerService;
    }

    public static void main(String[] args) {
        ScanningDaemon daemon = new ScanningDaemon(args);
        daemon.start();
    }

    private void initialize(String[] args) {

        String configFilePath = null;
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-configFile")) {
                    configFilePath = args[i + 1];
                }
            }
        } catch (Exception ignored) {
        }

        ConfigBuilder<DaemonConfig> configBuilder = Config.from(DaemonConfig.class);

        configBuilder.apply(new DefaultConfig());

        if (configFilePath != null) {
            try (InputStream fileStream = Files.newInputStream(Paths.get(configFilePath))) {
                configBuilder.apply(fileStream);
            } catch(Exception e){
                logger.error("error reading config file", e);
            }
        }

        configBuilder.apply(args);

        config = configBuilder.build();

        logger.info("Whitelist path: {}", config.getWhitelistPath());
        logger.info("Scan Duration: {}", config.getScanDurationMs());
        logger.info("Scan Timeout: {}", config.getScanTimeoutMs());
        logger.info("Connect Timeout: {}", config.getConnectTimeoutMs());
        logger.info("Controller Port: {}", config.getControllerPort());

        Path knownDevicesFilePath = Paths.get(config.getWhitelistPath());

        scanningExecutorService = Executors.newScheduledThreadPool(3);
        availableDevices = new CopyOnWriteArraySet<>();
        connectedDevices = new CopyOnWriteArraySet<>();
        knownDevices = new FileBasedKnownDeviceRepository(knownDevicesFilePath);
        deviceService = new NativeBle6LowpanIpspService();
        controllerService = new RemoteBle6LowpanControllerBroadcaster(this, config.getControllerPort());
    }

	public void stop() {
        logger.info("Stopping...");
        scanningExecutorService.shutdown();
        try {
            if (!scanningExecutorService.awaitTermination(config.getScanDurationMs()*2, TimeUnit.MILLISECONDS)) {
                logger.info("Force stopping...");
                scanningExecutorService.shutdownNow();
                if (!scanningExecutorService.awaitTermination(5, TimeUnit.SECONDS))
                    logger.error("Did not terminate, kill process manually.");
            } else {
                logger.info("Exiting normally...");
            }
        } catch (InterruptedException ie) {}

        knownDevices.stopWatcher();

        controllerService.stop();

        logger.info("Stopped");
	}

	public void start() {
        try {
            logger.info("Starting...");
            controllerService.start();

            knownDevices.startWatcher();

            deviceService.initializeDevice();

            scanningExecutorService.scheduleWithFixedDelay(
                    new BleIpspScanner(deviceService, config.getScanDurationMs(), availableDevices),
                    0,
                    config.getScanTimeoutMs(),
                    TimeUnit.MILLISECONDS);
            scanningExecutorService.scheduleWithFixedDelay(
                    new BleIpspConnector(deviceService, knownDevices, availableDevices, connectedDevices),
                    config.getScanDurationMs(),
                    config.getConnectTimeoutMs(),
                    TimeUnit.MILLISECONDS);

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            logger.info("Running...");
        } catch (Exception e) {
            logger.error(e.getMessage());
            for (StackTraceElement se : e.getStackTrace())
                logger.error(se.toString());
            throw e;
        }
    }

    @Override
    public Set<EUI48Address> getAvailableDevices() {
        return availableDevices;
    }

    @Override
    public Set<EUI48Address> getKnownDevices() {
        return knownDevices.getAll();
    }

    @Override
    public Set<EUI48Address> getConnectedDevices() {
        return connectedDevices;
    }

    @Override
    public boolean addKnownDevice(EUI48Address address) {
        try {
            return knownDevices.add(address);
        } catch (IllegalArgumentException iae) {
            logger.error(iae.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeKnownDevice(EUI48Address address) {
        try {
            return knownDevices.remove(address);
        } catch (IllegalArgumentException iae) {
            logger.error(iae.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateKnownDevice(EUI48Address address) {
        try {
            return knownDevices.update(address);
        } catch (IllegalArgumentException iae) {
            logger.error(iae.getMessage());
            return false;
        }
    }

    @Override
    public DaemonConfig getConfig() {
        return config;
    }

}
