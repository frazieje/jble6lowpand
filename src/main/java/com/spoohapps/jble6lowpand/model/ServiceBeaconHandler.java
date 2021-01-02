package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.MACAddress;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ServiceBeaconHandler {

    void broadcastDeviceList(Set<MACAddress> deviceList);

    void broadcastServices();

}
