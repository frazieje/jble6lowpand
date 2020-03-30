package com.spoohapps.jble6lowpand.config;

import com.spoohapps.farcommon.config.ConfigFlags;

import java.util.List;

public interface DaemonConfig {
    @ConfigFlags({"scanDuration", "d"})
    int getScanDurationMs();

    @ConfigFlags({"scanTimeout", "t"})
    int getScanTimeoutMs();

    @ConfigFlags({"connectTimeout", "c"})
    int getConnectTimeoutMs();

    @ConfigFlags({"disconnectTimeout", "v"})
    int getDisconnectTimeoutMs();

    @ConfigFlags({"publishTimeout", "o"})
    int getPublishTimeoutMs();

    @ConfigFlags({"controllerPort", "p"})
    int getControllerPort();

    @ConfigFlags({"allocatorType", "a"})
    String getAllocatorType();

    @ConfigFlags({"serviceBeaconHandlers", "k"})
    List<String> getServiceBeaconHandlers();

    @ConfigFlags({"whitelistPath", "w"})
    String getWhitelistPath();

    @ConfigFlags({"redisHost", "h"})
    String getRedisHost();

    @ConfigFlags({"redisPort", "u"})
    int getRedisPort();

    @ConfigFlags("serviceBeaconMulticastAddress")
    String getServiceBeaconMulticastAddress();

    @ConfigFlags("serviceBeaconMulticastPort")
    int getServiceBeaconMulticastPort();


}
