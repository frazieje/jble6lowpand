package com.spoohapps.jble6lowpand.manager;

import com.spoohapps.farcommon.manager.AbstractManager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.DeviceService;
import com.spoohapps.jble6lowpand.model.DeviceSetFactory;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

public class ConnectedDevicesManager extends AbstractManager<Set<EUI48Address>> {

    private static final Logger logger = LoggerFactory.getLogger(ConnectedDevicesManager.class);

    private final CopyOnWriteArraySet<EUI48Address> connectedDevices;
    private final DeviceSetFactory deviceSetFactory;
    private final DeviceService deviceService;
    private final KnownDeviceRepository knownDevices;

    public ConnectedDevicesManager(
            ScheduledExecutorService executorService,
            ManagerSettings managerSettings,
            DeviceSetFactory availableDevices,
            DeviceService deviceService,
            KnownDeviceRepository knownDevices) {

        super(executorService, managerSettings);

        connectedDevices = new CopyOnWriteArraySet<>();

        this.deviceSetFactory = availableDevices;
        this.deviceService = deviceService;
        this.knownDevices = knownDevices;

    }

    @Override
    protected void doProcess() {
        for (EUI48Address address : deviceSetFactory.get()) {
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
                            logger.error("Error disconnecting from unknown connected EUI48 address {}", devices[i]);
                            logger.error(e.getMessage());
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    logger.error("Error deserializing connected EUI48 address {}", devices[i]);
                }
            }
            connectedDevices.retainAll(connectedAddresses);
            connectedDevices.addAll(connectedAddresses);
        } catch (Exception e) {
            logger.error("Error getting connected devices {}", e.getMessage());
            logger.error(e.getMessage());
        }

    }

    @Override
    protected Set<EUI48Address> doGetResource() {
        return connectedDevices;
    }

}
