package com.spoohapps.jble6lowpand.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WhenLoadingConfigFromMalformedArgsTests {

    @Test
    public void ShouldIgnoreUnintelligibleValues() {
        int expectedDuration = 1000;
        String[] args = new String[] {"-lkdsa", "" + expectedDuration};
        DaemonConfig config = Config.fromArgs(args);
        assertEquals(Config.empty(), config);
    }

    @Test
    public void ShouldIgnoreRandomUnintelligibleValues() {
        int expectedDuration = 1000;
        String[] args = new String[] {"lkdsa", "" + expectedDuration};
        DaemonConfig config = Config.fromArgs(args);
        assertEquals(Config.empty(), config);
    }

    @Test
    public void ShouldIgnoreUnintelligibleValuesWhenValidValuesPresent() {
        int expectedDuration = 1000;
        String[] args = new String[] {"lkdsa", "" + 10, "-d", "" + expectedDuration};
        DaemonConfig config = Config.fromArgs(args);
        assertEquals(expectedDuration, config.getScanDurationMs());
    }

}
