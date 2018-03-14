package com.spoohapps.jble6lowpand.model;

import org.junit.jupiter.api.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileBasedKnownDeviceRepositoryTests {

    private FileBasedKnownDeviceRepository knownDevices;

    private Path filePath = Paths.get(System.getProperty("user.home"), "whitelist.conf");

    @BeforeAll
    public void context() {
        knownDevices = new FileBasedKnownDeviceRepository(filePath);
        knownDevices.startWatcher();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void cleanup() {
        knownDevices.clear();
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

    private void sleep(int timeMs) {
        try {
            Thread.sleep(timeMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Set<BTAddress> getFileContents() {
        try {
            return Files.lines(filePath).map(BTAddress::new).collect(Collectors.toCollection(HashSet::new));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new HashSet<>();
    }

    private void writeFileContents(Set<BTAddress> addresses) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath, Charset.defaultCharset())) {
            String data = addresses.stream().map(BTAddress::toString).collect(Collectors.joining(System.getProperty("line.separator")));
            bw.write(data);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void ShouldSaveAddedAddressesToFile() {
        BTAddress address = new BTAddress("00:AA:BB:CC:DD:FF");
        knownDevices.add(address);
        assertTrue(getFileContents().contains(address));
    }

    @Test
    public void ShouldSaveAddedAddressesWithNamesToFile() {
        BTAddress address = new BTAddress("00:AA:BB:CC:DD:FF testName");
        knownDevices.add(address);
        assertTrue(getFileContents().contains(address));
    }

    @Test
    public void ShouldRemoveRemovedAddressesFromFile() {
        BTAddress address = new BTAddress("00:AA:BB:CC:DD:FF");
        knownDevices.add(address);
        knownDevices.remove(address);
        assertFalse(getFileContents().contains(address));
    }

    @Test
    public void ShouldWatchForFileChanges() {
        sleep(1000);
        BTAddress address = new BTAddress("00:AA:BB:CC:DD:FF");
        Set<BTAddress> addAddresses = new HashSet<>();
        addAddresses.add(address);
        writeFileContents(addAddresses);
        sleep(10000);
        assertTrue(knownDevices.contains(address));
    }
}
