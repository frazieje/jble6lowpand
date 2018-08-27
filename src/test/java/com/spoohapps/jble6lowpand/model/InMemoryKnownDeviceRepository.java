package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.BTAddress;

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
    public boolean add(BTAddress address) {
        return knownDevices.add(address);
    }

    @Override
    public boolean remove(BTAddress address) {
        return knownDevices.remove(address);
    }

    @Override
    public boolean update(BTAddress address) {
        if (knownDevices.remove(address)) {
            return knownDevices.add(address);
        }
        return false;
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
