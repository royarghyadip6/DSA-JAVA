package I_Collection.I_List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  The synchronizedList() method of java.util.Collections class is used to return a synchronized (thread-safe) list backed by the specified list. In order to guarantee serial access, it is critical that all access to the backing list is accomplished through the returned list. Syntax:
 * public static <T> List<T>
 *   synchronizedList(List<T> list)
 * Parameters: This method takes the list as a parameter to be "wrapped" in a synchronized list. Return Value: This method returns a synchronized view of the specified list. Below are the examples to illustrate the synchronizedList() method
 *
 * */

public class I_SynchronizedArrayList {
    public static void main(String[] argv) throws Exception {
        try {

            // creating object of List<String>
            List<String> list = new ArrayList<String>();

            // populate the list
            list.add("A");
            list.add("B");
            list.add("C");
            list.add("D");
            list.add("E");

            // printing the Collection
            System.out.println("List : " + list);

            // create a synchronized list
            List<String> synlist = Collections.synchronizedList(list);

            // printing the Collection
            System.out.println("Synchronized list is : " + synlist);
        } catch (IllegalArgumentException e) {
            System.out.println("Exception thrown : " + e);
        }
    }
}
