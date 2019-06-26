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

    @ConfigFlags({"allocatorType", "a"})
    String getAllocatorType();

    @ConfigFlags({"knownDevicesType", "k"})
    String getKnownDevicesType();

    @ConfigFlags({"whitelistPath", "w"})
    String getWhitelistPath();

    @ConfigFlags({"knownDevicesHost", "h"})
    String getKnownDevicesHost();

    @ConfigFlags({"knownDevicesPort", "u"})
    int getKnownDevicesPort();
}
