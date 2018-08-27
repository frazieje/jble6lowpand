package com.spoohapps.jble6lowpand.tasks;

import com.spoohapps.jble6lowpand.Ble6LowpanIpspService;
import com.spoohapps.farcommon.model.BTAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BleIpspScanner implements Runnable {

    private final CopyOnWriteArraySet<BTAddress> availableDevices;
    private final Ble6LowpanIpspService ble6LowpanIpspService;
    private final int scanDurationSeconds;

    private final Logger logger = LoggerFactory.getLogger(BleIpspScanner.class);

    public BleIpspScanner(Ble6LowpanIpspService ipspService, int scanDurationSeconds, CopyOnWriteArraySet<BTAddress> availableDevices) {
        this.ble6LowpanIpspService = ipspService;
        this.availableDevices = availableDevices;
        this.scanDurationSeconds = scanDurationSeconds;
    }

    @Override
    public void run() {
        logger.trace("Scanning for ipsp devices");
        try {
            BTAddress[] devices = ble6LowpanIpspService.scanIpspDevices(scanDurationSeconds);
            Set<BTAddress> availableAddresses = new HashSet<>();
            logger.trace("{} devices found", devices.length);
            for (int i = 0; i < devices.length; i++) {
                try {
                    logger.trace("adding device {}", devices[i]);
                    availableAddresses.add(devices[i]);
                } catch (IllegalArgumentException iae) {
                    logger.trace("Error deserializing BT address");
                }
            }
            availableDevices.retainAll(availableAddresses);
            availableDevices.addAll(availableAddresses);
        } catch (Exception e) {
            logger.trace("Error in scanner");
        }
        logger.trace("Done scanning");
    }
}
