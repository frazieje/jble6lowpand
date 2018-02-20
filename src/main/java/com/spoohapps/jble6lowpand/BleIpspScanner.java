package com.spoohapps.jble6lowpand;

import com.spoohapps.jble6lowpand.model.BTAddress;
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
        logger.info("Scanning for ipsp devices");
        try {
            String[] devices = ble6LowpanIpspService.scanIpspDevices(scanDurationSeconds);
            Set<BTAddress> availableAddresses = new HashSet<>();
            logger.info("{} devices found", devices.length);
            for (int i = 0; i < devices.length; i++) {
                try {
                    logger.info("adding device {}", devices[i]);
                    availableAddresses.add(new BTAddress(devices[i]));
                } catch (IllegalArgumentException iae) {
                    logger.info("Error deserializing BT address");
                }
            }
            availableDevices.retainAll(availableAddresses);
            availableDevices.addAll(availableAddresses);
        } catch (Exception e) {
            logger.info("Error in scanner");
        }
        logger.info("Done scanning");
    }
}
