package com.spoohapps.jble6lowpand.model;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ServiceBeacon {
    void broadcast(String message) throws IOException, InterruptedException, ExecutionException, TimeoutException;
}
