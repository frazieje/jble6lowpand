package com.spoohapps.jble6lowpand;
import com.spoohapps.jble6lowpand.config.DaemonConfig;
import com.spoohapps.jble6lowpand.model.BTAddress;
import com.spoohapps.jble6lowpand.model.KnownDeviceRepository;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ScanningDaemonTests {

    private ScanningDaemon daemon;
    private KnownDeviceRepository knownDevices;
    private FakeBle6LowpanIpspService ipspService;

    @BeforeAll
    public void context() {
        knownDevices = new InMemoryKnownDeviceRepository();
        ipspService = new FakeBle6LowpanIpspService();
        daemon = new ScanningDaemon(knownDevices, ipspService, new TestDaemonConfig());
        try {
            daemon.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void cleanup() {
        knownDevices.clear();
        ipspService.clearSeedAddresses();
    }

    @AfterAll
    public void teardown() {
        daemon.destroy();
    }

    public void sleep(int timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldConnectToKnownDevice() {
        BTAddress knownAddress = new BTAddress("35:80:41:4B:D0:E2");
        knownDevices.add(knownAddress);
        ipspService.addSeedAddress(knownAddress);
        sleep(500);
        assertTrue(daemon.getConnectedDevices().contains(knownAddress.toString()));
    }

    @Test
    public void shouldScanForDevices() {
        sleep(500);
        assertTrue(daemon.getAvailableDevices().size() > 0);
    }

    class TestDaemonConfig implements DaemonConfig {

        @Override
        public int getScanDurationMs() {
            return 1;
        }

        @Override
        public int getScanTimeoutMs() {
            return 300;
        }

        @Override
        public int getConnectTimeoutMs() {
            return 100;
        }

        @Override
        public String getWhitelistPath() {
            return "some path";
        }
    }

}
