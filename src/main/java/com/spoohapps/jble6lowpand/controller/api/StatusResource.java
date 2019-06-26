package com.spoohapps.jble6lowpand.controller.api;

import com.spoohapps.jble6lowpand.controller.Ble6LowpanController;
import com.spoohapps.farcommon.model.EUI48Address;
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

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("{id}")
    public Response putKnownAddress(@PathParam("id") String id, EUI48Address address)
    {
        EUI48Address newAddress;

        try {
            newAddress = new EUI48Address(id);
        } catch (IllegalArgumentException e) {
            return Response.status(400, e.getMessage()).build();
        }

        newAddress.setName(address.getName());

        if (controller.addKnownDevice(newAddress)) {
            return Response.noContent().build();
        } else if (controller.updateKnownDevice(newAddress)) {
            return Response.noContent().build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteKnownAddress(@PathParam("id") String id)
    {
        EUI48Address address;

        try {
            address = new EUI48Address(id);
        } catch (IllegalArgumentException e) {
            return Response.status(400, e.getMessage()).build();
        }

        if (controller.removeKnownDevice(address))
            return Response.noContent().build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("known")
    public Response getKnownAddresses() {
        return Response.ok().entity(controller.getKnownDevices()).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("available")
    public Response getAvailableAddresses() {
        return Response.ok().entity(controller.getAvailableDevices()).build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("connected")
    public Response  getConnectedDevices() {
        return Response.ok().entity(controller.getConnectedDevices()).build();
    }

}
