package com.spoohapps.jble6lowpand;

import com.spoohapps.jble6lowpand.model.BTAddress;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BleIpspConnector implements Runnable {

    private final CopyOnWriteArraySet<BTAddress> connectedDevices;
    private final CopyOnWriteArraySet<BTAddress> availableDevices;
    private final Ble6LowpanIpspService ble6LowpanIpspService;
    private final KnownDeviceRepository knownDevices;

    private final Logger logger = LoggerFactory.getLogger(BleIpspConnector.class);

    public BleIpspConnector(Ble6LowpanIpspService ipspService, KnownDeviceRepository knownDevices, CopyOnWriteArraySet<BTAddress> availableDevices, CopyOnWriteArraySet<BTAddress> connectedDevices) {
        this.ble6LowpanIpspService = ipspService;
        this.connectedDevices = connectedDevices;
        this.availableDevices = availableDevices;
        this.knownDevices = knownDevices;
    }

    @Override
    public void run() {
        logger.info("Connecting to ipsp devices");
        for (BTAddress address : availableDevices) {
            if (knownDevices.contains(address)) {
                if (!connectedDevices.contains(address)) {
                    String message = "connecting to " + address.toString() + " ...";
                    try {
                        if (ble6LowpanIpspService.connectIpspDevice(address.toString())) {
                            logger.info("{} success", message);
                        } else {
                            logger.info("{} failed", message);
                        }
                    } catch (Exception e) {
                        logger.info("{} failed", message);
                    }
                } else {
                    logger.info("ignoring already connected device {}", address.toString());
                }
            } else {
                logger.info("ignoring unknown device {}", address.toString());
            }
        }
        logger.info("updating connected ipsp devices");
        try {
            String[] devices = ble6LowpanIpspService.getConnectedIpspDevices();
            Set<BTAddress> connectedAddresses = new HashSet<>();
            for (int i = 0; i < devices.length; i++) {
                try {
                    logger.info("Connected Device: {}", devices[i]);
                    connectedAddresses.add(new BTAddress(devices[i]));
                } catch (IllegalArgumentException iae) {
                    logger.info("Error deserializing BT address");
                }
            }
            connectedDevices.retainAll(connectedAddresses);
            connectedDevices.addAll(connectedAddresses);

        } catch (IllegalArgumentException iae) {
            logger.info("Error deserializing BT address");
        }
    }
}
