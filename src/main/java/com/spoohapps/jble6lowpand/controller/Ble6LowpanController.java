package com.spoohapps.jble6lowpand.controller;

import com.spoohapps.jble6lowpand.config.DaemonConfig;
import com.spoohapps.jble6lowpand.model.BTAddress;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Ble6LowpanController {
    Set<BTAddress> getConnectedDevices();
    Set<BTAddress> getAvailableDevices();
    Set<BTAddress> getKnownDevices();
    void addKnownDevice(BTAddress address);
    void removeKnownDevice(BTAddress address);
    DaemonConfig getConfig();
}
