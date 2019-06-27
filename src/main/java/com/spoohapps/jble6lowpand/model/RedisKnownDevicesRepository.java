package com.spoohapps.jble6lowpand.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoohapps.farcommon.cache.Cache;
import com.spoohapps.farcommon.cache.CacheConnectionSettings;
import com.spoohapps.farcommon.cache.CacheProvider;
import com.spoohapps.farcommon.cache.RedisCacheProvider;
import com.spoohapps.farcommon.manager.Manager;
import com.spoohapps.farcommon.manager.ManagerSettings;
import com.spoohapps.farcommon.manager.RedisCacheConnectionManager;
import com.spoohapps.farcommon.model.EUI48Address;
import io.lettuce.core.api.StatefulRedisConnection;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedisKnownDevicesRepository implements KnownDeviceRepository {


    private final Manager<StatefulRedisConnection<String, String>> redisConnectionManager;

    private final CacheProvider cacheProvider;

    private final String knownDevicesKey = "knownDevices";

    public RedisKnownDevicesRepository(ScheduledExecutorService scheduledExecutorService,
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
    public boolean contains(EUI48Address address) {
        Cache<EUI48Address> cache = cacheProvider.acquire(EUI48Address.class);
        try {
            return cache.getHashItem(knownDevicesKey, address.getAddress()).get() != null;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean add(EUI48Address address) {
        Cache<EUI48Address> cache = cacheProvider.acquire(EUI48Address.class);
        try {
            Boolean result = cache.putHashItem(knownDevicesKey, address.getAddress(), address).get();
            return  result != null && result;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean remove(EUI48Address address) {
        Cache<EUI48Address> cache = cacheProvider.acquire(EUI48Address.class);
        try {
            Boolean result = cache.removeHashItem(knownDevicesKey, address.getAddress()).get();
            return  result != null && result;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public boolean update(EUI48Address address) {
        return add(address);
    }

    @Override
    public void startWatcher() {
        redisConnectionManager.start();
    }

    @Override
    public void stopWatcher() {
        redisConnectionManager.stop();
    }

    @Override
    public void clear() {
        Cache<EUI48Address> cache = cacheProvider.acquire(EUI48Address.class);
        try {
            cache.removeHash(knownDevicesKey).get();
        } catch (InterruptedException | ExecutionException ignored) {

        }
    }

    @Override
    public Set<EUI48Address> getAll() {
        Cache<EUI48Address> cache = cacheProvider.acquire(EUI48Address.class);
        try {
            return new HashSet<>(cache.getHash(knownDevicesKey).get().values());
        } catch (InterruptedException | ExecutionException ignored) {
            return new HashSet<>();
        }
    }

}
