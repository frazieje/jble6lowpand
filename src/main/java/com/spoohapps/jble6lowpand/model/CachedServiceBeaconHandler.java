package com.spoohapps.jble6lowpand.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoohapps.farcommon.cache.Cache;
import com.spoohapps.farcommon.cache.CacheProvider;
import com.spoohapps.farcommon.model.DeviceListing;
import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.farcommon.model.ServiceBeaconMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CachedServiceBeaconHandler implements ServiceBeaconHandler {

    private final CacheProvider cacheProvider;

    private final ServiceBeacon serviceBeacon;

    private final String knownDevicesKey = "knownDevices";

    private final int expiresInSeconds = 10;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public CachedServiceBeaconHandler(CacheProvider cacheProvider, ServiceBeacon serviceBeacon) {

        this.cacheProvider = cacheProvider;

        this.serviceBeacon = serviceBeacon;

    }

    @Override
    public void broadcastDeviceList(Set<EUI48Address> deviceList) {

        Cache<DeviceListing> cache = cacheProvider.acquire(DeviceListing.class);

        if (cache == null) {
            return;
        }

        DeviceListing devices = new DeviceListing();
        devices.addAll(deviceList);

        try {
            cache.put(knownDevicesKey, devices, expiresInSeconds)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void broadcastServices() {

        Cache<ServiceBeaconMessage> cache = cacheProvider.acquire(ServiceBeaconMessage.class);

        if (cache == null) {
            return;
        }

        try {

            ServiceBeaconMessage message = cache.get(ServiceBeaconMessage.class.getSimpleName()).get(1, TimeUnit.SECONDS);

            serviceBeacon.broadcast(objectMapper.writeValueAsString(message));

        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
