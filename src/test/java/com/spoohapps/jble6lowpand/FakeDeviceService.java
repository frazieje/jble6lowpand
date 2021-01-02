package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.MACAddress;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class FakeDeviceService implements DeviceService {
	
	private static final int maxNumOfFakeConnections = 8;
	
	private final CopyOnWriteArraySet<MACAddress> currentConnections = new CopyOnWriteArraySet<>();
	private final CopyOnWriteArraySet<MACAddress> seedAddresses;

	public FakeDeviceService() {
        seedAddresses = new CopyOnWriteArraySet<>();
    }
	
	public FakeDeviceService(Collection<MACAddress> seedAddresses) {
	    this.seedAddresses = new CopyOnWriteArraySet<>(seedAddresses);
	}

	public void addSeedAddress(MACAddress address) {
        seedAddresses.add(address);
    }

    public void clearSeedAddresses() {
	    seedAddresses.clear();
    }
	
	@Override
	public MACAddress[] scanDevices(int timeoutMs) {
		Random random = new Random();
		int num = (Math.abs(random.nextInt()) % maxNumOfFakeConnections) + 1;
        List<MACAddress> addresses = new ArrayList<>();
        for (MACAddress address : seedAddresses) {
            if (!currentConnections.contains(address)) {
                addresses.add(address);
            }
        }
		for (int i = 0; i < num; i++) {
			boolean eui64 = random.nextBoolean();
			addresses.add(eui64 ? MACAddress.randomEUI64() : MACAddress.randomEUI48());
		}
		try {
			Thread.sleep(timeoutMs);
		} catch(InterruptedException ie) {
			
		}
		return addresses.toArray(new MACAddress[0]);
	}

	@Override
	public boolean connectDevice(MACAddress address) {
	    currentConnections.add(address);
		return true;
	}

	@Override
	public boolean disconnectDevice(MACAddress address) {
	    currentConnections.remove(address);
		return true;
	}

	@Override
	public MACAddress[] getConnectedDevices() {
		return currentConnections.toArray(new MACAddress[0]);
	}

	@Override
	public boolean initializeDevice() {
		return true;
	}


}
