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
import com.spoohapps.jble6lowpand.tasks.Publisher;
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

    public ScanningDaemon(String[] args) {
        initialize(args);
    }

    public ScanningDaemon(KnownDeviceRepository knownDeviceRepository, DeviceService deviceService, DaemonConfig config, List<DeviceListingConsumer> deviceListingConsumers, ControllerBroadcaster controllerService) {
        availableDevices = new CopyOnWriteArraySet<>();
        connectedDevices = new CopyOnWriteArraySet<>();
        this.knownDevices = knownDeviceRepository;
        this.deviceService = deviceService;
        this.config = config;
        this.controllerService = controllerService;
        this.deviceListingConsumers = deviceListingConsumers;
        initialize(new String[0]);
    }

    public static void main(String[] args) {
        ScanningDaemon daemon = new ScanningDaemon(args);
        daemon.start();
    }

    private void initialize(String[] args) {

        if (config == null) {

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
                } catch (Exception e) {
                    logger.error("error reading config file", e);
                }
            }

            configBuilder.apply(args);

            config = configBuilder.build();

        }

        logger.info("Scan Duration: {}", config.getScanDurationMs());
        logger.info("Scan Timeout: {}", config.getScanTimeoutMs());
        logger.info("Connect Timeout: {}", config.getConnectTimeoutMs());
        logger.info("Controller Port: {}", config.getControllerPort());
        logger.info("Allocator Type: {}", config.getAllocatorType());
        logger.info("Whitelist path: {}", config.getWhitelistPath());

        if (scanningExecutorService == null) {

            scanningExecutorService = Executors.newScheduledThreadPool(5);

        }

        if (deviceListingConsumers == null) {

            deviceListingConsumers = new ArrayList<>();

        }

        if (availableDevices == null) {

            availableDevices = new CopyOnWriteArraySet<>();

        }

        if (connectedDevices == null) {

            connectedDevices = new CopyOnWriteArraySet<>();

        }

        if (deviceService == null) {

            DeviceServiceType deviceServiceType = DeviceServiceType.valueOf(config.getAllocatorType());

            if (deviceServiceType == DeviceServiceType.native_ble_ipsp) {
                deviceService = new NativeBle6LowpanIpspService();
            }

        }

        if (knownDevices == null) {

            Path knownDevicesFilePath = Paths.get(config.getWhitelistPath());

            knownDevices = new FileBasedKnownDeviceRepository(knownDevicesFilePath);

        }

        if (controllerService == null) {

            controllerService = new HttpControllerBroadcaster(this, config.getControllerPort());

        }

        try {

            config.getDeviceListingConsumers().stream()
                    .map(DeviceListingConsumerType::valueOf)
                    .forEach(t -> {
                        if (t == DeviceListingConsumerType.redis) {

                            String redisHost = config.getRedisHost();

                            int redisPort = config.getRedisPort();

                            if (redisPort > 0 && redisHost != null && !redisHost.equals("")) {
                                deviceListingConsumers
                                        .add(new RedisDeviceListingConsumer(
                                                scanningExecutorService,
                                                redisHost,
                                                redisPort));

                                logger.info("Redis Device Listing Consumer at {}:{}", redisHost, redisPort);
                            } else {
                                logger.info("Problem loading redis publisher. Check configuration.");
                            }

                        }
                    });

        } catch (Exception e) {
            logger.error("Error reading device listing consumers");
        }

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

                logger.info("Starting Scanner...");

                scanningExecutorService.scheduleWithFixedDelay(
                        new Scanner(deviceService, config.getScanDurationMs(), availableDevices),
                        0,
                        config.getScanTimeoutMs(),
                        TimeUnit.MILLISECONDS);

                logger.info("Starting Connector...");

                scanningExecutorService.scheduleWithFixedDelay(
                        new Connector(deviceService, knownDevices, availableDevices, connectedDevices),
                        config.getScanDurationMs(),
                        config.getConnectTimeoutMs(),
                        TimeUnit.MILLISECONDS);

                if (deviceListingConsumers.size() > 0) {

                    logger.info("Starting Publisher...");

                    scanningExecutorService.scheduleWithFixedDelay(
                            new Publisher(deviceListingConsumers, knownDevices),
                            0,
                            3000,
                            TimeUnit.MILLISECONDS);

                } else {
                    logger.info("No Publisher Consumers Configured. Ignoring.");
                }

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
