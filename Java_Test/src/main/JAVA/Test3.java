import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test3 {
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(11,1,3,40,5,6,25,50,20,70);
        Map<Object, Long> resultMap = new HashMap<>();
        List resultList = new ArrayList();
        Set<Integer> resultSet = new HashSet();
        List<String> list1 = Arrays.asList("apple","Banana","Avocado","grape");
        List<Integer> list2 = Arrays.asList(1,2,3,4,4,5,2);
        String str = "ARGHYDIP ROY";

        StringBuffer bf = new StringBuffer();
        IntStream.range(0, str.length())
                .forEach(i->bf.append(str.charAt(str.length()-1-i)));
        System.out.println(bf);
        str.concat("test");
        System.out.println(str);
    }
}
