package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.MACAddress;

import java.util.Set;

public class DeviceServiceStatus {

    private final Set<MACAddress> availableDevices;
    private final Set<MACAddress> connectedDevices;

    public DeviceServiceStatus(Set<MACAddress> availableDevices, Set<MACAddress> connectedDevices) {

        this.availableDevices = availableDevices;
        this.connectedDevices = connectedDevices;

    }

    public Set<MACAddress> getAvailableDevices() {
        return availableDevices;
    }

    public Set<MACAddress> getConnectedDevices() {
        return connectedDevices;
    }
}
