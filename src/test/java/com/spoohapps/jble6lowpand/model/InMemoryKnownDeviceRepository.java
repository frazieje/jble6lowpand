package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.EUI48Address;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class InMemoryKnownDeviceRepository implements KnownDeviceRepository {

    private final Set<EUI48Address> knownDevices;

    public InMemoryKnownDeviceRepository() {
        knownDevices = new CopyOnWriteArraySet<>();
    }

    public InMemoryKnownDeviceRepository(Collection<EUI48Address> seedSet) {
        knownDevices = new CopyOnWriteArraySet<>(seedSet);
    }

    @Override
    public boolean contains(EUI48Address address) {
        return knownDevices.contains(address);
    }

    @Override
    public boolean add(EUI48Address address) {
        return knownDevices.add(address);
    }

    @Override
    public boolean remove(EUI48Address address) {
        return knownDevices.remove(address);
    }

    @Override
    public boolean update(EUI48Address address) {
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
    public Set<EUI48Address> getAll() {
        return new HashSet<>(knownDevices);
    }
}
