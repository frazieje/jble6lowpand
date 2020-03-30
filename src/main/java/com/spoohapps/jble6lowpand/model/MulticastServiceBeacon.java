package com.spoohapps.jble6lowpand.model;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.WriteResult;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.nio.transport.UDPNIOConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;
import org.glassfish.grizzly.utils.Futures;
import org.glassfish.grizzly.utils.StringFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MulticastServiceBeacon implements ServiceBeacon {

    private final String groupAddress;
    private final int port;

    public MulticastServiceBeacon(String groupAddress, int port) {
        this.groupAddress = groupAddress;
        this.port = port;
    }

    @Override
    public void broadcast(String message) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        final FilterChain filterChain = FilterChainBuilder.stateless()
                .add(new TransportFilter())
                .add(new StringFilter(StandardCharsets.UTF_8))
                .build();

        // Create UDP transport
        final UDPNIOTransport transport =
                UDPNIOTransportBuilder.newInstance()
                        .setProcessor(filterChain)
                        .build();

        UDPNIOConnection connection = null;

        try {
            // start the transport
            transport.start();

            // Create non-connected UDP connection and bind it to the local PORT
            final Future<Connection> connectFuture = transport.connect(
                    null, new InetSocketAddress(port));

            connection = (UDPNIOConnection) connectFuture.get(5, TimeUnit.SECONDS);

            InetAddress groupAddr = InetAddress.getByName(groupAddress);

            final InetSocketAddress peerAddr = new InetSocketAddress(groupAddr, port);

            // Create Future to be able to block until the message is sent
            final FutureImpl<WriteResult<String, SocketAddress>> writeFuture =
                    Futures.createSafeFuture();

            // Send the message
            connection.write(peerAddr, message,
                    Futures.toCompletionHandler(writeFuture));

            // Block until the message is sent
            writeFuture.get(5, TimeUnit.SECONDS);

        } finally {
            // Close connection is it's not null
            if (connection != null) {
                connection.close();
            }

            // stop the transport
            transport.shutdownNow();

        }
    }
}
