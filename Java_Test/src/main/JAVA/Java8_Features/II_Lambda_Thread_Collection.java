package Java8_Features;

/*
// Code with normal thread without lambda expression
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Run Method");
        for (int i = 0; i < 10; i++) {
            System.out.println("Child thread running: "+i);
        }
    }
}
public class II_Lambda_Thread_Collection {
    public static void main(String[] args) {
        MyRunnable runnable = new MyRunnable();
        Thread t = new Thread(runnable);
        t.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Main thread running: "+i);
        }
    }
}
*/


/*
// Thread Code with lambda expression
public class II_Lambda_Thread_Collection {
    public static void main(String[] args) {
        Runnable runnable = () -> {
            for (int i=0; i <10; i++) {
                System.out.println("Child thread running: "+i);
            }
        };
        Thread t = new Thread(runnable);
        t.start();
        for (int i = 0; i < 10; i++) {
            System.out.println("Main thread running: "+i);
        }
    }
}
*/
/*

// Collection code without lambda expression

import java.util.*;

class MyComparator implements Comparator<Integer> {

    public int compare(Integer o1, Integer o2) {
        if (o1 > o2) {
            return 1;
        } else if (o1 < o2) {
            return -1;
        } else {
            return 0;
        }
    }
}
public class II_Lambda_Thread_Collection {
    public static void main(String[] args) {
        Integer []arr = {23,45,64,12,9,40,69,96,45,60,39,65};
        List<Integer> arrayList = Arrays.asList(arr);

        arrayList.sort(new MyComparator());
        System.out.println(arrayList);

    }
}
*/

// Collection with lambda expression
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class II_Lambda_Thread_Collection {
    public static void main(String[] args) {
        Integer []arr = {23,45,64,12,9,40,69,96,45,60,39,65};
        List<Integer> arrayList = Arrays.asList(arr);

        Comparator<Integer> cp = (o1,o2) -> o1>o2?1:(o1<o2?-1:0);
        arrayList.sort(cp);
        System.out.println(arrayList);

    }
}