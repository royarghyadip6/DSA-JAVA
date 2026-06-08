package src.main.StudyNotes.JAVA;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestString {
    public static void main(String[] args) {
        String input = "Learning Java";
        String result = input.codePoints()
                .mapToObj(ch -> (char)ch)
                .collect(
                        StringBuilder::new,
                        (sb,ch) -> sb.insert(0, ch),
                        ((sb1, sb2) -> sb2.append(sb1))
                ).toString();
        System.out.println(result);
    }
}