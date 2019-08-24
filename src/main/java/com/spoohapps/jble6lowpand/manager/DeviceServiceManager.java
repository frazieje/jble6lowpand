package com.spoohapps.jble6lowpand.manager;

import com.spoohapps.farcommon.manager.AbstractManager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.DeviceService;
import com.spoohapps.jble6lowpand.model.DeviceServiceStatus;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

public class DeviceServiceManager extends AbstractManager<DeviceServiceStatus> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceManager.class);

    private final DeviceService deviceService;

    private final KnownDeviceRepository knownDevices;

    private final CopyOnWriteArraySet<EUI48Address> availableDevices;

    private final CopyOnWriteArraySet<EUI48Address> connectedDevices;

    private final int scanDurationSeconds;

    private final long connectTimeoutMs;

    private final long disconnectTimeoutMs;

    public DeviceServiceManager(
            ScheduledExecutorService executorService,
            ManagerSettings managerSettings,
            DeviceService deviceService,
            KnownDeviceRepository knownDevices,
            int scanDurationSeconds,
            long connectTimeoutMs,
            long disconnectTimeoutMs) {

        super(executorService, managerSettings);

        this.deviceService = deviceService;

        this.knownDevices = knownDevices;

        this.availableDevices = new CopyOnWriteArraySet<>();

        this.connectedDevices = new CopyOnWriteArraySet<>();

        this.scanDurationSeconds = scanDurationSeconds;

        this.connectTimeoutMs = connectTimeoutMs;

        this.disconnectTimeoutMs = disconnectTimeoutMs;

    }

    @Override
    protected void doProcess() {

        logger.trace("Starting process");

        logger.trace("Scanning for available devices");
        try {
            EUI48Address[] devices = deviceService.scanDevices(scanDurationSeconds);
            Set<EUI48Address> availableAddresses = new HashSet<>();
            logger.trace("{} available devices found", devices.length);
            for (EUI48Address device : devices) {
                try {
                    logger.trace("adding available device {}", device);
                    availableAddresses.add(device);
                } catch (IllegalArgumentException iae) {
                    logger.trace("Error deserializing EUI48 address");
                }
            }
            availableDevices.retainAll(availableAddresses);
            availableDevices.addAll(availableAddresses);
        } catch (Exception e) {
            logger.error("Error in scan process", e);
        }
        logger.trace("Done scanning");

        logger.trace("Connecting to known devices...");
        try {
            for (EUI48Address address : availableDevices) {
                if (knownDevices.contains(address) && !connectedDevices.contains(address)) {
                    logger.info("Connecting to {} ... ", address.toString());
                    if (deviceService.connectDevice(address)) {
                        logger.info("Connected to {}", address.toString());
                        Thread.sleep(connectTimeoutMs);
                    } else {
                        logger.error("Could not connect to {}", address.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error connecting to known devices", e);
        }
        logger.trace("Done connecting to known devices");

        logger.trace("Updating connected devices");
        try {
            Set<EUI48Address> connectedAddresses = new HashSet<>();
            for (EUI48Address address : deviceService.getConnectedDevices()) {
                connectedAddresses.add(address);
                if (!knownDevices.contains(address)) {
                    if (deviceService.disconnectDevice(address)) {
                        logger.info("Disconnecting from {}", address.toString());
                        Thread.sleep(disconnectTimeoutMs);
                    } else {
                        logger.error("Could not disconnect from {}", address.toString());
                    }
                }
            }
            connectedDevices.retainAll(connectedAddresses);
            connectedDevices.addAll(connectedAddresses);
        } catch (Exception e) {
            logger.error("Error updating connected devices", e);
        }
        logger.trace("Done updating connected devices");

        logger.trace("Process complete");

    }

    @Override
    protected DeviceServiceStatus doGetResource() {
        return new DeviceServiceStatus(availableDevices, connectedDevices);
    }
}
