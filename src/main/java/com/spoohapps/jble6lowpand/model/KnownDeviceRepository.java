package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Set;

public interface KnownDeviceRepository {

	boolean contains(EUI48Address address);
	boolean add(EUI48Address address);
	boolean remove(EUI48Address address);
	boolean update(EUI48Address address);

	void startWatcher();
	void stopWatcher();
	void clear();
	Set<EUI48Address> getAll();
}
