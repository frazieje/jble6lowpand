package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.BTAddress;

public class NativeBle6LowpanIpspService implements Ble6LowpanIpspService {
	
	static {
		System.loadLibrary("ble6lowpan");
	}	
	
	@Override
	public BTAddress[] scanIpspDevices(int timeoutMs) {
		return scanIpspDevicesInternal((timeoutMs / 1000));
	}

	private native BTAddress[] scanIpspDevicesInternal(int timeoutSeconds);

	@Override
	public native boolean connectIpspDevice(String address);

	@Override
	public native boolean disconnectIpspDevice(String address);

	@Override
	public native String[] getConnectedIpspDevices();
	
}
