package com.spoohapps.jble6lowpand.tasks;

import com.spoohapps.jble6lowpand.Ble6LowpanIpspService;
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
        for (BTAddress address : availableDevices) {
            if (knownDevices.contains(address) && !connectedDevices.contains(address)) {
                logger.info("Connecting to {} ... ", address.toString());
                try {
                    if (ble6LowpanIpspService.connectIpspDevice(address.getAddress())) {
                        logger.info("Connected to {}", address.toString());
                        break;
                    } else {
                        logger.error("Could not connect to {}", address.toString());
                    }
                } catch (Exception e) {
                    logger.error("Could not connect to {}", address.toString());
                }
            }
        }
        try {
            String[] devices = ble6LowpanIpspService.getConnectedIpspDevices();
            Set<BTAddress> connectedAddresses = new HashSet<>();
            for (int i = 0; i < devices.length; i++) {
                try {
                    BTAddress address = new BTAddress(devices[i]);
                    connectedAddresses.add(address);
                    if (!knownDevices.contains(address)) {
                        try {
                            ble6LowpanIpspService.disconnectIpspDevice(address.getAddress());
                        } catch (Exception e) {
                            logger.error("Error disconnecting from unknown connected BT address {}", devices[i]);
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    logger.error("Error deserializing connected BT address {}", devices[i]);
                }
            }
            connectedDevices.retainAll(connectedAddresses);
            connectedDevices.addAll(connectedAddresses);
        } catch (Exception e) {
            logger.error("Error getting connected devices {}", e.getMessage());
        }
    }
}
