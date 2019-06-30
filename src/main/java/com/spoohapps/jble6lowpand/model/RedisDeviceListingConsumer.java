package com.spoohapps.jble6lowpand.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoohapps.farcommon.cache.Cache;
import com.spoohapps.farcommon.cache.CacheConnectionSettings;
import com.spoohapps.farcommon.cache.CacheProvider;
import com.spoohapps.farcommon.cache.RedisCacheProvider;
import com.spoohapps.farcommon.manager.Manager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.manager.RedisCacheConnectionManager;
import com.spoohapps.farcommon.model.DeviceListing;
import com.spoohapps.farcommon.model.EUI48Address;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedisDeviceListingConsumer implements DeviceListingConsumer {

    private final Manager<StatefulRedisConnection<String, String>> redisConnectionManager;

    private final CacheProvider cacheProvider;

    private final String knownDevicesKey = "knownDevices";

    public RedisDeviceListingConsumer(ScheduledExecutorService scheduledExecutorService,
                              String host,
                              int port) {

        redisConnectionManager = new RedisCacheConnectionManager(
        scheduledExecutorService,
        new CacheConnectionSettings() {
            @Override
            public String host() {
                return host;
            }

            @Override
            public int port() {
                return port;
            }
        },
        new ManagerSettings() {
            @Override
            public long startDelay() {
                return 0;
            }

            @Override
            public TimeUnit startDelayTimeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public long timeout() {
                return 10;
            }

            @Override
            public TimeUnit timeoutTimeUnit() {
                return TimeUnit.SECONDS;
            }
        });

        cacheProvider = new RedisCacheProvider(redisConnectionManager, new ObjectMapper(), null);

    }

    @Override
    public void start() {
        redisConnectionManager.start();
    }

    @Override
    public void stop() {
        redisConnectionManager.stop();
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
            cache.put(knownDevicesKey, devices)
                    .get();
        } catch (InterruptedException | ExecutionException e) {

        }
    }
}
