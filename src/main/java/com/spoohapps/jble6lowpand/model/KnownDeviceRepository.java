package com.spoohapps.jble6lowpand.model;

import java.util.Set;

public interface KnownDeviceRepository {
	boolean contains(BTAddress address);
	boolean add(BTAddress address);
	boolean remove(BTAddress address);
	boolean update(BTAddress address);

	void startWatcher();
	void stopWatcher();
	void clear();
	Set<BTAddress> getAll();
}
