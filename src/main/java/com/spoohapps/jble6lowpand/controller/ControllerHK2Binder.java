package com.spoohapps.jble6lowpand.controller;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

public class ControllerHK2Binder extends AbstractBinder {

    private Controller controller;

    public ControllerHK2Binder(Controller controller) {
        this.controller = controller;
    }

    @Override
    protected void configure() {
        bindFactory(new Factory<Controller>() {
            @Override
            public Controller provide() {
                return controller;
            }

            @Override
            public void dispose(Controller instance) {
                //ignore
            }
        }).to(Controller.class).in(Singleton.class);
    }
}
