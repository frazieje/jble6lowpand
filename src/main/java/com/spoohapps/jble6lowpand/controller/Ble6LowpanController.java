package com.spoohapps.jble6lowpand.controller;

import com.spoohapps.jble6lowpand.model.BTAddress;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Ble6LowpanController extends Remote {
    Set<BTAddress> getConnectedDevices() throws RemoteException;
    Set<BTAddress> getAvailableDevices() throws RemoteException;
    Set<BTAddress> getKnownDevices() throws RemoteException;
    void addKnownDevice(BTAddress address) throws RemoteException;
    void removeKnownDevice(BTAddress address) throws RemoteException;
}
