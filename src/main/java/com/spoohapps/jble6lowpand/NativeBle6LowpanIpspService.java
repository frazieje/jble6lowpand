package com.spoohapps.jble6lowpand;

public class NativeBle6LowpanIpspService implements Ble6LowpanIpspService {
	
	static {
		System.loadLibrary("ble6lowpand");	
	}	
	
	@Override
	public String[] scanIpspDevices(int timeoutMs) {
		return scanIpspDevicesInternal((timeoutMs / 1000));
	}

	private native String[] scanIpspDevicesInternal(int timeoutSeconds);

	@Override
	public native boolean connectIpspDevice(String address);

	@Override
	public native boolean disconnectIpspDevice(String address);

	@Override
	public native String[] getConnectedIpspDevices();
	
}
