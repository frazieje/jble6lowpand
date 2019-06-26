package com.spoohapps.jble6lowpand.config;

public enum KnownDevicesType {

    WHITELIST("whitelist"),

    REDIS("redis");

    private String name;

    KnownDevicesType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
