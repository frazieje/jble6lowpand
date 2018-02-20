package com.spoohapps.jble6lowpand;

import com.spoohapps.jble6lowpand.model.BTAddress;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class FakeBle6LowpanIpspService implements Ble6LowpanIpspService {
	
	private static final int maxNumOfFakeConnections = 8;
	private static final int btAddressLength = 12;
	
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
	public String[] scanIpspDevices(int timeoutMs) {
		Random random = new Random();
		int num = (Math.abs(random.nextInt()) % maxNumOfFakeConnections) + 1;
        List<BTAddress> addresses = new ArrayList<>();
        for (BTAddress address : seedAddresses) {
            if (!currentConnections.contains(address)) {
                addresses.add(address);
            }
        }
		for (int i = 0; i < num; i++) {
			addresses.add(new BTAddress(generateRandomBTAddress()));
		}
		try {
			Thread.sleep(timeoutMs);
		} catch(InterruptedException ie) {
			
		}
		return addresses.stream().map(BTAddress::toString).toArray(String[]::new);
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
	
	private String generateRandomBTAddress() {
		Random random = new Random();
		String[] chars = new String[] { "A", "B", "C", "D", "E", "F" };
		
		String address = "";		
		
		for (int i = 1; i <= btAddressLength; i++) {
			int num = Math.abs(random.nextInt()) % 16;
			if (num > 9) {
				address += chars[num-10];
			} else {
				address += num;
			}
			if (i % 2 == 0 && i != btAddressLength) {
				address += ":";
			}
		}
		
		return address;
	}

}
