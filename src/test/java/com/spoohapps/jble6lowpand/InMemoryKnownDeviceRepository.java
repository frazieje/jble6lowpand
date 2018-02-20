package com.spoohapps.jble6lowpand;

import com.spoohapps.jble6lowpand.model.BTAddress;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class InMemoryKnownDeviceRepository implements KnownDeviceRepository {

    private final Set<BTAddress> knownDevices;

    public InMemoryKnownDeviceRepository() {
        knownDevices = new CopyOnWriteArraySet<>();
    }

    public InMemoryKnownDeviceRepository(Collection<BTAddress> seedSet) {
        knownDevices = new CopyOnWriteArraySet<>(seedSet);
    }

    @Override
    public boolean contains(BTAddress address) {
        return knownDevices.contains(address);
    }

    @Override
    public void add(BTAddress address) {
        knownDevices.add(address);
    }

    @Override
    public void remove(BTAddress address) {
        knownDevices.remove(address);
    }

    @Override
    public void startWatcher() {

    }

    @Override
    public void stopWatcher() {

    }

    @Override
    public void clear() { knownDevices.clear(); }

    @Override
    public Set<BTAddress> getAll() {
        return new HashSet<>(knownDevices);
    }
}
