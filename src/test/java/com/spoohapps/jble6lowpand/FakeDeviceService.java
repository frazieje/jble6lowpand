package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class FakeDeviceService implements DeviceService {
	
	private static final int maxNumOfFakeConnections = 8;
	
	private final CopyOnWriteArraySet<EUI48Address> currentConnections = new CopyOnWriteArraySet<>();
	private final CopyOnWriteArraySet<EUI48Address> seedAddresses;

	public FakeDeviceService() {
        seedAddresses = new CopyOnWriteArraySet<>();
    }
	
	public FakeDeviceService(Collection<EUI48Address> seedAddresses) {
	    this.seedAddresses = new CopyOnWriteArraySet<>(seedAddresses);
	}

	public void addSeedAddress(EUI48Address address) {
        seedAddresses.add(address);
    }

    public void clearSeedAddresses() {
	    seedAddresses.clear();
    }
	
	@Override
	public EUI48Address[] scanDevices(int timeoutMs) {
		Random random = new Random();
		int num = (Math.abs(random.nextInt()) % maxNumOfFakeConnections) + 1;
        List<EUI48Address> addresses = new ArrayList<>();
        for (EUI48Address address : seedAddresses) {
            if (!currentConnections.contains(address)) {
                addresses.add(address);
            }
        }
		for (int i = 0; i < num; i++) {
			addresses.add(EUI48Address.random());
		}
		try {
			Thread.sleep(timeoutMs);
		} catch(InterruptedException ie) {
			
		}
		return addresses.toArray(new EUI48Address[0]);
	}

	@Override
	public boolean connectDevice(EUI48Address address) {
	    currentConnections.add(address);
		return true;
	}

	@Override
	public boolean disconnectDevice(EUI48Address address) {
	    currentConnections.remove(address);
		return true;
	}

	@Override
	public EUI48Address[] getConnectedDevices() {
		return currentConnections.toArray(new EUI48Address[0]);
	}

	@Override
	public boolean initializeDevice() {
		return true;
	}


}
