package Java8_Features.Problems;

/*
    WAJP to take numbres and check cube of numbers is greater than 50. and store the cube value > 50 into a list.
* */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class P_01_ListToList {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        System.out.println("With java 8");
        List<Integer> result1 = list.stream().map(x -> x * x * x).filter(e -> e > 50).collect(Collectors.toList());
        result1.forEach(System.out::println);

        System.out.println("Without java 8");
        List<Integer> result2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int cube = (int) Math.pow(list.get(i),3);
            if (cube > 50) {
                result2.add(cube);
            }
        }

        result2.forEach( n -> System.out.print(n+" "));

    }

}
