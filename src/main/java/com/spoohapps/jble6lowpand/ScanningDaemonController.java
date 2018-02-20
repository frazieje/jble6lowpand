package com.spoohapps.jble6lowpand;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface ScanningDaemonController extends Remote {
    Set<String> getConnectedDevices() throws RemoteException;
    Set<String> getAvailableDevices() throws RemoteException;
    Set<String> getKnownDevices() throws RemoteException;
    void addKnownDevice(String address) throws RemoteException;
    void removeKnownDevice(String address) throws RemoteException;
}
