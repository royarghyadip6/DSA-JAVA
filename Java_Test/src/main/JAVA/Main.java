import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    public static void main(String[] args) {
        String str = "Arghyadip";
        List<String> list1 = new ArrayList<>();
        List<String> list = List.of();
        list.add("A");

        for(String s : list) {
            list.remove("B");
        }
    }

}
