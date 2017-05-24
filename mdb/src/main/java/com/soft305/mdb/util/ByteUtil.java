package com.soft305.mdb.util;

import java.util.Arrays;

/**
 * Created by pablo on 5/14/17.
 */
public class ByteUtil {

    public static boolean compare(byte[] arrayLeft, byte[] arrayRight) {
        return Arrays.equals(arrayLeft, arrayRight);
    }

    public static boolean compare(byte[] arrayLeft, byte[] arrayRight, int len) {
        if (arrayLeft==arrayRight)
            return true;

        if (arrayLeft==null || arrayRight==null)
            return false;

        if (arrayLeft.length < len || arrayRight.length < len)
            return false;

        for (int i=0; i<len; i++)
            if (arrayLeft[i] != arrayRight[i])
                return false;

        return true;
    }

    public static boolean areEqual(byte b1, byte b2) {
        return Byte.compare(b1, b2) == 0;
    }

    /**
     *  The given array must have reserved the last byte for the check sum
     * */
    public static byte[] mdbChkSum(byte[] data) {
        int sum = 0;
        for (int i=1; i<data.length-1; i+=2) {
            sum += data[i];
        }

        data[data.length - 1] = (byte) sum;

        return data;
    }

}
