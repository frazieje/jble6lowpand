package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface DeviceListingConsumer {

    void accept(Set<EUI48Address> deviceList);

}
