package com.spoohapps.jble6lowpand.controller;

import com.spoohapps.farcommon.model.MACAddress;
import com.spoohapps.jble6lowpand.config.DaemonConfig;

import java.util.Set;

public interface Controller {
    Set<MACAddress> getConnectedDevices();
    Set<MACAddress> getAvailableDevices();
    Set<MACAddress> getKnownDevices();
    boolean addKnownDevice(MACAddress address);
    boolean removeKnownDevice(MACAddress address);
    boolean updateKnownDevice(MACAddress address);
    DaemonConfig getConfig();
}
