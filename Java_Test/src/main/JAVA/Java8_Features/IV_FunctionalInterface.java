package Java8_Features;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

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
        System.out.println("--- 1. BASIC PRIMITIVE INTERFACES ---");

        // IntPredicate: Returns boolean
        IntPredicate isEven = n -> n % 2 == 0;
        System.out.println("IntPredicate (is 10 even?): " + isEven.test(10));

        // IntConsumer: Returns void (side effects)
        IntConsumer logger = n -> System.out.println("IntConsumer logging: " + n);
        logger.accept(42);

        // IntSupplier: Returns int
        IntSupplier randomInt = () -> (int) (Math.random() * 100);
        System.out.println("IntSupplier value: " + randomInt.getAsInt());


        System.out.println("\n--- 2. TRANSFORMATION FUNCTIONS ---");

        // IntFunction<R>: int -> Object
        IntFunction<String> toCurrency = i -> "$" + i + ".00";
        System.out.println("IntFunction (to String): " + toCurrency.apply(50));

        // ToIntFunction<T>: Object -> int
        ToIntFunction<String> stringLength = s -> s.length();
        System.out.println("ToIntFunction (length of 'Gemini'): " + stringLength.applyAsInt("Gemini"));

        // IntToLongFunction: int -> long
        IntToLongFunction multiplyToLong = i -> (long) i * 1_000_000_000L;
        System.out.println("IntToLongFunction: " + multiplyToLong.applyAsLong(2));

        // IntToDoubleFunction: int -> double
        IntToDoubleFunction divideByThree = i -> i / 3.0;
        System.out.println("IntToDoubleFunction: " + divideByThree.applyAsDouble(10));


        System.out.println("\n--- 3. OPERATORS (Same Input/Output) ---");

        // IntUnaryOperator: 1 input, 1 output
        IntUnaryOperator increment = n -> n + 1;
        System.out.println("IntUnaryOperator (10 + 1): " + increment.applyAsInt(10));

        // IntBinaryOperator: 2 inputs, 1 output
        IntBinaryOperator multiply1 = (e, d) -> e * d;
        System.out.println("IntBinaryOperator (5 * 4): " + multiply1.applyAsInt(5, 4));


        System.out.println("\n--- 4. HYBRID INTERFACES ---");

        // ObjIntConsumer<T>: Object + int -> void
        List<Integer> numbers = new ArrayList<>();
        ObjIntConsumer<List<Integer>> listAdder = (list, val) -> list.add(val);

        listAdder.accept(numbers, 100);
        listAdder.accept(numbers, 200);
        System.out.println("ObjIntConsumer result (List contents): " + numbers);

    }
}
