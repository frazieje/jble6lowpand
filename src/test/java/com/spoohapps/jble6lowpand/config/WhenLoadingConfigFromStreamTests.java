package com.spoohapps.jble6lowpand.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WhenLoadingConfigFromStreamTests {

    private DaemonConfig config;

    @BeforeAll
    public void context() {

        List<String> lines = Arrays.asList(
                "scanDuration=1000",
                "connectTimeout=500",
                "t=750",
                "whitelistPath=/etc/jble6lowpand"
        );

        String data = lines.stream().map(s -> s + System.lineSeparator()).collect(Collectors.joining());

        InputStream configStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        config = Config.fromStream(configStream);
    }

    @Test
    public void ShouldSetScanDuration() {
        assertTrue(config.getScanDurationMs() > 0);
    }

    @Test
    public void ShouldSetScanTimeout() {
        assertTrue(config.getScanTimeoutMs() > 0);
    }

    @Test
    public void ShouldSetConnectimeout() {
        assertTrue(config.getConnectTimeoutMs() > 0);
    }

    @Test
    public void ShouldSetWhitelistPath() {
        assertNotNull(config.getWhitelistPath());
    }

}
