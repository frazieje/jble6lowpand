package com.spoohapps.jble6lowpand.tasks;

import com.spoohapps.jble6lowpand.model.DeviceListingConsumer;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Publisher implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Publisher.class);

    private final List<DeviceListingConsumer> deviceListingConsumers;
    private final KnownDeviceRepository knownDevices;

    public Publisher(List<DeviceListingConsumer> deviceListingConsumers, KnownDeviceRepository knownDevices) {
        this.deviceListingConsumers = deviceListingConsumers;
        this.knownDevices = knownDevices;
    }

    @Override
    public void run() {
        logger.trace("publishing known device list");
        deviceListingConsumers.forEach(c -> c.accept(knownDevices.getAll()));
    }

}
