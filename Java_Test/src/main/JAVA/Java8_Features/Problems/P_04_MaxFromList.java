package Java8_Features.Problems;

import java.util.Arrays;
import java.util.List;

public class P_04_MaxFromList {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(12, 22, 13, 46, 75, 16, 73, 88, 94, 10);

        list.stream().max(Integer::compareTo).ifPresent(System.out::println);
    }
}
