package com.spoohapps.jble6lowpand.config;

import java.util.ArrayList;
import java.util.List;

public class DefaultConfig implements DaemonConfig {

    private static final int defaultScanDurationMs = 4000;
    private static final int defaultTimeBetweenScansMs = 1000;
    private static final int defaultTimeBetweenConnectionAttemptsMs = 2000;
    private static final int defaultTimeBetweenDisconnectionAttemptsMs = 2000;
    private static final int defaultControllerPort = 8089;
    private static final int defaultPublishTimeout = 3000;

    private static final String defaultWhitelistPath = "/opt/jble6lowpand/data/knowndevices.conf";

    @Override
    public int getScanDurationMs() {
        return defaultScanDurationMs;
    }

    @Override
    public int getScanTimeoutMs() {
        return defaultTimeBetweenScansMs;
    }

    @Override
    public int getConnectTimeoutMs() {
        return defaultTimeBetweenConnectionAttemptsMs;
    }

    @Override
    public int getDisconnectTimeoutMs() {
        return defaultTimeBetweenDisconnectionAttemptsMs;
    }

    @Override
    public int getPublishTimeoutMs() {
        return defaultPublishTimeout;
    }

    @Override
    public int getControllerPort() {
        return defaultControllerPort;
    }

    @Override
    public String getAllocatorType() {
        return DeviceServiceType.none.name();
    }

    @Override
    public List<String> getDeviceListingConsumers() {
        return new ArrayList<>();
    }

    @Override
    public String getWhitelistPath() {
        return defaultWhitelistPath;
    }

    @Override
    public String getRedisHost() {
        return null;
    }

    @Override
    public int getRedisPort() {
        return 0;
    }
}
