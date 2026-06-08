import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Test2 {
    public static void main(String[] args) {

        List<Integer> listNum = Arrays.asList(7,12,2,32,4,55,6,43);
        List<String> listStr = Arrays.asList("A", "B", "C");
        List resultList = new ArrayList<>();
        Map resultMap = new HashMap<>();

        resultMap = listNum.stream().collect(Collectors.partitioningBy(x-> x %2 ==0));
        System.out.println(resultMap);

        System.out.println(listStr.stream().collect(Collectors.joining(",")));


    }
}
