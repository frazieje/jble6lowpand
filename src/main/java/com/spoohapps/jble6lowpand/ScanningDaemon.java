package com.spoohapps.jble6lowpand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoohapps.farcommon.Config;
import com.spoohapps.farcommon.cache.CacheConnectionSettings;
import com.spoohapps.farcommon.cache.CacheProvider;
import com.spoohapps.farcommon.cache.RedisCacheProvider;
import com.spoohapps.farcommon.config.ConfigBuilder;
import com.spoohapps.farcommon.manager.Manager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.manager.RedisCacheConnectionManager;
import com.spoohapps.farcommon.model.MACAddress;
import com.spoohapps.jble6lowpand.config.DaemonConfig;
import com.spoohapps.jble6lowpand.config.DefaultConfig;
import com.spoohapps.jble6lowpand.config.ServiceBeaconHandlerType;
import com.spoohapps.jble6lowpand.config.DeviceServiceType;
import com.spoohapps.jble6lowpand.controller.Controller;
import com.spoohapps.jble6lowpand.controller.ControllerBroadcaster;
import com.spoohapps.jble6lowpand.controller.HttpControllerBroadcaster;
import com.spoohapps.jble6lowpand.manager.DeviceServiceManager;
import com.spoohapps.jble6lowpand.manager.ServiceBeaconManager;
import com.spoohapps.jble6lowpand.model.*;
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

    private ScheduledExecutorService scanningExecutorService;

    private ScheduledExecutorService workerExecutorService;

    private Manager<DeviceServiceStatus> deviceServiceManager;

    private Manager<Set<MACAddress>> knownDevicesManager;

    private ControllerBroadcaster controllerService;

    private DaemonConfig config;

    private final Logger logger = LoggerFactory.getLogger(ScanningDaemon.class);

    private List<ServiceBeaconHandler> serviceBeaconHandlers;

    private CacheProvider cacheProvider;

    private List<Manager<?>> workerManagers;

    public ScanningDaemon(String[] args) {
        initialize(args);
    }

    public ScanningDaemon(
            KnownDeviceRepository knownDeviceRepository,
            DeviceService deviceService,
            DaemonConfig config,
            List<ServiceBeaconHandler> serviceBeaconHandlers,
            ControllerBroadcaster controllerService,
            CacheProvider cacheProvider,
            Manager<DeviceServiceStatus> deviceServiceManager,
            Manager<Set<MACAddress>> knownDevicesManager) {
        this.knownDevices = knownDeviceRepository;
        this.deviceService = deviceService;
        this.config = config;
        this.controllerService = controllerService;
        this.serviceBeaconHandlers = serviceBeaconHandlers;
        this.deviceServiceManager = deviceServiceManager;
        this.knownDevicesManager = knownDevicesManager;
        this.cacheProvider = cacheProvider;
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
        logger.info("Service Beacon Address: {}", config.getServiceBeaconMulticastAddress());
        logger.info("Service Beacon Port: {}", config.getServiceBeaconMulticastPort());

        if (workerManagers == null) {
            workerManagers = new ArrayList<>();
        }



        if (workerExecutorService == null) {

            workerExecutorService = Executors.newScheduledThreadPool(4);

        }

        if (serviceBeaconHandlers == null) {

            serviceBeaconHandlers = new ArrayList<>();

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

        if (deviceServiceManager == null && deviceService != null) {

            ManagerSettings deviceServiceManagerSettings = new ManagerSettings() {
                @Override
                public long startDelay() {
                    return 0;
                }

                @Override
                public TimeUnit startDelayTimeUnit() {
                    return TimeUnit.SECONDS;
                }

                @Override
                public long timeout() {
                    return config.getScanTimeoutMs();
                }

                @Override
                public TimeUnit timeoutTimeUnit() {
                    return TimeUnit.MILLISECONDS;
                }
            };

            if (scanningExecutorService == null) {

                scanningExecutorService = Executors.newSingleThreadScheduledExecutor();

            }

            deviceServiceManager =
                    new DeviceServiceManager(
                            scanningExecutorService,
                            deviceServiceManagerSettings,
                            deviceService,
                            knownDevices,
                            config.getScanDurationMs(),
                            config.getConnectTimeoutMs(),
                            config.getDisconnectTimeoutMs());

        }

        if (knownDevicesManager == null) {

            ManagerSettings knownDevicesSettings = new ManagerSettings() {
                @Override
                public long startDelay() {
                    return 10;
                }

                @Override
                public TimeUnit startDelayTimeUnit() {
                    return TimeUnit.SECONDS;
                }

                @Override
                public long timeout() {
                    return config.getPublishTimeoutMs();
                }

                @Override
                public TimeUnit timeoutTimeUnit() {
                    return TimeUnit.MILLISECONDS;
                }
            };

            knownDevicesManager =
                    new ServiceBeaconManager(
                            workerExecutorService,
                            knownDevicesSettings,
                            serviceBeaconHandlers,
                            knownDevices);

        }

        try {

            config.getServiceBeaconHandlers().stream()
                    .map(ServiceBeaconHandlerType::valueOf)
                    .forEach(t -> {
                        if (t == ServiceBeaconHandlerType.redis) {

                            if (cacheProvider == null) {

                                RedisCacheConnectionManager redisConnectionManager = new RedisCacheConnectionManager(
                                        workerExecutorService,
                                        new CacheConnectionSettings() {
                                            @Override
                                            public String host() {
                                                return config.getRedisHost();
                                            }

                                            @Override
                                            public int port() {
                                                return config.getRedisPort();
                                            }
                                        },
                                        new ManagerSettings() {
                                            @Override
                                            public long startDelay() {
                                                return 0;
                                            }

                                            @Override
                                            public TimeUnit startDelayTimeUnit() {
                                                return TimeUnit.SECONDS;
                                            }

                                            @Override
                                            public long timeout() {
                                                return 10;
                                            }

                                            @Override
                                            public TimeUnit timeoutTimeUnit() {
                                                return TimeUnit.SECONDS;
                                            }
                                        });

                                cacheProvider = new RedisCacheProvider(redisConnectionManager, new ObjectMapper(), null);

                                workerManagers.add(redisConnectionManager);

                            }

                            if (cacheProvider instanceof RedisCacheProvider) {

                                ServiceBeacon serviceBeacon =
                                        new MulticastServiceBeacon(
                                                config.getServiceBeaconMulticastAddress(),
                                                config.getServiceBeaconMulticastPort());

                                serviceBeaconHandlers
                                        .add(new CachedServiceBeaconHandler(cacheProvider, serviceBeacon));

                                logger.info("Redis SearviceBeaconHandler loaded");
                            } else {
                                logger.info("Cache provider is not a redis cache provider. Check configuration.");
                            }

                        }
                    });

        } catch (Exception e) {
            logger.error("Error reading device listing consumers");
        }

    }

	public void stop() {
        logger.info("Stopping...");

        if (scanningExecutorService != null) {
            stopExecutor(scanningExecutorService, "scanning");
        }

        if (workerExecutorService != null) {
            stopExecutor(workerExecutorService, "worker");
        }

        if (deviceServiceManager != null) {
            deviceServiceManager.stop();
        }

        if (knownDevicesManager != null) {
            knownDevicesManager.stop();
        }

        knownDevices.stopWatcher();

        workerManagers.forEach(Manager::stop);

        controllerService.stop();

        logger.info("Stopped");
	}

    private void stopExecutor(ScheduledExecutorService executorService, String name) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(config.getScanDurationMs()*2, TimeUnit.MILLISECONDS)) {
                logger.debug("Force stopping {} executor...", name);
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS))
                    logger.debug("{} executor did not terminate, kill process manually.", name);
            } else {
                logger.info("Stopped {} executor normally.", name);
            }
        } catch (InterruptedException ignored) {}
    }

    public void start() {
        try {
            logger.info("Starting...");
            logger.info("Starting Controller Service...");
            controllerService.start();

            logger.info("Starting Known Devices Manager...");
            knownDevices.startWatcher();

            if (deviceService != null) {

                deviceService.initializeDevice();

                logger.info("Starting Device Allocator...");

                deviceServiceManager.start();

            } else {
                logger.info("No Device Allocator configured.");
            }

            if (serviceBeaconHandlers.size() > 0) {

                logger.info("Starting Publisher...");

                workerManagers.forEach(Manager::start);

                knownDevicesManager.start();

            } else {
                logger.info("No Publisher Consumers Configured.");
            }

            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

            logger.info("Running.");

        } catch (Exception e) {
            logger.error(e.getMessage());
            for (StackTraceElement se : e.getStackTrace())
                logger.error(se.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<MACAddress> getAvailableDevices() {
        if (deviceServiceManager != null) {
            return deviceServiceManager.getResource().getAvailableDevices();
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public Set<MACAddress> getKnownDevices() {
        return knownDevicesManager.getResource();
    }

    @Override
    public Set<MACAddress> getConnectedDevices() {
        if (deviceServiceManager != null) {
            return deviceServiceManager.getResource().getConnectedDevices();
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public boolean addKnownDevice(MACAddress address) {
        try {
            return knownDevices.add(address);
        } catch (IllegalArgumentException iae) {
            logger.error(iae.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeKnownDevice(MACAddress address) {
        try {
            return knownDevices.remove(address);
        } catch (IllegalArgumentException iae) {
            logger.error(iae.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateKnownDevice(MACAddress address) {
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
