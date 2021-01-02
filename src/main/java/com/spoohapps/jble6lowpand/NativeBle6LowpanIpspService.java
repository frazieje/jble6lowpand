package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeBle6LowpanIpspService implements DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(NativeBle6LowpanIpspService.class);
	
	static {
		System.loadLibrary("ble6lowpan");
	}	
	
	@Override
	public MACAddress[] scanDevices(int timeoutMs) throws DeviceServiceException {
		return scanDevicesInternal((timeoutMs / 1000));
	}

	private native MACAddress[] scanDevicesInternal(int timeoutSeconds) throws NativeBle6LowpanIpspException;

	@Override
	public native boolean connectDevice(MACAddress address) throws NativeBle6LowpanIpspException;

	@Override
	public native boolean disconnectDevice(MACAddress address) throws NativeBle6LowpanIpspException;

	@Override
	public native MACAddress[] getConnectedDevices() throws NativeBle6LowpanIpspException;

	@Override
	public native boolean initializeDevice() throws NativeBle6LowpanIpspException;
	
}
