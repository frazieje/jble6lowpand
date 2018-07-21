package com.spoohapps.jble6lowpand.controller;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class ControllerHK2Binder extends AbstractBinder {

    private Ble6LowpanController controller;

    public ControllerHK2Binder(Ble6LowpanController controller) {
        this.controller = controller;
    }

    @Override
    protected void configure() {
        bindFactory(new Factory<Ble6LowpanController>() {
            @Override
            public Ble6LowpanController provide() {
                return controller;
            }

            @Override
            public void dispose(Ble6LowpanController instance) {
                //ignore
            }
        }).to(Ble6LowpanController.class).in(Singleton.class);
    }
}
