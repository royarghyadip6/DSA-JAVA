package Practice;

import java.util.*;
import java.util.stream.Collectors;

public class LambdaExpPractice {
    public static void main(String[] args) {
        ArrayList<Integer> al = new ArrayList<>();
        al.add(6);
        al.add(7);
        al.add(4);
        al.add(9);
        al.add(1);
        al.add(3);
        al.add(2);

        Comparator<Integer> c = (v1, v2) -> v1.compareTo(v2);
        Collections.sort(al,c);

        al.stream().forEach(System.out::println);

        Set<Integer> intList = al.stream().filter(i-> i%2==0).collect(Collectors.toSet());
        System.out.println(intList);
    }

}
