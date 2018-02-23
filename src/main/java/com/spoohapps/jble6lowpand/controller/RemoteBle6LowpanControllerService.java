package com.spoohapps.jble6lowpand.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteBle6LowpanControllerService implements Ble6LowpanControllerService{

    private final Ble6LowpanController controller;

    private Registry rmiRegistry;
    private final int port;

    public static final String ControllerName = "jble6lowpand";

    private final Logger logger = LoggerFactory.getLogger(RemoteBle6LowpanControllerService.class);

    public RemoteBle6LowpanControllerService(Ble6LowpanController controller, int port) {
        this.controller = controller;
        this.port = port;
    }

    public void start() {
        try {
            rmiRegistry = LocateRegistry.createRegistry(port);
            rmiRegistry.bind(ControllerName, UnicastRemoteObject.exportObject(controller, 0));
            logger.info("RMI Server ready");
        } catch (Exception e) {
            logger.error("RMI Server exception: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            rmiRegistry.unbind(ControllerName);
            UnicastRemoteObject.unexportObject(controller, true);
        } catch (Exception ex) {
            logger.error("RMI server exception: " + ex.getMessage());
        }
    }
}
