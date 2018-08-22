package com.spoohapps.jble6lowpand.controller.api;

import com.spoohapps.jble6lowpand.controller.Ble6LowpanController;
import com.spoohapps.jble6lowpand.model.Status;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class StatusResource {

    private final Ble6LowpanController controller;

    @Inject
    public StatusResource(Ble6LowpanController controller) {
        this.controller = controller;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Status getStatus() {
        return new Status(
                controller.getConfig(),
                controller.getAvailableDevices(),
                controller.getConnectedDevices(), controller.getKnownDevices());
    }

}
