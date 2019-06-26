package com.spoohapps.jble6lowpand.tasks;

import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.DeviceService;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class BleIpspConnector implements Runnable {

    private final CopyOnWriteArraySet<EUI48Address> connectedDevices;
    private final CopyOnWriteArraySet<EUI48Address> availableDevices;
    private final DeviceService deviceService;
    private final KnownDeviceRepository knownDevices;

    private final Logger logger = LoggerFactory.getLogger(BleIpspConnector.class);

    public BleIpspConnector(DeviceService ipspService, KnownDeviceRepository knownDevices, CopyOnWriteArraySet<EUI48Address> availableDevices, CopyOnWriteArraySet<EUI48Address> connectedDevices) {
        this.deviceService = ipspService;
        this.connectedDevices = connectedDevices;
        this.availableDevices = availableDevices;
        this.knownDevices = knownDevices;
    }

    @Override
    public void run() {
        for (EUI48Address address : availableDevices) {
            if (knownDevices.contains(address) && !connectedDevices.contains(address)) {
                logger.info("Connecting to {} ... ", address.toString());
                try {
                    if (deviceService.connectDevice(address)) {
                        logger.info("Connected to {}", address.toString());
                        break;
                    } else {
                        logger.error("Could not connect to {}", address.toString());
                    }
                } catch (Exception e) {
                    logger.error("Could not connect to {}", address.toString());
                    logger.error(e.getMessage());
                }
            }
        }
        try {
            EUI48Address[] devices = deviceService.getConnectedDevices();
            Set<EUI48Address> connectedAddresses = new HashSet<>();
            for (int i = 0; i < devices.length; i++) {
                try {
                    connectedAddresses.add(devices[i]);
                    if (!knownDevices.contains(devices[i])) {
                        try {
                            deviceService.disconnectDevice(devices[i]);
                        } catch (Exception e) {
                            logger.error("Error disconnecting from unknown connected BT address {}", devices[i]);
                            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
        }
    }
}
