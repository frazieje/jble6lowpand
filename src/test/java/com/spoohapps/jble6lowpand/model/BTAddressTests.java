package com.spoohapps.jble6lowpand.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BTAddressTests {

    @Test
    public void shouldConstructFromValidAddressString() {

        String addressString = "78:4f:43:62:6f:58";

        BTAddress address = new BTAddress(addressString);

        assertEquals(address.toString().toLowerCase(), addressString);

    }

    @Test
    public void shouldConstructFromHyphenatedValidAddressString() {

        String addressString = "78-4f-43-62-6f-58";

        BTAddress address = new BTAddress(addressString);

        assertNotNull(address);
    }

    @Test
    public void shouldConstructFromCollapsedAddressString() {

        String addressString = "784f43626f58";

        BTAddress address = new BTAddress(addressString);

        assertEquals("78:4F:43:62:6F:58", address.toString());
    }

    @Test
    public void shouldNotConstructFromInvalidAddressString() {

        String addressString = "78:4f:43:62:6fz58";

        assertThrows(IllegalArgumentException.class, () -> {
            BTAddress address = new BTAddress(addressString);
        });

    }

    @Test
    public void shouldConstructFromBytes() {

        String addressString = "78:4f:43:62:6f:58";

        BTAddress address = new BTAddress(new byte[] { 120,79,67,98,111,88 });

        assertEquals(address.toString().toLowerCase(), addressString);

    }

    @Test
    public void shouldConstructWithName() {

        String addressString = "78:4f:43:62:6f:58";

        String expectedName = "testName";
        BTAddress address = new BTAddress(new byte[] { 120,79,67,98,111,88 }, expectedName);

        assertTrue(
                address.toString().toLowerCase().equals(addressString + " " + expectedName.toLowerCase())
                        && address.getName().equals(expectedName)
        );

    }

    @Test
    public void shouldConstructWithNameInString() {

        String expectedName = "testName";
        String addressString = "78:4f:43:62:6f:58";

        BTAddress address = new BTAddress(addressString + " " + expectedName);

        assertTrue(
                address.toString().toLowerCase().equals(addressString + " " + expectedName.toLowerCase())
                        && address.getName().equals(expectedName)
        );

    }

}
