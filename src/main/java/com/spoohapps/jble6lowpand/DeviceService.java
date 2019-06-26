package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.EUI48Address;

public interface DeviceService {
	EUI48Address[] scanDevices(int timeoutMs);
	boolean connectDevice(EUI48Address address);
	boolean disconnectDevice(EUI48Address address);
	EUI48Address[] getConnectedDevices();
	boolean initializeDevice();
}
