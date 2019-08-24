package com.spoohapps.jble6lowpand.manager;

import com.spoohapps.farcommon.manager.AbstractManager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

public class AvailableDevicesManager extends AbstractManager<Set<EUI48Address>> {

    private static final Logger logger = LoggerFactory.getLogger(AvailableDevicesManager.class);

    private final CopyOnWriteArraySet<EUI48Address> availableDevices;
    private final DeviceService deviceService;
    private final int scanDurationSeconds;

    public AvailableDevicesManager(ScheduledExecutorService executorService, ManagerSettings managerSettings, DeviceService deviceService, int scanDurationSeconds) {
        super(executorService, managerSettings);
        availableDevices = new CopyOnWriteArraySet<>();
        this.deviceService = deviceService;
        this.scanDurationSeconds = scanDurationSeconds;
    }

    @Override
    protected void doProcess() {
        logger.trace("Scanning for devices");
        try {
            EUI48Address[] devices = deviceService.scanDevices(scanDurationSeconds);
            Set<EUI48Address> availableAddresses = new HashSet<>();
            logger.trace("{} devices found", devices.length);
            for (EUI48Address device : devices) {
                try {
                    logger.trace("adding device {}", device);
                    availableAddresses.add(device);
                } catch (IllegalArgumentException iae) {
                    logger.trace("Error deserializing EUI48 address");
                }
            }
            availableDevices.retainAll(availableAddresses);
            availableDevices.addAll(availableAddresses);
        } catch (Exception e) {
            logger.trace("Error in scanner", e);
        }
        logger.trace("Done scanning");
    }

    @Override
    protected Set<EUI48Address> doGetResource() {
        return availableDevices;
    }
}
