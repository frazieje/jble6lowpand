package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.EUI48Address;

public interface DeviceService {
	EUI48Address[] scanDevices(int timeoutMs) throws DeviceServiceException;
	boolean connectDevice(EUI48Address address) throws DeviceServiceException;
	boolean disconnectDevice(EUI48Address address) throws DeviceServiceException;
	EUI48Address[] getConnectedDevices() throws DeviceServiceException;
	boolean initializeDevice() throws DeviceServiceException;
}
