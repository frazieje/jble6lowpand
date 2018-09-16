package com.spoohapps.jble6lowpand.controller;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainerProvider;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

public class RemoteBle6LowpanControllerBroadcaster implements Ble6LowpanControllerBroadcaster {

    private final HttpServer httpServer;

    private final Logger logger = LoggerFactory.getLogger(RemoteBle6LowpanControllerBroadcaster.class);

    public RemoteBle6LowpanControllerBroadcaster(Ble6LowpanController controller, int port) {

        URI baseUri = UriBuilder.fromPath("/").host("0.0.0.0").port(port).build();

        ResourceConfig config =
                new ResourceConfig()
                        .register(new ControllerHK2Binder(controller))
                        .packages("com.spoohapps.jble6lowpand.controller.api");

        GrizzlyHttpContainer httpContainer =
                new GrizzlyHttpContainerProvider().createContainer(GrizzlyHttpContainer.class, config);

        httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, httpContainer, false, null, false);
    }

    public void start() {
        logger.info("Starting HTTP Server...");
        try {
            httpServer.start();
            logger.info("HTTP Server Started...");
        } catch (IOException e) {
            logger.error("Error starting HTTP Server", e);
        }
    }

    public void stop() {
        logger.info("Stopping HTTP Server...");
        httpServer.shutdownNow();
        logger.info("HTTP Server Stopped.");
    }
}
