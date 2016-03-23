/*
 * Copyright 2011 Phil Burk, Mobileer Inc
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

package com.softsynth.math;

import java.util.ArrayList;

/**
 * Tool for factoring primes and prime ratios. This class contains a static array of primes
 * generated using the Sieve of Eratosthenes.
 *
 * @author Phil Burk (C) 2011 Mobileer Inc
 */
public class PrimeFactors {
    private static final int SIEVE_SIZE = 1000;
    private static int[] primes;
    private final int[] factors;

    static {
        // Use Sieve of Eratosthenes to fill Prime table
        boolean[] sieve = new boolean[SIEVE_SIZE];
        ArrayList<Integer> primeList = new ArrayList<Integer>();
        int i = 2;
        while (i < (SIEVE_SIZE / 2)) {
            if (!sieve[i]) {
                primeList.add(i);
                int multiple = 2 * i;
                while (multiple < SIEVE_SIZE) {
                    sieve[multiple] = true;
                    multiple += i;
                }
            }
            i += 1;
        }
        primes = primeListToArray(primeList);
    }

    private static int[] primeListToArray(ArrayList<Integer> primeList) {
        int[] primes = new int[primeList.size()];
        for (int i = 0; i < primes.length; i++) {
            primes[i] = primeList.get(i);
        }
        return primes;
    }

    public PrimeFactors(int[] factors) {
        this.factors = factors;
    }

    public PrimeFactors(int numerator, int denominator) {
        int[] topFactors = factor(numerator);
        int[] bottomFactors = factor(denominator);
        factors = subtract(topFactors, bottomFactors);
    }

    public PrimeFactors subtract(PrimeFactors pf) {
        return new PrimeFactors(subtract(factors, pf.factors));
    }

    public PrimeFactors add(PrimeFactors pf) {
        return new PrimeFactors(add(factors, pf.factors));
    }

    public static int[] subtract(int[] factorsA, int[] factorsB) {
        int max;
        int min;
        if (factorsA.length > factorsB.length) {
            max = factorsA.length;
            min = factorsB.length;
        } else {

            min = factorsA.length;
            max = factorsB.length;
        }
        ArrayList<Integer> primeList = new ArrayList<Integer>();
        int i;
        for (i = 0; i < min; i++) {
            primeList.add(factorsA[i] - factorsB[i]);
        }
        if (factorsA.length > factorsB.length) {
            for (; i < max; i++) {
                primeList.add(factorsA[i]);
            }
        } else {
            for (; i < max; i++) {
                primeList.add(0 - factorsB[i]);
            }
        }
        trimPrimeList(primeList);
        return primeListToArray(primeList);
    }

    public static int[] add(int[] factorsA, int[] factorsB) {
        int max;
        int min;
        if (factorsA.length > factorsB.length) {
            max = factorsA.length;
            min = factorsB.length;
        } else {
            min = factorsA.length;
            max = factorsB.length;
        }
        ArrayList<Integer> primeList = new ArrayList<Integer>();
        int i;
        for (i = 0; i < min; i++) {
            primeList.add(factorsA[i] + factorsB[i]);
        }
        if (factorsA.length > factorsB.length) {
            for (; i < max; i++) {
                primeList.add(factorsA[i]);
            }
        } else if (factorsB.length > factorsA.length) {
            for (; i < max; i++) {
                primeList.add(factorsB[i]);
            }
        }
        trimPrimeList(primeList);
        return primeListToArray(primeList);
    }

    private static void trimPrimeList(ArrayList<Integer> primeList) {
        int i;
        // trim zero factors off end.
        for (i = primeList.size() - 1; i >= 0; i--) {
            if (primeList.get(i) == 0) {
                primeList.remove(i);
            } else {
                break;
            }
        }
    }

    public static int[] factor(int n) {
        ArrayList<Integer> primeList = new ArrayList<Integer>();
        int i = 0;
        int p = primes[i];
        int exponent = 0;
        while (n > 1) {
            // does the prime number divide evenly into n?
            int d = n / p;
            int m = d * p;
            if (m == n) {
                n = d;
                exponent += 1;
            } else {
                primeList.add(exponent);
                exponent = 0;
                i += 1;
                p = primes[i];
            }
        }
        if (exponent > 0) {
            primeList.add(exponent);
        }
        return primeListToArray(primeList);
    }

    /**
     * Get prime from table.
     *
     *
     * @param n Warning: Do not exceed getPrimeCount()-1.
     * @return Nth prime number, the 0th prime is 2
     */
    public static int getPrime(int n) {
        return primes[n];
    }

    /**
     * @return the number of primes stored in the table
     */
    public static int getPrimeCount() {
        return primes.length;
    }

    public JustRatio getJustRatio() {
        long n = 1;
        long d = 1;
        for (int i = 0; i < factors.length; i++) {
            int exponent = factors[i];
            int p = primes[i];
            if (exponent > 0) {
                for (int k = 0; k < exponent; k++) {
                    n = n * p;
                }
            } else if (exponent < 0) {
                exponent = 0 - exponent;
                for (int k = 0; k < exponent; k++) {
                    d = d * p;
                }
            }
        }
        return new JustRatio(n, d);
    }

    public int[] getFactors() {
        return factors.clone();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        printFactors(buffer, 1);
        buffer.append("/");
        printFactors(buffer, -1);
        return buffer.toString();
    }

    private void printFactors(StringBuffer buffer, int sign) {
        boolean gotSome = false;
        for (int i = 0; i < factors.length; i++) {
            int pf = factors[i] * sign;
            if (pf > 0) {
                if (gotSome)
                    buffer.append('*');
                int prime = primes[i];
                if (pf == 1) {
                    buffer.append("" + prime);
                } else if (pf == 2) {
                    buffer.append(prime + "*" + prime);
                } else if (pf > 2) {
                    buffer.append("(" + prime + "^" + pf + ")");
                }
                gotSome = true;
            }
        }
        if (!gotSome) {
            buffer.append("1");
        }
    }
}
