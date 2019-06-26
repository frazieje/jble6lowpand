package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.EUI48Address;

public class NativeBle6LowpanIpspService implements DeviceService {
	
	static {
		System.loadLibrary("ble6lowpan");
	}	
	
	@Override
	public EUI48Address[] scanDevices(int timeoutMs) {
		return scanDevicesInternal((timeoutMs / 1000));
	}

	private native EUI48Address[] scanDevicesInternal(int timeoutSeconds);

	@Override
	public native boolean connectDevice(EUI48Address address);

	@Override
	public native boolean disconnectDevice(EUI48Address address);

	@Override
	public native EUI48Address[] getConnectedDevices();

	@Override
	public native boolean initializeDevice();
	
}
