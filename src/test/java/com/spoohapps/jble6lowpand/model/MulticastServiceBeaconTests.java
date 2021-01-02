package com.spoohapps.jble6lowpand.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spoohapps.farcommon.model.ServiceBeaconMessage;
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

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MulticastServiceBeaconTests {

    private static final String expectedBeaconAddress = "224.0.0.148";
    private static final int expectedBeaconPort = 9889;

    private static final int expectedApiPort = 15234;
    private static final int expectedAuthApiPort = 10001;
    private static final String expectedReplicationRemoteHost = "someRemoteHost";
    private static final int expectedReplicationRemotePort = 12837;

    private final StringCapturingFilter capturingFilter = new StringCapturingFilter();

    private UDPNIOTransport transport;
    private UDPNIOConnection connection;

    private ObjectMapper objectMapper = new ObjectMapper();
    private ServiceBeaconMessage actualServiceBeaconMessage;
    public static final String expectedServiceName = "expectedServiceName";
    public static final String expectedProfileId = "h23n4hfg";

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

        ServiceBeaconMessage toBroadcast = new ServiceBeaconMessage();

        toBroadcast.setServiceName(expectedServiceName);
        toBroadcast.setProfileId(expectedProfileId);
        toBroadcast.setApiPort(expectedApiPort);
        toBroadcast.setAuthApiPort(expectedAuthApiPort);
        toBroadcast.setReplicationRemoteHost(expectedReplicationRemoteHost);
        toBroadcast.setReplicationRemotePort(expectedReplicationRemotePort);

        serviceBeacon.broadcast(objectMapper.writeValueAsString(toBroadcast));

        Thread.sleep(500);

        actualServiceBeaconMessage = objectMapper.readValue(capturingFilter.getReadQueue().get(0), ServiceBeaconMessage.class);

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

    @Test
    public void shouldBroadcastServiceName() {
        assertEquals(expectedServiceName, actualServiceBeaconMessage.getServiceName());
    }

    @Test
    public void shouldBroadcastProfileId() {
        assertEquals(expectedProfileId, actualServiceBeaconMessage.getProfileId());
    }

    @Test
    public void shouldBroadcastApiPort() {
        assertEquals(expectedApiPort, actualServiceBeaconMessage.getApiPort());
    }

    @Test
    public void shouldBroadcastAuthApiPort() {
        assertEquals(expectedAuthApiPort, actualServiceBeaconMessage.getAuthApiPort());
    }

    @Test
    public void shouldBroadcastReplicationRemoteHost() {
        assertEquals(expectedReplicationRemoteHost, actualServiceBeaconMessage.getReplicationRemoteHost());
    }

    @Test
    public void shouldBroadcastReplicationRemotePort() {
        assertEquals(expectedReplicationRemotePort, actualServiceBeaconMessage.getReplicationRemotePort());
    }

}