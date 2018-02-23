package com.spoohapps.jble6lowpand.controller;

import com.spoohapps.jble6lowpand.Ble6LowpanController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.stream.Collectors;

import static com.spoohapps.jble6lowpand.ScanningDaemon.ControllerName;

public class ScanningDaemonController {

    private static final Logger logger = LoggerFactory.getLogger(ScanningDaemonController.class);

    public static void main(String[] args) {
        String command = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(null);
            Ble6LowpanController stub = (Ble6LowpanController) registry.lookup(ControllerName);

            if (command != null) {
                if (command.toLowerCase().equals("-list"))
                    System.out.println(stub.getConnectedDevices().stream().collect(Collectors.joining(", ")));
                else if (command.toLowerCase().equals("-known"))
                    System.out.println(stub.getKnownDevices().stream().collect(Collectors.joining(", ")));
            }
        } catch (Exception e) {
            logger.error("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
