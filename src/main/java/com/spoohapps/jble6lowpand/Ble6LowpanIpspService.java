package com.spoohapps.jble6lowpand;

public interface Ble6LowpanIpspService {
	String[] scanIpspDevices(int timeoutMs);
	boolean connectIpspDevice(String address);
	boolean disconnectIpspDevice(String address);
	String[] getConnectedIpspDevices();
}
