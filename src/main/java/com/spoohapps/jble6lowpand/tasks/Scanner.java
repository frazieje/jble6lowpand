package com.spoohapps.jble6lowpand.tasks;

import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Scanner implements Runnable {

    private final CopyOnWriteArraySet<EUI48Address> availableDevices;
    private final DeviceService deviceService;
    private final int scanDurationSeconds;

    private final Logger logger = LoggerFactory.getLogger(Scanner.class);

    public Scanner(DeviceService deviceService, int scanDurationSeconds, CopyOnWriteArraySet<EUI48Address> availableDevices) {
        this.deviceService = deviceService;
        this.availableDevices = availableDevices;
        this.scanDurationSeconds = scanDurationSeconds;
    }

    @Override
    public void run() {
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
}
