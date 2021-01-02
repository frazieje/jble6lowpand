package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.MACAddress;

import java.util.Set;

public interface KnownDeviceRepository {

	boolean contains(MACAddress address);
	boolean add(MACAddress address);
	boolean remove(MACAddress address);
	boolean update(MACAddress address);

	void startWatcher();
	void stopWatcher();
	void clear();
	Set<MACAddress> getAll();
}
