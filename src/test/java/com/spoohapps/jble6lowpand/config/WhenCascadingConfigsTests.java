package com.spoohapps.jble6lowpand.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WhenCascadingConfigsTests {

    private int expectedScanTimeoutMs = 1000;
    private int expectedConnectTimeoutMs = 2000;
    private int expectedScanDurationMs = 3000;
    private int expectedControllerPort = 42;
    private String expectedWhitelistPath = "/path";


    private DaemonConfig config;

    @BeforeAll
    public void context() {
        String[] args = new String[] {
                "-d", "1",
                "-t", "2",
                "-c", "3",
                "-p", "34",
                "-w", "4"
        };

        String[] args2 = new String[] {
                "-d", "" + expectedScanDurationMs,
                "-t", "" + expectedScanTimeoutMs,
                "-c", "" + expectedConnectTimeoutMs,
                "-p", "" + expectedControllerPort,
                "-w", expectedWhitelistPath
        };

        config = Config.fromArgs(args).apply(Config.fromArgs(args2));
    }

    @Test
    public void ShouldSetScanDuration() {
        assertEquals(expectedScanDurationMs, config.getScanDurationMs());
    }

    @Test
    public void ShouldSetScanTimeout() {
        assertEquals(expectedScanTimeoutMs, config.getScanTimeoutMs());
    }

    @Test
    public void ShouldSetConnectimeout() {
        assertEquals(expectedConnectTimeoutMs, config.getConnectTimeoutMs());
    }

    @Test
    public void ShouldSetControllerPort() {
        assertEquals(expectedControllerPort, config.getControllerPort());
    }

    @Test
    public void ShouldSetWhitelistPath() {
        assertEquals(expectedWhitelistPath, config.getWhitelistPath());
    }

}
