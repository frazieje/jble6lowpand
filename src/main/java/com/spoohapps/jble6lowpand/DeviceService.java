package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.MACAddress;

public interface DeviceService {
	MACAddress[] scanDevices(int timeoutMs) throws DeviceServiceException;
	boolean connectDevice(MACAddress address) throws DeviceServiceException;
	boolean disconnectDevice(MACAddress address) throws DeviceServiceException;
	MACAddress[] getConnectedDevices() throws DeviceServiceException;
	boolean initializeDevice() throws DeviceServiceException;
}
