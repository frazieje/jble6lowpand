package com.spoohapps.jble6lowpand.controller;

import com.spoohapps.farcommon.model.EUI48Address;
import com.spoohapps.jble6lowpand.config.DaemonConfig;

import java.util.Set;

public interface Ble6LowpanController {
    Set<EUI48Address> getConnectedDevices();
    Set<EUI48Address> getAvailableDevices();
    Set<EUI48Address> getKnownDevices();
    boolean addKnownDevice(EUI48Address address);
    boolean removeKnownDevice(EUI48Address address);
    boolean updateKnownDevice(EUI48Address address);
    DaemonConfig getConfig();
}
