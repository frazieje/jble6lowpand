package com.spoohapps.jble6lowpand.config;

public enum DeviceServiceType {

    NATIVE_BLE_IPSP("native-ble-ipsp"),

    NONE("none");

    private String name;

    DeviceServiceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
