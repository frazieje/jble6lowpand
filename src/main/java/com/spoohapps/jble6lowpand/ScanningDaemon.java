package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.Config;
import com.spoohapps.farcommon.config.ConfigBuilder;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.config.DaemonConfig;
import com.spoohapps.jble6lowpand.config.DefaultConfig;
import com.spoohapps.jble6lowpand.config.DeviceListingConsumerType;
import com.spoohapps.jble6lowpand.config.DeviceServiceType;
import com.spoohapps.jble6lowpand.controller.Controller;
import com.spoohapps.jble6lowpand.controller.ControllerBroadcaster;
import com.spoohapps.jble6lowpand.controller.HttpControllerBroadcaster;
import com.spoohapps.jble6lowpand.model.DeviceListingConsumer;
import com.spoohapps.jble6lowpand.model.FileBasedKnownDeviceRepository;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import com.spoohapps.jble6lowpand.model.RedisDeviceListingConsumer;
import com.spoohapps.jble6lowpand.tasks.Connector;
import com.spoohapps.jble6lowpand.tasks.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class ScanningDaemon implements Controller {
	
	private KnownDeviceRepository knownDevices;
	private DeviceService deviceService;

    private CopyOnWriteArraySet<EUI48Address> connectedDevices;

    private CopyOnWriteArraySet<EUI48Address> availableDevices;

    private ScheduledExecutorService scanningExecutorService;

    private ControllerBroadcaster controllerService;

    private DaemonConfig config;

    private final Logger logger = LoggerFactory.getLogger(ScanningDaemon.class);
    private List<DeviceListingConsumer> deviceListingConsumers;

    public ScanningDaemon() {}

    public ScanningDaemon(String[] args) {
        initialize(args);
    }

    public ScanningDaemon(KnownDeviceRepository knownDeviceRepository, DeviceService deviceService, DaemonConfig config, List<DeviceListingConsumer> deviceListingConsumers, ControllerBroadcaster controllerService) {
        this.scanningExecutorService = Executors.newScheduledThreadPool(3);
        availableDevices = new CopyOnWriteArraySet<>();
        connectedDevices = new CopyOnWriteArraySet<>();
        this.knownDevices = knownDeviceRepository;
        this.deviceService = deviceService;
        this.config = config;
        this.controllerService = controllerService;
        this.deviceListingConsumers = deviceListingConsumers;
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

        logger.info("Scan Duration: {}", config.getScanDurationMs());
        logger.info("Scan Timeout: {}", config.getScanTimeoutMs());
        logger.info("Connect Timeout: {}", config.getConnectTimeoutMs());
        logger.info("Controller Port: {}", config.getControllerPort());
        logger.info("Allocator Type: {}", config.getAllocatorType());
        logger.info("Whitelist path: {}", config.getWhitelistPath());

        scanningExecutorService = Executors.newScheduledThreadPool(5);

        deviceListingConsumers = new ArrayList<>();

        String redisHost = config.getRedisHost();

        int redisPort = config.getRedisPort();

        try {

            config.getDeviceListingConsumers().stream()
                    .map(DeviceListingConsumerType::valueOf)
                    .forEach(t -> {
                        if (t == DeviceListingConsumerType.redis) {
                            deviceListingConsumers
                                    .add(new RedisDeviceListingConsumer(
                                            scanningExecutorService,
                                            redisHost,
                                            redisPort));
                            logger.info("Redis Device Listing Consumer at {}:{}", redisHost, redisPort);
                        }
                    });

        } catch (Exception e) {
            logger.error("Error reading device listing consumers");
        }

        Path knownDevicesFilePath = Paths.get(config.getWhitelistPath());

        availableDevices = new CopyOnWriteArraySet<>();

        connectedDevices = new CopyOnWriteArraySet<>();

        DeviceServiceType deviceServiceType = DeviceServiceType.valueOf(config.getAllocatorType());

        if (deviceServiceType == DeviceServiceType.native_ble_ipsp) {
            deviceService = new NativeBle6LowpanIpspService();
        }

        knownDevices = new FileBasedKnownDeviceRepository(knownDevicesFilePath, deviceListingConsumers);

        controllerService = new HttpControllerBroadcaster(this, config.getControllerPort());
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

        deviceListingConsumers.forEach(DeviceListingConsumer::stop);

        controllerService.stop();

        logger.info("Stopped");
	}

	public void start() {
        try {
            logger.info("Starting...");
            controllerService.start();

            deviceListingConsumers.forEach(DeviceListingConsumer::start);

            knownDevices.startWatcher();

            if (deviceService != null) {

                deviceService.initializeDevice();

                scanningExecutorService.scheduleWithFixedDelay(
                        new Scanner(deviceService, config.getScanDurationMs(), availableDevices),
                        0,
                        config.getScanTimeoutMs(),
                        TimeUnit.MILLISECONDS);

                scanningExecutorService.scheduleWithFixedDelay(
                        new Connector(deviceService, knownDevices, availableDevices, connectedDevices),
                        config.getScanDurationMs(),
                        config.getConnectTimeoutMs(),
                        TimeUnit.MILLISECONDS);

                scanningExecutorService.scheduleWithFixedDelay(
                        () -> deviceListingConsumers.forEach(c -> c.accept(knownDevices.getAll())),
                        0,
                        5000,
                        TimeUnit.MILLISECONDS);

            }

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
