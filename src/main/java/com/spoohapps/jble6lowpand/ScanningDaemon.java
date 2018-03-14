package com.spoohapps.jble6lowpand;

import com.spoohapps.jble6lowpand.config.Config;
import com.spoohapps.jble6lowpand.config.DaemonConfig;
import com.spoohapps.jble6lowpand.controller.Ble6LowpanController;
import com.spoohapps.jble6lowpand.controller.Ble6LowpanControllerService;
import com.spoohapps.jble6lowpand.controller.RemoteBle6LowpanControllerService;
import com.spoohapps.jble6lowpand.model.BTAddress;
import com.spoohapps.jble6lowpand.model.FileBasedKnownDeviceRepository;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import com.spoohapps.jble6lowpand.tasks.BleIpspConnector;
import com.spoohapps.jble6lowpand.tasks.BleIpspScanner;
import org.apache.commons.daemon.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ScanningDaemon implements Daemon, Ble6LowpanController {
	
	private KnownDeviceRepository knownDevices;
	private Ble6LowpanIpspService ble6LowpanIpspService;

    private CopyOnWriteArraySet<BTAddress> connectedDevices;

    private CopyOnWriteArraySet<BTAddress> availableDevices;

    private ScheduledExecutorService scanningExecutorService;

    private Ble6LowpanControllerService controllerService;

    private DaemonConfig config;

    private final Logger logger = LoggerFactory.getLogger(ScanningDaemon.class);

    public ScanningDaemon() {}

    public ScanningDaemon(String[] args) {
        initialize(args);
    }

    public ScanningDaemon(KnownDeviceRepository knownDeviceRepository, Ble6LowpanIpspService ble6LowpanIpspService, DaemonConfig config, Ble6LowpanControllerService controllerService) {
        this.scanningExecutorService = Executors.newScheduledThreadPool(3);
        availableDevices = new CopyOnWriteArraySet<>();
        connectedDevices = new CopyOnWriteArraySet<>();
        this.knownDevices = knownDeviceRepository;
        this.ble6LowpanIpspService = ble6LowpanIpspService;
        this.config = config;
        this.controllerService = controllerService;
    }

    public static void main(String[] args) {
        ScanningDaemon daemon = new ScanningDaemon(args);
        daemon.start();
    }

    private void initialize(String[] args) {
        InputStream configFileStream = null;

        try {
            Path path = Paths.get(new URI(System.getProperty("user.home") + "/jble6lowpand.conf"));
            configFileStream = Files.newInputStream(path);
        } catch (Exception e) {}

        config = Config.fromDefaults()
                .apply(Config.fromStream(configFileStream))
                .apply(Config.fromArgs(args));

        logger.info("Whitelist path: {}", config.getWhitelistPath());
        logger.info("Scan Duration: {}", config.getScanDurationMs());
        logger.info("Scan Timeout: {}", config.getScanTimeoutMs());
        logger.info("Connect Timeout: {}", config.getConnectTimeoutMs());
        logger.info("RMI Port: {}", config.getControllerPort());

        Path knownDevicesFilePath = Paths.get(config.getWhitelistPath());

        scanningExecutorService = Executors.newScheduledThreadPool(3);
        availableDevices = new CopyOnWriteArraySet<>();
        connectedDevices = new CopyOnWriteArraySet<>();
        knownDevices = new FileBasedKnownDeviceRepository(knownDevicesFilePath);
        ble6LowpanIpspService = new NativeBle6LowpanIpspService();
        controllerService = new RemoteBle6LowpanControllerService(this, config.getControllerPort());
    }

	@Override
	public void init(DaemonContext context) throws DaemonInitException {
        logger.info("Initializing...");
        initialize(context.getArguments());
	}

	@Override
	public void stop() throws InterruptedException {
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

	@Override
	public void destroy() {
	}

	@Override
	public void start() {
        try {
            logger.info("Starting...");
            controllerService.start();
            knownDevices.startWatcher();
            scanningExecutorService.scheduleWithFixedDelay(
                    new BleIpspScanner(ble6LowpanIpspService, config.getScanDurationMs(), availableDevices),
                    0,
                    config.getScanTimeoutMs(),
                    TimeUnit.MILLISECONDS);
            scanningExecutorService.scheduleWithFixedDelay(
                    new BleIpspConnector(ble6LowpanIpspService, knownDevices, availableDevices, connectedDevices),
                    config.getScanDurationMs(),
                    config.getConnectTimeoutMs(),
                    TimeUnit.MILLISECONDS);
            logger.info("Running...");
        } catch (Exception e) {
            logger.error(e.getMessage());
            for (StackTraceElement se : e.getStackTrace())
                logger.error(se.toString());
            throw e;
        }
    }

    @Override
    public Set<BTAddress> getAvailableDevices() {
        return availableDevices;
    }

    @Override
    public Set<BTAddress> getKnownDevices() {
        return knownDevices.getAll();
    }

    @Override
    public Set<BTAddress> getConnectedDevices() {
        return connectedDevices;
    }

    @Override
    public void addKnownDevice(BTAddress address) {
        try {
            knownDevices.add(address);
        } catch (IllegalArgumentException iae) {
            logger.error("Can not add device. Invalid bluetooth address.");
        }
    }

    @Override
    public void removeKnownDevice(BTAddress address) {
        try {
            knownDevices.remove(address);
        } catch (IllegalArgumentException iae) {
            logger.error("Can not remove device. Invalid bluetooth address.");
        }
    }
	
}
