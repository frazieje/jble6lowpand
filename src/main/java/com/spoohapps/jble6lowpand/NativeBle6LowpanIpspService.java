package com.spoohapps.jble6lowpand;

import com.spoohapps.jble6lowpand.model.BTAddress;

public class NativeBle6LowpanIpspService implements Ble6LowpanIpspService {
	
	static {
		System.loadLibrary("ble6lowpand");	
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
