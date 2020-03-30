package com.spoohapps.jble6lowpand.model;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.UDPNIOConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;
import org.glassfish.grizzly.utils.StringFilter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MulticastServiceBeaconTests {

    private static final String expectedBeaconAddress = "224.0.0.147";
    private static final int expectedBeaconPort = 9889;

    private final StringCapturingFilter capturingFilter = new StringCapturingFilter();

    private UDPNIOTransport transport;
    private UDPNIOConnection connection;

    @BeforeAll
    public void setup() throws IOException, InterruptedException, ExecutionException, TimeoutException {

        final FilterChain filterChain = FilterChainBuilder.stateless()
                .add(new TransportFilter())
                .add(new StringFilter(StandardCharsets.UTF_8))
                .add(capturingFilter)
                .build();

        // Create UDP transport
        transport = UDPNIOTransportBuilder.newInstance()
                .setProcessor(filterChain)
                .build();

        transport.start();

        final Future<Connection> connectFuture = transport.connect(
                null, new InetSocketAddress(expectedBeaconPort));

        connection = (UDPNIOConnection) connectFuture.get(10, TimeUnit.SECONDS);

        InetAddress groupAddr = InetAddress.getByName(expectedBeaconAddress);

        final Enumeration<NetworkInterface> niEnumeration =
                NetworkInterface.getNetworkInterfaces();

        while (niEnumeration.hasMoreElements()) {
            final NetworkInterface ni = niEnumeration.nextElement();
            if (ni.supportsMulticast()) {
                connection.join(groupAddr, ni, null);
            }
        }

        ServiceBeacon serviceBeacon = new MulticastServiceBeacon(expectedBeaconAddress, expectedBeaconPort);
        serviceBeacon.broadcast("{ \"some\":\"json\", \"super\":\"easy\" }");
        Thread.sleep(50000);

    }

    @AfterAll
    public void tearDown() throws IOException {
        if (connection != null) {
            connection.close();
        }
        transport.shutdownNow();
    }

    @Test
    public void shouldBroadcast() {
        assertTrue(capturingFilter.getReadQueue().size() > 0);
    }

}