package I_Collection.I_List;

import java.util.Stack;

public class IV_Stack {
    public static void main(String[] args) {
/*
In Java, a Stack is a linear data structure that follows the Last In First Out (LIFO) principle and is defined in the java.util package. Internally, it extends the Vector class.

Stack class maintains insertion order and allows duplicates and null values.
Grows dynamically when its capacity is exceeded.
All the methods of Stack are synchronized. It is thread-safe.
Stack is considered a legacy class, introduced in early versions of Java and a preferred solution to implement Stack Data Structure (especially when thread synchronization is not needed) is either to use ArrayDeque or LinkedList
Stack class implements List, RandomAccess, Cloneable, and Serializable interfaces.

* */

        // Creating an empty Stack
        Stack<String> stack = new Stack<String>();

        // Use push() to add elements into the Stack
        stack.push("Welcome");
        stack.push("To");
        stack.push("Geeks");
        stack.push("For");
        stack.push("Geeks");

        // Displaying the Stack
        System.out.println("Initial Stack: " + stack);

        // Fetching the element at the head of the Stack
        System.out.println("Top element is: " + stack.peek());
        System.out.println("Stack: " + stack);
        stack.pop();
        System.out.println("After pop Stack: " + stack);
        System.out.println("A present: "+stack.search("A"));
        System.out.println("Geeks present: "+stack.search("Geeks"));
        System.out.println("Empty : "+stack.empty());
        System.out.println("Final Stack: " + stack);


    }
}
