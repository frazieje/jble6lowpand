package com.spoohapps.jble6lowpand.util;

import com.spoohapps.farcommon.util.EUI48AddressValidator;
import com.spoohapps.farcommon.util.EUI48AddressValidatorImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EUI48AddressValidatorTests {

    private final EUI48AddressValidator validator;

    public EUI48AddressValidatorTests() {
        validator = new EUI48AddressValidatorImpl();
    }

    @Test
    public void validatesGoodAddressWithHyphens() {
        assertTrue(validator.validate("78-4f-43-62-6f-58"));
    }

    @Test
    public void validatesGoodAddressWithColons() {
        assertTrue(validator.validate("78:4f:43:62:6f:58"));
    }

    @Test
    public void doesNotValidateBadAddressWithColons() {
        assertFalse(validator.validate("78:4f:43:62:6z:58"));
    }

    @Test
    public void doesNotValidateBadAddressWithHyphens() {
        assertFalse(validator.validate("78-4f-43-62-6z-58"));
    }

}
