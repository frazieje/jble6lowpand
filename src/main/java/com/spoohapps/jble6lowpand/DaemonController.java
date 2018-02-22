package com.spoohapps.jble6lowpand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.stream.Collectors;

public class DaemonController {

    private static final Logger logger = LoggerFactory.getLogger(DaemonController.class);

    public static void main(String[] args) {
        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            ScanningDaemonController stub = (ScanningDaemonController) registry.lookup("jble6lowpand");
            System.out.println(stub.getKnownDevices().stream().collect(Collectors.joining(", ")));
        } catch (Exception e) {
            logger.error("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
