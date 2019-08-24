package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.EUI48Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeBle6LowpanIpspService implements DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(NativeBle6LowpanIpspService.class);
	
	static {
		System.loadLibrary("ble6lowpan");
	}	
	
	@Override
	public EUI48Address[] scanDevices(int timeoutMs) throws DeviceServiceException {
		return scanDevicesInternal((timeoutMs / 1000));
	}

	private native EUI48Address[] scanDevicesInternal(int timeoutSeconds) throws NativeBle6LowpanIpspException;

	@Override
	public native boolean connectDevice(EUI48Address address);

	@Override
	public native boolean disconnectDevice(EUI48Address address);

	@Override
	public native EUI48Address[] getConnectedDevices();

	@Override
	public native boolean initializeDevice();
	
}
