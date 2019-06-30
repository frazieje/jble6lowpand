package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface DeviceListingConsumer {

    public void start();
    public void stop();
    void accept(Set<EUI48Address> deviceList);

}
