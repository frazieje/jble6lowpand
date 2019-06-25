package com.spoohapps.jble6lowpand;

import com.spoohapps.farcommon.model.BTAddress;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class FakeBle6LowpanIpspService implements Ble6LowpanIpspService {
	
	private static final int maxNumOfFakeConnections = 8;
	
	private final CopyOnWriteArraySet<BTAddress> currentConnections = new CopyOnWriteArraySet<>();
	private final CopyOnWriteArraySet<BTAddress> seedAddresses;

	public FakeBle6LowpanIpspService() {
        seedAddresses = new CopyOnWriteArraySet<>();
    }
	
	public FakeBle6LowpanIpspService(Collection<BTAddress> seedAddresses) {
	    this.seedAddresses = new CopyOnWriteArraySet<>(seedAddresses);
	}

	public void addSeedAddress(BTAddress address) {
        seedAddresses.add(address);
    }

    public void clearSeedAddresses() {
	    seedAddresses.clear();
    }
	
	@Override
	public BTAddress[] scanIpspDevices(int timeoutMs) {
		Random random = new Random();
		int num = (Math.abs(random.nextInt()) % maxNumOfFakeConnections) + 1;
        List<BTAddress> addresses = new ArrayList<>();
        for (BTAddress address : seedAddresses) {
            if (!currentConnections.contains(address)) {
                addresses.add(address);
            }
        }
		for (int i = 0; i < num; i++) {
			addresses.add(BTAddress.random());
		}
		try {
			Thread.sleep(timeoutMs);
		} catch(InterruptedException ie) {
			
		}
		return addresses.toArray(new BTAddress[0]);
	}

	@Override
	public boolean connectIpspDevice(String address) {
	    currentConnections.add(new BTAddress(address));
		return true;
	}

	@Override
	public boolean disconnectIpspDevice(String address) {
	    currentConnections.remove(new BTAddress(address));
		return true;
	}

	@Override
	public String[] getConnectedIpspDevices() {
		List<String> connections = new ArrayList<>();
		for (BTAddress address : currentConnections) {
			connections.add(address.toString());
		}
		return connections.toArray(new String[0]);
	}

	@Override
	public boolean initializeDevice() {
		return true;
	}


}
