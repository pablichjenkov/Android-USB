package com.soft305.mdb.util;

/**
 * Created by pablo on 5/6/17.
 */
public class StringUtil {

    private StringUtil() {}

    public static String fromByteArrayToHexString(final byte[] bytes) {

        StringBuilder sb = new StringBuilder();

        for (byte value : bytes) {
            int valueUnsignedExtended = value & 0xFF;
            String hexPrefix = (valueUnsignedExtended < 16) ? "0x0" : "0x";
            String hexValue = Integer.toHexString(valueUnsignedExtended);
            sb.append(hexPrefix.concat(hexValue).concat(" "));
        }

        return sb.toString();
    }

}
