package com.spoohapps.jble6lowpand.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BTAddressValidatorTests {

    private final BTAddressValidator validator;

    public BTAddressValidatorTests() {
        validator = new BTAddressValidatorImpl();
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
