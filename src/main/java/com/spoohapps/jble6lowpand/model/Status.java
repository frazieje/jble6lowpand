package com.spoohapps.jble6lowpand.model;

import java.util.List;

public class Status {

    private String whitelistFile;

    private int scanDurationMs;

    private int scanTimeoutMs;

    private int connectTimeoutMs;

    private int controllerPort;

    private List<BTAddress> connectedDevices;
    private List<BTAddress> knownDevices;
    private List<BTAddress> availableDevices;
}
