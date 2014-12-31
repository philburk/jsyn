/*
 * Copyright 1999 Phil Burk, Mobileer Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsyn.util;

/**
 * Formatted numeric output. Convert integers and floats to strings based on field widths and
 * desired decimal places.
 * 
 * @author Phil Burk (C) 1999 SoftSynth.com
 */

public class NumericOutput {
    static char digitToChar(int digit) {
        if (digit > 9) {
            return (char) ('A' + digit - 10);
        } else {
            return (char) ('0' + digit);
        }
    }

    public static String integerToString(int n, int width, boolean leadingZeros) {
        return integerToString(n, width, leadingZeros, 10);
    }

    public static String integerToString(int n, int width) {
        return integerToString(n, width, false, 10);
    }

    public static String integerToString(int n, int width, boolean leadingZeros, int radix) {
        if (width > 32)
            width = 32;
        StringBuffer buf = new StringBuffer();
        long ln = n;
        boolean ifNeg = false;
        // only do sign if decimal
        if (radix != 10) {
            // System.out.println("MASK before : ln = " + ln );
            ln = ln & 0x00000000FFFFFFFFL;
            // System.out.println("MASK after : ln = " + ln );
        } else if (ln < 0) {
            ifNeg = true;
            ln = -ln;
        }
        if (ln == 0) {
            buf.append('0');
        } else {
            // System.out.println(" ln = " + ln );
            while (ln > 0) {
                int rem = (int) (ln % radix);
                buf.append(digitToChar(rem));
                ln = ln / radix;
            }
        }
        if (leadingZeros) {
            int pl = width;
            if (ifNeg)
                pl -= 1;
            for (int i = buf.length(); i < pl; i++)
                buf.append('0');
        }
        if (ifNeg)
            buf.append('-');
        // leading spaces
        for (int i = buf.length(); i < width; i++)
            buf.append(' ');
        // reverse buffer to put characters in correct order
        buf.reverse();

        return buf.toString();
    }

    /**
     * Convert double to string.
     * 
     * @param width = minimum width of formatted string
     * @param places = number of digits displayed after decimal point
     */
    public static String doubleToString(double value, int width, int places) {
        return doubleToString(value, width, places, false);
    }

    /**
     * Convert double to string.
     * 
     * @param width = minimum width of formatted string
     * @param places = number of digits displayed after decimal point
     */
    public static String doubleToString(double value, int width, int places, boolean leadingZeros) {
        if (width > 32)
            width = 32;
        if (places > 16)
            places = 16;

        boolean ifNeg = false;
        if (value < 0.0) {
            ifNeg = true;
            value = -value;
        }
        // round at relevant decimal place
        value += 0.5 * Math.pow(10.0, 0 - places);
        int ival = (int) Math.floor(value);
        // get portion after decimal point as an integer
        int fval = (int) ((value - Math.floor(value)) * Math.pow(10.0, places));
        String result = "";

        result += integerToString(ival, 0, false, 10);
        result += ".";
        result += integerToString(fval, places, true, 10);

        if (leadingZeros) {
            // prepend leading zeros and {-}
            int zw = width;
            if (ifNeg)
                zw -= 1;
            while (result.length() < zw)
                result = "0" + result;
            if (ifNeg)
                result = "-" + result;
        } else {
            // prepend {-} and leading spaces
            if (ifNeg)
                result = "-" + result;
            while (result.length() < width)
                result = " " + result;
        }
        return result;
    }

    static void testInteger(int n) {
        System.out.println("Test " + n + ", 0x" + Integer.toHexString(n) + ", %"
                + Integer.toBinaryString(n));
        System.out.println("  +,8,t,10 = " + integerToString(n, 8, true, 10));
        System.out.println("  +,8,f,10 = " + integerToString(n, 8, false, 10));
        System.out.println("  -,8,t,10 = " + integerToString(-n, 8, true, 10));
        System.out.println("  -,8,f,10 = " + integerToString(-n, 8, false, 10));
        System.out.println("  +,8,t,16 = " + integerToString(n, 8, true, 16));
        System.out.println("  +,8,f,16 = " + integerToString(n, 8, false, 16));
        System.out.println("  -,8,t,16 = " + integerToString(-n, 8, true, 16));
        System.out.println("  -,8,f,16 = " + integerToString(-n, 8, false, 16));
        System.out.println("  +,8,t, 2 = " + integerToString(n, 8, true, 2));
        System.out.println("  +,8,f, 2 = " + integerToString(n, 8, false, 2));
    }

    static void testDouble(double value) {
        System.out.println("Test " + value);
        System.out.println("  +,5,1 = " + doubleToString(value, 5, 1));
        System.out.println("  -,5,1 = " + doubleToString(-value, 5, 1));

        System.out.println("  +,14,3 = " + doubleToString(value, 14, 3));
        System.out.println("  -,14,3 = " + doubleToString(-value, 14, 3));

        System.out.println("  +,6,2,true = " + doubleToString(value, 6, 2, true));
        System.out.println("  -,6,2,true = " + doubleToString(-value, 6, 2, true));
    }

    public static void main(String argv[]) {
        System.out.println("Test NumericOutput");
        testInteger(0);
        testInteger(1);
        testInteger(16);
        testInteger(23456);
        testInteger(0x23456);
        testInteger(0x89ABC);
        testDouble(0.0);
        testDouble(0.0678);
        testDouble(0.1234567);
        testDouble(1.234567);
        testDouble(12.34567);
        testDouble(123.4567);
        testDouble(1234.5678);

    }
}
