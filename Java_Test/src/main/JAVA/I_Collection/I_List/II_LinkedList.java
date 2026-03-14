package I_Collection.I_List;

import java.util.LinkedList;

public class II_LinkedList {
    public static void main(String[] args) {

        /**
         * LinkedList is a part of the Java Collections Framework and is present in the java.util package. It implements a doubly linked list where elements are stored as nodes containing data and references to the previous and next nodes, rather than in contiguous memory locations.
         *
         * The size of the LinkedList can grow or shrink dynamically at runtime.
         * Maintains the order in which elements are inserted.
         * Multiple duplicate elements can be stored.
         * LinkedList is not thread-safe by default; it can be synchronized using Collections.synchronizedList().
         * Provides better performance than ArrayList for insertion and deletion operations, especially at the beginning or middle.
         *
         * Note: LinkedList nodes cannot be accessed directly by index; elements must be accessed by traversing from the head.
         *
         * */


        LinkedList<String> ll = new LinkedList<>();

        ll.add("Geeks");
        ll.add("Geeks");
        ll.add(1, "For");
        ll.add("Roy");
        System.out.println("Initial LinkedList " + ll);

        ll.remove(1);
        System.out.println("After the Index Removal " + ll);

        ll.set(1, "ARGHYA");
        System.out.println("Updated LinkedList " + ll);

        ll.remove("Geeks");
        System.out.println("After the Object Removal "+ ll);

        // Using the Get method and the for loop
        for (int i = 0; i < ll.size(); i++) {
            System.out.print(ll.get(i) + " ");
        }
    }
}
