package com.spoohapps.jble6lowpand.config;

public interface DaemonConfig {
    int getScanDurationMs();
    int getScanTimeoutMs();
    int getConnectTimeoutMs();
    int getControllerPort();
    String getWhitelistPath();
}
