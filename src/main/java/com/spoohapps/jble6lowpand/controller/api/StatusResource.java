package com.spoohapps.jble6lowpand.controller.api;

import com.spoohapps.jble6lowpand.controller.Ble6LowpanController;
import com.spoohapps.jble6lowpand.model.BTAddress;
import com.spoohapps.jble6lowpand.model.Status;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
                controller.getConnectedDevices(),
                controller.getKnownDevices());
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("known")
    public Response postKnownAddress(BTAddress address)
    {
        controller.addKnownDevice(address);
        return Response.accepted().build();
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("known")
    public Response deleteKnownAddress(BTAddress address)
    {
        controller.removeKnownDevice(address);
        return Response.accepted().build();
    }

}
