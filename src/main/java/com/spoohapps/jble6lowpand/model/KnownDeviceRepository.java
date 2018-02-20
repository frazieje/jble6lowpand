package com.spoohapps.jble6lowpand.model;

import java.util.Set;

public interface KnownDeviceRepository {
	boolean contains(BTAddress address);
	void add(BTAddress address);
	void remove(BTAddress address);

	void startWatcher();
	void stopWatcher();
	void clear();
	Set<BTAddress> getAll();
}
