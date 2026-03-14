package I_Collection.I_List;

import java.util.Vector;

public class III_Vector {
    public static void main(String[] args) {

        /**
         * In Java, a Vector is a dynamic array that can grow or shrink in size as elements are added or removed. It is part of the java.util package and extends the AbstractList class.
         *
         * Maintains insertion order and allows duplicate and null values.
         * Dynamically grows its size when capacity is exceeded.
         * Implements List, RandomAccess, Cloneable, and Serializable interfaces.
         * Vector is a Legacy class that was introduced in early versions of Java.
         * Thread-safe: All methods are synchronized for safe multi-threaded access.
         * ArrayList is preferred over vector in general when in-built thread synchronization is not required..
        * */

        Vector<Integer> vector = new Vector<>(2); // initial capacity = 2
        System.out.println("Initial capacity: " + vector.capacity());

        // Add elements to trigger capacity increase
        vector.add(10);
        vector.add(20);
        System.out.println("Capacity after adding 2 elements: " + vector.capacity());

        vector.add(30); // Triggers resize (2 → 4)
        System.out.println("Capacity after adding 3rd element: " + vector.capacity());

        vector.add(40);
        vector.add(50); // Triggers resize again (4 → 8)
        System.out.println("Capacity after adding 5 elements: "+vector.capacity());
    }
}
