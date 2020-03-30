package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ServiceBeaconHandler {

    void broadcastDeviceList(Set<EUI48Address> deviceList);

    void broadcastServices();

}
