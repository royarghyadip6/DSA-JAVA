package Java8_Features.Problems;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/*
    WAJP to take numbers and check cube of numbers is greater than 50
     and store value and the cube value > 50 into a map.
* */

public class P_02_ListToHashMap {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        System.out.println("With java 8");
        HashMap<Integer, Integer> map = (HashMap<Integer, Integer>) list.stream()
                .filter(element -> (int)Math.pow(element,3) >50)
                .collect(Collectors.toMap(n -> n, n -> (int)Math.pow(n,3)));
        System.out.println(map);

        System.out.println("Without java 8");
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        Iterator<Integer> iterator = list.iterator();
        System.out.println("ABC");
        iterator.forEachRemaining(n -> System.out.print(n+" "));
        Iterator<Integer> iterator1 = list.iterator();
        System.out.println(iterator1.hasNext());
        while (iterator1.hasNext()) {
            int value = iterator1.next();
            if ((int) Math.pow(value,3) >50){
                hashMap.put(value, (int) Math.pow(value,3));
            }
        }
        System.out.println(hashMap);


    }
}
