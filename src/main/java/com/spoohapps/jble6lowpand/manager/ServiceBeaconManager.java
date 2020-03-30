package com.spoohapps.jble6lowpand.manager;

import com.spoohapps.farcommon.manager.AbstractManager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.model.ServiceBeaconHandler;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ServiceBeaconManager extends AbstractManager<Set<EUI48Address>> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBeaconManager.class);

    private final List<ServiceBeaconHandler> serviceBeaconHandlers;
    private final KnownDeviceRepository knownDevices;

    public ServiceBeaconManager(
            ScheduledExecutorService executorService,
            ManagerSettings managerSettings,
            List<ServiceBeaconHandler> serviceBeaconHandlers,
            KnownDeviceRepository knownDevices) {

        super(executorService, managerSettings);

        this.serviceBeaconHandlers = serviceBeaconHandlers;
        this.knownDevices = knownDevices;

    }

    @Override
    protected void doProcess() {
        logger.trace("publishing service details");
        for (ServiceBeaconHandler consumer : serviceBeaconHandlers) {
            logger.trace("publishing known devices to consumer");
            try {
                consumer.broadcastDeviceList(knownDevices.getAll());
            } catch (Exception e) {
                logger.error("error publishing to consumer", e);
            }
            try {
                consumer.broadcastServices();
            } catch (Exception e) {
                logger.error("error broadcasting services", e);
            }
        }

    }

    @Override
    protected Set<EUI48Address> doGetResource() {
        return knownDevices.getAll();
    }
}
