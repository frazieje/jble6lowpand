package com.spoohapps.jble6lowpand.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config implements DaemonConfig {

    private static final List<String> durationKeys = Arrays.asList("scanDuration", "d");
    private static final List<String> scanTimeoutKeys = Arrays.asList("scanTimeout", "t");
    private static final List<String> connectTimeoutKeys = Arrays.asList("connectTimeout", "c");
    private static final List<String> whitelistPathKeys = Arrays.asList("whitelistPath", "w");
    private static final List<String> controllerPortKeys = Arrays.asList("controllerPort", "p");

    private static final int defaultScanDurationMs = 5000;
    private static final int defaultTimeBetweenScansMs = 5000;
    private static final int defaultTimeBetweenConnectionAttemptsMs = 3000;

    private static final int defaultControllerPort = 1099;

    private static final String defaultWhitelistPath = "./knowndevices.conf";

    private static final Set<String> allKeys =
            Stream.of(durationKeys, scanTimeoutKeys, connectTimeoutKeys, whitelistPathKeys, controllerPortKeys)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

    private static final Set<String> integerKeys =
            Stream.of(durationKeys, scanTimeoutKeys, connectTimeoutKeys, controllerPortKeys)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

    private int scanDurationMs;
    private int scanTimeoutMs;
    private int connectTimeoutMs;
    private int controllerPort;
    private String whitelistPath;

    private Config() {}

    public static Config empty() {
        return new Config();
    }

    public static Config fromDefaults() {
        Config result = new Config();
        result.scanDurationMs = defaultScanDurationMs;
        result.scanTimeoutMs = defaultTimeBetweenScansMs;
        result.connectTimeoutMs = defaultTimeBetweenConnectionAttemptsMs;
        result.whitelistPath = defaultWhitelistPath;
        result.controllerPort = defaultControllerPort;
        return result;
    }

    public static Config fromArgs(String[] args) {
        Config config = new Config();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-") && i != (args.length - 1)) {
                String arg = args[i].substring(1);
                String val = args[i + 1];
                config.setArg(arg, val);
            }
        }
        return config;
    }

    public static Config fromStream(InputStream input) {

        Properties prop = new Properties();

        try {
            Config config = new Config();

            if (input == null)
                return config;

            prop.load(input);

            prop.stringPropertyNames().forEach(p -> config.setArg(p, prop.getProperty(p)));

            return config;

        } catch (IOException ex) {
            ex.printStackTrace();
            return new Config();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Config apply(DaemonConfig config) {
        if (config.getScanDurationMs() > 0)
            scanDurationMs = config.getScanDurationMs();
        if (config.getScanTimeoutMs() > 0)
            scanTimeoutMs = config.getScanTimeoutMs();
        if (config.getConnectTimeoutMs() > 0)
            connectTimeoutMs = config.getConnectTimeoutMs();
        if (config.getControllerPort() > 0)
            controllerPort = config.getControllerPort();
        if (config.getWhitelistPath() != null)
            whitelistPath = config.getWhitelistPath();
        return this;
    }

    private void setArg(String arg, String val) {
        if (allKeys.contains(arg)) {
            if (integerKeys.contains(arg)) {
                try {
                    int intVal = Integer.parseInt(val);
                    if (durationKeys.contains(arg)) {
                        scanDurationMs = intVal;
                    } else if (scanTimeoutKeys.contains(arg)) {
                        scanTimeoutMs = intVal;
                    } else if (connectTimeoutKeys.contains(arg)) {
                        connectTimeoutMs = intVal;
                    } else if (controllerPortKeys.contains(arg)) {
                        controllerPort = intVal;
                    }
                } catch (Exception e) {
                }
            } else if (whitelistPathKeys.contains(arg)) {
                whitelistPath = val;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!Config.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final DaemonConfig other = (DaemonConfig) obj;

        if (scanDurationMs != other.getScanDurationMs()
            || scanTimeoutMs != other.getScanTimeoutMs()
            || connectTimeoutMs != other.getConnectTimeoutMs()
            || controllerPort != other.getControllerPort()) {
            return false;
        }

        if ((whitelistPath == null) ? (other.getWhitelistPath() != null) : !this.whitelistPath.equals(other.getWhitelistPath())) {
            return false;
        }

        return true;

    }

    @Override
    public int getScanDurationMs() {
        return scanDurationMs;
    }

    @Override
    public int getScanTimeoutMs() {
        return scanTimeoutMs;
    }

    @Override
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    @Override
    public int getControllerPort() {
        return controllerPort;
    }

    @Override
    public String getWhitelistPath() {
        return whitelistPath;
    }
}
