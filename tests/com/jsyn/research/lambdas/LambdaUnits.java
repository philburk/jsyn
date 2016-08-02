package com.jsyn.research.lambdas;

import java.util.function.BinaryOperator;

public class LambdaUnits {


    public static void main(String[] args) {
        test();
    }

    void tryLambda(BinaryOperator<Double> op) {
        double result = op.apply(3.0,  4.0);
        System.out.println("result = " + result);
    }

    private static void test() {
        System.out.println("Test Lambdas");
  // Need Java 8!      tryLambda((x, y) -> (x * y));
    }

}
