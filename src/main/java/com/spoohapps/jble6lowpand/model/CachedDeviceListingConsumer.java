package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.cache.Cache;
import com.spoohapps.farcommon.cache.CacheProvider;
import com.spoohapps.farcommon.model.DeviceListing;
import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CachedDeviceListingConsumer implements DeviceListingConsumer {

    private final CacheProvider cacheProvider;

    private final String knownDevicesKey = "knownDevices";

    private final int expiresInSeconds = 10;

    public CachedDeviceListingConsumer(CacheProvider cacheProvider) {

        this.cacheProvider = cacheProvider;

    }

    @Override
    public void accept(Set<EUI48Address> deviceList) {

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
}
