package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.MACAddress;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class InMemoryKnownDeviceRepository implements KnownDeviceRepository {

    private final Set<MACAddress> knownDevices;

    public InMemoryKnownDeviceRepository() {
        knownDevices = new CopyOnWriteArraySet<>();
    }

    public InMemoryKnownDeviceRepository(Collection<MACAddress> seedSet) {
        knownDevices = new CopyOnWriteArraySet<>(seedSet);
    }

    @Override
    public boolean contains(MACAddress address) {
        return knownDevices.contains(address);
    }

    @Override
    public boolean add(MACAddress address) {
        return knownDevices.add(address);
    }

    @Override
    public boolean remove(MACAddress address) {
        return knownDevices.remove(address);
    }

    @Override
    public boolean update(MACAddress address) {
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
    public Set<MACAddress> getAll() {
        return new HashSet<>(knownDevices);
    }
}
