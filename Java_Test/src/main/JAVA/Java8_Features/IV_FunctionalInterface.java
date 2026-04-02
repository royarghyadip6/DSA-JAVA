package Java8_Features;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class IV_FunctionalInterface {
    public static void main(String[] args) {
/*
    A Predicate is a functional interface in java.util.function package introduced in Java 8.
     It represents a condition that takes one argument and returns a boolean.
* */
        Predicate<Integer> predicate = (i) -> i<10;
        boolean p = predicate.test(5);
        System.out.println("Testing 5 is less then 10 > "+p);

// and() — Both conditions must be true
        int numberAnd =-10;
        Predicate<Integer> predicateEven = (n) -> n%2 ==0;
        Predicate<Integer> predicatePositive = (n) -> n>0;
        Predicate<Integer> predicateAnd = predicateEven.and(predicatePositive);
        System.out.println("Number "+ numberAnd +" is even and positive? "+predicateEven.test(numberAnd));

//  or() — At least one condition must be true
        int numberOr =-10;
        Predicate<Integer> predicateOdd = (n) -> n%2 != 0;
        Predicate<Integer> predicateNegative = (n) -> n < 0;
        Predicate<Integer> predicateOr = predicateOdd.or(predicateNegative);
        System.out.println("Number "+ numberOr +" is even or positive? "+predicateOr.test(numberOr));

// negate() — Reverses the condition
        System.out.println("Number "+ numberOr +" is negative ? "+predicateNegative.negate().test(numberOr));

/*
    A BiPredicate is a functional interface in java.util.function that takes two arguments and returns a boolean.
     It's the two-argument version of Predicate.
* */
        int a = 30;
        int b = 40;
        BiPredicate<Integer,Integer> biPredicate = (i,j) -> i > j;
        boolean bp = biPredicate.test(a,b);
        System.out.println(a+" is greater then "+b+"? "+bp);


/*
Supplier is a functional interface in java.util.function package introduced in Java 8.
 It produces/supplies a value without taking any input.
Syntax:
    Supplier<T> with single method get()
Key Points:
    No argument, only return type
    Method is get()
    Used for lazy initialization
    Used in Optional.orElseGet()
* */
        Supplier<String> supplier = () -> "Hello";
        System.out.println(supplier.get());  // Hello

/*
Consumer is a functional interface in java.util.function package introduced in Java 8.
It consumes/accepts a value without returning anything.
Syntax:
    Consumer<T> with single method accept()
Key Points:
    Takes one argument, no return type (void)
    Method is accept()
    Used for printing, saving, sending data
    Supports andThen() for chaining
*/
        Consumer<String> c = (ip) -> System.out.println(ip);
        c.accept("Hello");  // Hello

        Consumer<String> upper = (s) -> System.out.println(s.toUpperCase());
        Consumer<String> lower = (s) -> System.out.println(s.toLowerCase());

        Consumer<String> chain = upper.andThen(lower);
        chain.accept("Hello");

/*
Function is a functional interface in java.util.function package introduced in Java 8.
It takes one input and returns one output.

* */

    }
}
