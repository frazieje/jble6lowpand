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
    @Path("{id}")
    public Response postKnownAddress(@PathParam("id") String id, BTAddress address)
    {
        BTAddress newAddress;

        try {
            newAddress = new BTAddress(id);
        } catch (IllegalArgumentException e) {
            return Response.status(400, e.getMessage()).build();
        }

        if (!newAddress.equals(address)) {
            return Response.status(400, "Payload address does not match resource target").build();
        }

        newAddress.setName(address.getName());

        controller.addKnownDevice(newAddress);

        return Response.accepted().build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteKnownAddress(@PathParam("id") String id)
    {
        BTAddress address;

        try {
            address = new BTAddress(id);
        } catch (IllegalArgumentException e) {
            return Response.status(400, e.getMessage()).build();
        }

        controller.removeKnownDevice(address);

        return Response.accepted().build();
    }

}
