/*
 * Copyright 1997 Phil Burk, Mobileer Inc
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

/**
 * Polynomial<br>
 * Implement polynomial using Vector as coefficient holder. Element index is power of X, value at a
 * given index is coefficient.<br>
 * <br>
 *
 * @author Nick Didkovsky, (C) 1997 Phil Burk and Nick Didkovsky
 */

public class Polynomial {

    private static final Logger LOGGER = LoggerFactory.getLogger(Polynomial.class);

    private final Vector terms;

    // TODO: Does this need to exist?
    static class DoubleHolder {
        double value;

        public DoubleHolder(double val) {
            value = val;
        }

        public double get() {
            return value;
        }

        public void set(double val) {
            value = val;
        }
    }

    /** create a polynomial with no terms */
    public Polynomial() {
        terms = new Vector();
    }

    /** create a polynomial with one term of specified constant */
    public Polynomial(double c0) {
        this();
        appendTerm(c0);
    }

    /** create a polynomial with two terms with specified coefficients */
    public Polynomial(double c1, double c0) {
        this(c0);
        appendTerm(c1);
    }

    /** create a polynomial with specified coefficients */
    public Polynomial(double c2, double c1, double c0) {
        this(c1, c0);
        appendTerm(c2);
    }

    /** create a polynomial with specified coefficients */
    public Polynomial(double c3, double c2, double c1, double c0) {
        this(c2, c1, c0);
        appendTerm(c3);
    }

    /** create a polynomial with specified coefficients */
    public Polynomial(double c4, double c3, double c2, double c1, double c0) {
        this(c3, c2, c1, c0);
        appendTerm(c4);
    }

    /**
     * Append a term with specified coefficient. Power will be next available order (ie if the
     * polynomial is of order 2, appendTerm will supply the coefficient for x^3
     */
    public void appendTerm(double coefficient) {
        terms.addElement(new DoubleHolder(coefficient));
    }

    /** Set the coefficient of given term */
    public void setTerm(double coefficient, int power) {
        // If setting a term greater than the current order of the polynomial, pad with zero terms
        int size = terms.size();
        if (power >= size) {
            for (int i = 0; i < (power - size + 1); i++) {
                appendTerm(0);
            }
        }
        ((DoubleHolder) terms.elementAt(power)).set(coefficient);
    }

    /**
     * Add the coefficient of given term to the specified coefficient. ex. addTerm(3, 1) add 3x to a
     * polynomial, addTerm(4, 3) adds 4x^3
     */
    public void addTerm(double coefficient, int power) {
        setTerm(coefficient + get(power), power);
    }

    /** @return coefficient of nth term (first term=0) */
    public double get(int power) {
        if (power >= terms.size())
            return 0.0;
        else
            return ((DoubleHolder) terms.elementAt(power)).get();
    }

    /** @return number of terms in this polynomial */
    public int size() {
        return terms.size();
    }

    /**
     * Add two polynomials together
     *
     * @return new Polynomial that is the sum of p1 and p2
     */
    public static Polynomial plus(Polynomial p1, Polynomial p2) {
        Polynomial sum = new Polynomial();
        for (int i = 0; i < Math.max(p1.size(), p2.size()); i++) {
            sum.appendTerm(p1.get(i) + p2.get(i));
        }
        return sum;
    }

    /**
     * Subtract polynomial from another. (First arg - Second arg)
     *
     * @return new Polynomial p1 - p2
     */
    public static Polynomial minus(Polynomial p1, Polynomial p2) {
        Polynomial sum = new Polynomial();
        for (int i = 0; i < Math.max(p1.size(), p2.size()); i++) {
            sum.appendTerm(p1.get(i) - p2.get(i));
        }
        return sum;
    }

    /**
     * Multiply two Polynomials
     *
     * @return new Polynomial that is the product p1 * p2
     */

    public static Polynomial mult(Polynomial p1, Polynomial p2) {
        Polynomial product = new Polynomial();
        for (int i = 0; i < p1.size(); i++) {
            for (int j = 0; j < p2.size(); j++) {
                product.addTerm(p1.get(i) * p2.get(j), i + j);
            }
        }
        return product;
    }

    /**
     * Multiply a Polynomial by a scaler
     *
     * @return new Polynomial that is the product p1 * p2
     */

    public static Polynomial mult(double scaler, Polynomial p1) {
        Polynomial product = new Polynomial();
        for (int i = 0; i < p1.size(); i++) {
            product.appendTerm(p1.get(i) * scaler);
        }
        return product;
    }

    /** Evaluate this polynomial for x */
    public double evaluate(double x) {
        double result = 0.0;
        for (int i = 0; i < terms.size(); i++) {
            result += get(i) * Math.pow(x, i);
        }
        return result;
    }

    @Override
    public String toString() {
        String s = "";
        if (size() == 0)
            s = "empty polynomial";
        boolean somethingPrinted = false;
        for (int i = size() - 1; i >= 0; i--) {
            if (get(i) != 0.0) {
                if (somethingPrinted)
                    s += " + ";
                String coeff = "";
                // if (get(i) == (int)(get(i)))
                // coeff = (int)(get(i)) + "";
                if ((get(i) != 1.0) || (i == 0))
                    coeff += get(i);
                if (i == 0)
                    s += coeff;
                else {
                    String power = "";
                    if (i != 1)
                        power = "^" + i;
                    s += coeff + "x" + power;
                }
                somethingPrinted = true;
            }
        }
        return s;
    }

    public static void main(String[] args) {
        Polynomial p1 = new Polynomial();
        LOGGER.debug("p1=" + p1);
        Polynomial p2 = new Polynomial(3);
        LOGGER.debug("p2=" + p2);
        Polynomial p3 = new Polynomial(2, 3);
        LOGGER.debug("p3=" + p3);
        Polynomial p4 = new Polynomial(1, 2, 3);
        LOGGER.debug("p4=" + p4);
        LOGGER.debug("p4*5=" + Polynomial.mult(5.0, p4));

        LOGGER.debug("{}", p4.evaluate(10));

        LOGGER.debug("{}", Polynomial.plus(p4, p1));
        LOGGER.debug("{}", Polynomial.minus(p4, p3));
        p4.setTerm(12.2, 5);
        LOGGER.debug("{}", p4);
        p4.addTerm(0.8, 5);
        LOGGER.debug("{}", p4);
        p4.addTerm(0.8, 7);
        LOGGER.debug("{}", p4);
        LOGGER.debug("{}", Polynomial.mult(p3, p2));
        LOGGER.debug("{}", Polynomial.mult(p3, p3));
        LOGGER.debug("{}", Polynomial.mult(p2, p2));

        Polynomial t2 = new Polynomial(2, 0, -1); // 2x^2-1, Chebyshev Polynomial of order 2
        Polynomial t3 = new Polynomial(4, 0, -3, 0); // 4x^3-3x, Chebyshev Polynomial of order 3
        // Calculate Chebyshev Polynomial of order 4 from relation Tk+1(x) = 2xTk(x) - Tk-1(x)
        Polynomial t4 = Polynomial.minus(Polynomial.mult(t3, (new Polynomial(2, 0))), t2);
        LOGGER.debug(t2 + "\n" + t3 + "\n" + t4);
        // com.softsynth.jmsl.util

    }
}
