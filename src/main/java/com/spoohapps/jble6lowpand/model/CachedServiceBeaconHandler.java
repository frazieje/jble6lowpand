package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.cache.Cache;
import com.spoohapps.farcommon.cache.CacheProvider;
import com.spoohapps.farcommon.model.DeviceListing;
import com.spoohapps.farcommon.model.EUI48Address;

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

        Cache<String> cache = cacheProvider.acquire(String.class);

        if (cache == null) {
            return;
        }

        try {

            String beacon = cache.get("far_serviceBeacon").get(1, TimeUnit.SECONDS);

            serviceBeacon.broadcast(beacon);

        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
