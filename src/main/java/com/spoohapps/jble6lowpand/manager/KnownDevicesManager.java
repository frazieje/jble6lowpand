package com.spoohapps.jble6lowpand.manager;

import com.spoohapps.farcommon.manager.AbstractManager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.model.DeviceListingConsumer;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class KnownDevicesManager extends AbstractManager<Set<EUI48Address>> {

    private static final Logger logger = LoggerFactory.getLogger(KnownDevicesManager.class);

    private final List<DeviceListingConsumer> deviceListingConsumers;
    private final KnownDeviceRepository knownDevices;

    public KnownDevicesManager(
            ScheduledExecutorService executorService,
            ManagerSettings managerSettings,
            List<DeviceListingConsumer> deviceListingConsumers,
            KnownDeviceRepository knownDevices) {

        super(executorService, managerSettings);

        this.deviceListingConsumers = deviceListingConsumers;
        this.knownDevices = knownDevices;

    }

    @Override
    protected void doProcess() {
        logger.trace("publishing known devices");
        for (DeviceListingConsumer consumer : deviceListingConsumers) {
            logger.trace("publishing to consumer");
            try {
                consumer.accept(knownDevices.getAll());
            } catch (Exception e) {
                logger.error("error publishing to consumer", e);
            }
        }

    }

    @Override
    protected Set<EUI48Address> doGetResource() {
        return knownDevices.getAll();
    }
}
