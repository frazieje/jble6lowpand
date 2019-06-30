package com.spoohapps.jble6lowpand.model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WhenStartingFileBasedKnownDeviceManagerWithNoFileTests {

    private FileBasedKnownDeviceRepository knownDevices;

    private Path filePath = Paths.get(System.getProperty("user.home"), "whitelist.conf");

    @BeforeAll
    public void setup() {

        try {
            Files.delete(filePath);
        } catch (Exception ignored) {}

        knownDevices = new FileBasedKnownDeviceRepository(filePath, new ArrayList<>());
        knownDevices.startWatcher();
    }

    @AfterAll
    public void teardown() {
        try {
            knownDevices.stopWatcher();
            Files.delete(filePath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void shouldCreateFileIfNotExists() {
        assertTrue(Files.exists(filePath));
    }

}
