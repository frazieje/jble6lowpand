package com.spoohapps.jble6lowpand.config;

import com.spoohapps.farcommon.config.ConfigFlags;

public interface DaemonConfig {
    @ConfigFlags({"scanDuration", "d"})
    int getScanDurationMs();

    @ConfigFlags({"scanTimeout", "t"})
    int getScanTimeoutMs();

    @ConfigFlags({"connectTimeout", "c"})
    int getConnectTimeoutMs();

    @ConfigFlags({"controllerPort", "p"})
    int getControllerPort();

    @ConfigFlags({"whitelistPath", "w"})
    String getWhitelistPath();
}
