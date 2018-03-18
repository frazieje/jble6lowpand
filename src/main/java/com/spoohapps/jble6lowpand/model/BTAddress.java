package com.spoohapps.jble6lowpand.model;


import com.spoohapps.jble6lowpand.util.BTAddressValidator;
import com.spoohapps.jble6lowpand.util.BTAddressValidatorImpl;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class BTAddress {

    private BTAddressValidator btAddressValidator = new BTAddressValidatorImpl();

    public BTAddress(String address) {

        String addressStr = address.trim();
        String nameStr = null;

        if (address.contains(" ")) {
            int index = address.indexOf(" ");
            addressStr = address.substring(0, index);
            nameStr = address.substring(index + 1);
        }

        if (!btAddressValidator.validate(addressStr))
            throw new IllegalArgumentException("Not a valid Bluetooth address.");

        data = hexStringToByteArray(addressStr);
        name = nameStr;
    }

    public BTAddress(byte[] data) {
        this.data = data;
    }

    public BTAddress(byte[] data, String name) {
        this(data);
        this.name = name;
    }

    public BTAddress(String address, String name) {
        this(address + " " + name);
    }

    public byte[] getData() {
        return data;
    }

    private byte[] data;

    private String name;

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

    public static BTAddress random() {
        Random random = new Random();
        String[] chars = new String[] { "A", "B", "C", "D", "E", "F" };

        String address = "";

        for (int i = 1; i <= 12; i++) {
            int num = Math.abs(random.nextInt()) % 16;
            if (num > 9) {
                address += chars[num-10];
            } else {
                address += num;
            }
            if (i % 2 == 0 && i != 12) {
                address += ":";
            }
        }

        return new BTAddress(address);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressString() {
        return byteArrayToHexString(data);
    }

    @Override
    public String toString() {
        String displayName = name != null && !name.equals("") ? name : "";
        return byteArrayToHexString(data) + (!displayName.isEmpty() ? (" " + displayName) : "");
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
