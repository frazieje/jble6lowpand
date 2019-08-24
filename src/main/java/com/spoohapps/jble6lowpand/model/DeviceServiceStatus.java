package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Set;

public class DeviceServiceStatus {

    private final Set<EUI48Address> availableDevices;
    private final Set<EUI48Address> connectedDevices;

    public DeviceServiceStatus(Set<EUI48Address> availableDevices, Set<EUI48Address> connectedDevices) {

        this.availableDevices = availableDevices;
        this.connectedDevices = connectedDevices;

    }

    public Set<EUI48Address> getAvailableDevices() {
        return availableDevices;
    }

    public Set<EUI48Address> getConnectedDevices() {
        return connectedDevices;
    }
}
