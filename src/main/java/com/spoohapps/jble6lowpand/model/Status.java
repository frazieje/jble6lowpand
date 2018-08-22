package com.spoohapps.jble6lowpand.model;

import com.spoohapps.jble6lowpand.config.DaemonConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Status {

    private String whitelistFile;

    private int scanDurationMs;

    private int scanTimeoutMs;

    private int connectTimeoutMs;

    private int controllerPort;

    private Set<String> connectedDevices;
    private Set<String> knownDevices;
    private Set<String> availableDevices;

    public Status(DaemonConfig config, Set<BTAddress> availableDevices, Set<BTAddress> connectedDevices, Set<BTAddress> knownDevices) {
        whitelistFile = config.getWhitelistPath();
        scanDurationMs = config.getScanDurationMs();
        scanTimeoutMs = config.getScanTimeoutMs();
        connectTimeoutMs = config.getConnectTimeoutMs();
        controllerPort = config.getControllerPort();
        this.availableDevices = availableDevices.stream().map(BTAddress::toString).collect(Collectors.toSet());
        this.connectedDevices = connectedDevices.stream().map(BTAddress::toString).collect(Collectors.toSet());
        this.knownDevices = knownDevices.stream().map(BTAddress::toString).collect(Collectors.toSet());
    }

    public String getWhitelistFile() {
        return whitelistFile;
    }

    public void setWhitelistFile(String whitelistFile) {
        this.whitelistFile = whitelistFile;
    }

    public int getScanDurationMs() {
        return scanDurationMs;
    }

    public void setScanDurationMs(int scanDurationMs) {
        this.scanDurationMs = scanDurationMs;
    }

    public int getScanTimeoutMs() {
        return scanTimeoutMs;
    }

    public void setScanTimeoutMs(int scanTimeoutMs) {
        this.scanTimeoutMs = scanTimeoutMs;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getControllerPort() {
        return controllerPort;
    }

    public void setControllerPort(int controllerPort) {
        this.controllerPort = controllerPort;
    }

    public Set<String> getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectedDevices(Set<String> connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    public Set<String> getKnownDevices() {
        return knownDevices;
    }

    public void setKnownDevices(Set<String> knownDevices) {
        this.knownDevices = knownDevices;
    }

    public Set<String> getAvailableDevices() {
        return availableDevices;
    }

    public void setAvailableDevices(Set<String> availableDevices) {
        this.availableDevices = availableDevices;
    }
}
