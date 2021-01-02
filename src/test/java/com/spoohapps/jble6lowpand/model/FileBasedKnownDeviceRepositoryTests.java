package com.spoohapps.jble6lowpand.model;

import com.spoohapps.farcommon.model.MACAddress;
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
import java.util.stream.Stream;

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

    private Set<MACAddress> getFileContents() {
        try (Stream<String> fileLines = Files.lines(filePath)) {
            return fileLines.map(MACAddress::new).collect(Collectors.toCollection(HashSet::new));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return new HashSet<>();
    }

    private void writeFileContents(Set<MACAddress> addresses) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath, Charset.defaultCharset())) {
            String data = addresses.stream().map(MACAddress::toString).collect(Collectors.joining(System.getProperty("line.separator")));
            bw.write(data);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void shouldSaveAddedAddressesToFile() {
        MACAddress address = new MACAddress("00:AA:BB:CC:DD:FF");
        knownDevices.add(address);
        assertTrue(getFileContents().contains(address));
    }

    @Test
    public void shouldSaveAddedEUI64AddressesToFile() {
        MACAddress address = new MACAddress("00:11:22:AA:BB:CC:DD:FF");
        knownDevices.add(address);
        assertTrue(getFileContents().contains(address));
    }

    @Test
    public void shouldSaveAddedAddressesWithNamesToFile() {
        MACAddress address = new MACAddress("00:AA:BB:CC:DD:FF testName");
        knownDevices.add(address);
        assertTrue(getFileContents().contains(address));
    }

    @Test
    public void shouldSaveAddedEUI64AddressesWithNamesToFile() {
        MACAddress address = new MACAddress("00:11:22:AA:BB:CC:DD:FF testName");
        knownDevices.add(address);
        assertTrue(getFileContents().contains(address));
    }

    @Test
    public void shouldRemoveRemovedAddressesFromFile() {
        MACAddress address = new MACAddress("00:AA:BB:CC:DD:FF");
        knownDevices.add(address);
        knownDevices.remove(address);
        assertFalse(getFileContents().contains(address));
    }

    @Test
    public void shouldRemoveRemovedEUI64AddressesFromFile() {
        MACAddress address = new MACAddress("00:11:22:AA:BB:CC:DD:FF");
        knownDevices.add(address);
        knownDevices.remove(address);
        assertFalse(getFileContents().contains(address));
    }

    @Test
    public void shouldNotAddExistingDevice() {
        MACAddress address = new MACAddress("00:AA:BB:CC:DD:FF TEST");
        knownDevices.add(address);
        MACAddress address2 = new MACAddress("00:AA:BB:CC:DD:FF testName");
        knownDevices.add(address2);
        assertFalse(getFileContents().stream().anyMatch(x -> x.getName().equals("testName")));
    }

    @Test
    public void shouldNotAddExistingEUI64Device() {
        MACAddress address = new MACAddress("00:11:22:AA:BB:CC:DD:FF TEST");
        knownDevices.add(address);
        MACAddress address2 = new MACAddress("00:11:22:AA:BB:CC:DD:FF testName");
        knownDevices.add(address2);
        assertFalse(getFileContents().stream().anyMatch(x -> x.getName().equals("testName")));
    }

    @Test
    public void shouldUpdateExistingDevice() {
        MACAddress address = new MACAddress("00:AA:BB:CC:DD:FF TEST");
        knownDevices.add(address);
        MACAddress address2 = new MACAddress("00:AA:BB:CC:DD:FF testName");
        knownDevices.update(address2);
        assertEquals(1, getFileContents().size());
        assertTrue(getFileContents().stream().anyMatch(x -> x.getName().equals("testName")));
    }

    @Test
    public void shouldUpdateExistingEUI64Device() {
        MACAddress address = new MACAddress("00:11:22:AA:BB:CC:DD:FF TEST");
        knownDevices.add(address);
        MACAddress address2 = new MACAddress("00:11:22:AA:BB:CC:DD:FF testName");
        knownDevices.update(address2);
        assertEquals(1, getFileContents().size());
        assertTrue(getFileContents().stream().anyMatch(x -> x.getName().equals("testName")));
    }

    @Test
    public void shouldWatchForFileChanges() {
        sleep(1000);
        MACAddress address = new MACAddress("00:AA:BB:CC:DD:FF");
        Set<MACAddress> addAddresses = new HashSet<>();
        addAddresses.add(address);
        writeFileContents(addAddresses);
        sleep(10000);
        assertTrue(knownDevices.contains(address));
    }
}
