package com.spoohapps.jble6lowpand.model;


import com.spoohapps.jble6lowpand.util.BTAddressValidator;
import com.spoohapps.jble6lowpand.util.BTAddressValidatorImpl;

import java.util.Arrays;
import java.util.Objects;

public class BTAddress {

    private BTAddressValidator btAddressValidator = new BTAddressValidatorImpl();

    public BTAddress(String address) {

        if (!btAddressValidator.validate(address))
            throw new IllegalArgumentException("Not a valid Bluetooth address.");

        data = hexStringToByteArray(address);
    }

    public BTAddress(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    private byte[] data;

    private static byte[] hexStringToByteArray(String hex) {
        String s = hex.replaceAll("[:-]", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3 - 1];
        for ( int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if (j != bytes.length-1)
                hexChars[j * 3 + 2] = ':';
        }
        return new String(hexChars);
    }

    @Override
    public String toString() {
        return byteArrayToHexString(data);
    }

    @Override
    public boolean equals(Object other) {
        BTAddress otherAddress = (BTAddress)other;
        if (other == null)
            return false;
        return Arrays.equals(getData(), otherAddress.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }
}
