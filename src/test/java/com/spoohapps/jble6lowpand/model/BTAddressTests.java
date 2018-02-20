package com.spoohapps.jble6lowpand.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BTAddressTests {

    @Test
    public void shouldConstructFromValidAddressString() {

        String addressString = "78:4f:43:62:6f:58";

        BTAddress address = new BTAddress(addressString);

        assertTrue(address.toString().toLowerCase().equals(addressString));

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

        assertTrue(address.toString().toLowerCase().equals(addressString));

    }

}
