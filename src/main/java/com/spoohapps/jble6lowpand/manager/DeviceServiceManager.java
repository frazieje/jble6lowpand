package com.spoohapps.jble6lowpand.manager;

import com.spoohapps.farcommon.manager.AbstractManager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.model.MACAddress;
import com.spoohapps.jble6lowpand.DeviceService;
import com.spoohapps.jble6lowpand.model.DeviceServiceStatus;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

public class DeviceServiceManager extends AbstractManager<DeviceServiceStatus> {

    private static final Logger logger = LoggerFactory.getLogger(DeviceServiceManager.class);

    private final DeviceService deviceService;

    private final KnownDeviceRepository knownDevices;

    private final CopyOnWriteArraySet<MACAddress> availableDevices;

    private final CopyOnWriteArraySet<MACAddress> connectedDevices;

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

        Set<MACAddress> connectedAddresses = updateConnectedDevices();

        disconnectFromUnknownDevices(connectedAddresses);

        Set<MACAddress> availableAddresses = scanForDevices();

        connectToKnownDevices(availableAddresses, connectedAddresses);

        updateConnectedDevices();

        logger.trace("Process complete");

    }

    private void connectToKnownDevices(Set<MACAddress> availableAddresses, Set<MACAddress> connectedAddresses) {
        logger.trace("Connecting to known devices...");
        try {
            for (MACAddress address : availableAddresses) {
                if (knownDevices.contains(address) && !connectedAddresses.contains(address)) {
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
    }

    private void disconnectFromUnknownDevices(Set<MACAddress> connectedAddresses) {
        logger.trace("Disconnecting from unknown devices...");
        try {
            for (MACAddress address : connectedAddresses) {
                if (!knownDevices.contains(address)) {
                    logger.info("Disconnected from {} ... ", address.toString());
                    if (deviceService.disconnectDevice(address)) {
                        logger.info("Disconnected from {}", address.toString());
                        Thread.sleep(disconnectTimeoutMs);
                    } else {
                        logger.error("Could not disconnect from {}", address.toString());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error disconnecting from known devices", e);
        }
        logger.trace("Done disconnecting from known devices");
    }

    private Set<MACAddress> scanForDevices() {
        logger.trace("Scanning for available devices");
        Set<MACAddress> availableAddresses = new HashSet<>();
        try {
            MACAddress[] devices = deviceService.scanDevices(scanDurationSeconds);
            logger.trace("{} available devices found", devices.length);
            for (MACAddress device : devices) {
                logger.trace("adding available device {}", device);
                availableAddresses.add(device);
            }
            availableDevices.retainAll(availableAddresses);
            availableDevices.addAll(availableAddresses);
        } catch (Exception e) {
            logger.error("Error in scan process", e);
        }
        logger.trace("Done scanning");
        return availableAddresses;
    }

    private Set<MACAddress> updateConnectedDevices() {

        Set<MACAddress> connectedAddresses = new HashSet<>();

        logger.trace("Updating connected devices");
        try {

            connectedAddresses.addAll(Arrays.asList(deviceService.getConnectedDevices()));

            connectedDevices.retainAll(connectedAddresses);
            connectedDevices.addAll(connectedAddresses);

        } catch (Exception e) {
            logger.error("Error updating connected devices", e);
        }
        logger.trace("Done updating connected devices");

        return connectedAddresses;
    }

    @Override
    protected DeviceServiceStatus doGetResource() {
        return new DeviceServiceStatus(availableDevices, connectedDevices);
    }
}
