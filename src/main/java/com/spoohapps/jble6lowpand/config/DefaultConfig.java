package com.spoohapps.jble6lowpand.config;

import com.spoohapps.jble6lowpand.DeviceService;

public class DefaultConfig implements DaemonConfig {

    private static final int defaultScanDurationMs = 3000;
    private static final int defaultTimeBetweenScansMs = 2000;
    private static final int defaultTimeBetweenConnectionAttemptsMs = 1000;
    private static final int defaultControllerPort = 8089;

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
    public int getControllerPort() {
        return defaultControllerPort;
    }

    @Override
    public String getAllocatorType() {
        return DeviceServiceType.NATIVE_BLE_IPSP.getName();
    }

    @Override
    public String getKnownDevicesType() {
        return KnownDevicesType.WHITELIST.getName();
    }

    @Override
    public String getWhitelistPath() {
        return defaultWhitelistPath;
    }

    @Override
    public String getKnownDevicesHost() {
        return null;
    }

    @Override
    public int getKnownDevicesPort() {
        return 0;
    }
}
