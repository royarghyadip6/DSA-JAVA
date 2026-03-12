package Java8_Features;

@FunctionalInterface
interface I_Demo {
    public void printHi();
}

@FunctionalInterface
interface I_PrintName {
    public void name(String str);
}

@FunctionalInterface
interface I_PrintAdd {
    public void add(int a, int b);
}

@FunctionalInterface
interface I_Add {
    public int add(int a, int b);
}

public class I_LambdaExpression {
    public static void main(String[] args) {
        System.out.println("1. No parameter, single statement");
        I_Demo demo = () -> System.out.println("Printing Hello");
        demo.printHi();  // Method Calling

        System.out.println("2. One parameter, single statement (no parentheses needed) ");
        I_PrintName name1 = name -> System.out.println("Hello " + name);
        name1.name("Arghya");

        System.out.println("3. One parameter with type");
        I_PrintName name2 = (String name) -> System.out.println("Hello " + name);
        name1.name("Roy");

        // Two parameters
        System.out.println("4. Single statement with two parameter");
        I_PrintAdd pAdd = (a, b) -> System.out.println("Sum is: "+ (a+b));
        pAdd.add(10,50);

        System.out.println("5. Return result with two parameter");
        I_Add add1 = (a, b) -> a + b;
        System.out.println("Printing the sum: "+ add1.add(40,50));

        System.out.println("6. Multi-line body — needs curly braces and return");
        I_Add add2 =(int a, int b) -> {
            int sum = a + b;
            return sum;
        };
        System.out.println("Multiline sum: "+ add2.add(1,50));


    }
}

/*
// Case 1:
@FunctionalInterface
interface A {
    public void method1();
}
@FunctionalInterface
interface B extends A {
}
// Valid. because FunctionalInterface can contain only 1 abstract method. interface B is extending A. So, interface B will inherit method1. So both of the interface have 1 abstract method only, thus no problem at all.

*/


/*
// Case 2:
@FunctionalInterface
interface A {
    public void method1();
}
@FunctionalInterface
interface B extends A {
    public void method1();
}

// Valid. Because interface B will inherit and override method1.
*/

/*
// Case 3:
@FunctionalInterface
interface A {
    public void method1();
}
@FunctionalInterface
interface B extends A {
    public void method2();
}

// Invalid. Because interface B already contains 1 in method1.
*/


/*
// Case 4:
@FunctionalInterface
interface A {
    public void method1();
}
interface B extends A {
    public void method2();
}

// Valid. Because interface B is not a FunctionalInterface, So interface B can contain 2 abstract method.
*/



/*
Q. What is Lambda Expressions?
An anonymous function (no name, no return type, and no access modifier) that can be passed as an argument. We can remove the parameter type as compiler can guess the type.
Syntax: (parameters) -> { body };
Three parts:
    Parameter list — inputs to the function (can be empty)
    Arrow operator -> — separates parameters from body
    Body — the logic/code to execute
Rule: If body has only ONE statement, you can skip {} and return.
If body has MULTIPLE statements, {} and return are mandatory.

Q: Why were lambdas introduced?
To eliminate boilerplate code (anonymous inner classes) and enable functional programming in Java.

* */

/*
Lambda & Functional Interface — The Connection
Lambda expression can ONLY be used where a Functional Interface is expected.

What is a Functional Interface?
An interface that has exactly ONE abstract method.

@FunctionalInterface is optional.
But it has advantage, If we made any mistake compiler will throw error.

* */