package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.BTAddress;

public interface Ble6LowpanIpspService {
	BTAddress[] scanIpspDevices(int timeoutMs);
	boolean connectIpspDevice(String address);
	boolean disconnectIpspDevice(String address);
	String[] getConnectedIpspDevices();
	boolean initializeDevice();
}
