package Java8_Features;

import java.util.*;
import java.util.stream.Collectors;

/*
    Stream API is a way to express and process collections of objects.
    The features of Java streams are mentioned below:
        A Stream is not a data structure; it just takes input from Collections, Arrays or I/O channels.
        Streams do not modify the original data; they only produce results using their methods.

    There are two types of Operations in Streams:
        Intermediate Operations :-
            Methods are chained together.
            Intermediate operations transform a stream into another stream.
            It enables the concept of filtering where one method filters data and passes it to another method after processing.

            flatMap(List::stream):      Flattens the nested lists into a single stream of strings.
            filter(s -> s.startsWith("S")):     Filters the strings to only include those that start with "S".
            map(String::toUpperCase):   Converts each string in the stream to uppercase.
            distinct():     Removes any duplicate strings.
            sorted():   Sorts the resulting strings alphabetically.
            peek(...):  Adds each processed element to the intermediateResults set for intermediate inspection.
            collect(Collectors.toList()):   Collects the final processed strings into a list called result.

        Terminal Operations :-
            Terminal Operations are the type of Operations that return the result. These Operations are not processed further just return a final result value.

            forEach:    Prints each name in the list.
            collect:    Filters names starting with 'S' and collects them into a new list.
            reduce:     Concatenates all names into a single string.
            count:      Counts the total number of names.
            findFirst:  Finds and prints the first name in the list.
            allMatch:   Checks if all names start with 'S'.
            anyMatch:   Checks if any name starts with 'S'.

* */
public class V_Stream {
    public static void main(String[] args) {

        List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,10,5,3,2);
        List<String> names = Arrays.asList("Arghya", "Roy", "Java", "Spring", "Boot", "Roy");

        // 1️⃣ Find even numbers
        List<Integer> evens = numbers.stream()
                .filter(n -> n % 2 == 0)
                .toList();
        System.out.println("Even numbers: " + evens);

        // 2️⃣ Find duplicate elements
        Set<Integer> duplicates = numbers.stream()
                .filter(n -> Collections.frequency(numbers, n) > 1)
                .collect(Collectors.toSet());
        System.out.println("Duplicates: " + duplicates);

        // 3️⃣ Find first non-repeated character
        String input = "java stream";
        Character firstNonRepeat = input.chars()
                .mapToObj(c -> (char) c)
                .filter(c -> input.indexOf(c) == input.lastIndexOf(c))
                .findFirst()
                .orElse(null);
        System.out.println("First non-repeated char: " + firstNonRepeat);

        // 4️⃣ Count frequency of each element
        Map<Integer, Long> frequency = numbers.stream()
                .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        System.out.println("Frequency: " + frequency);

        // 5️⃣ Find max and min
        int max = numbers.stream().max(Integer::compare).orElse(0);
        int min = numbers.stream().min(Integer::compare).orElse(0);
        System.out.println("Max: " + max + ", Min: " + min);

        // 6️⃣ Sort list
        List<Integer> sorted = numbers.stream()
                .sorted()
                .toList();
        System.out.println("Sorted: " + sorted);

        // 7️⃣ Remove duplicates
        List<Integer> unique = numbers.stream()
                .distinct()
                .toList();
        System.out.println("Unique: " + unique);

        // 8️⃣ Find second highest number
        int secondHighest = numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .skip(1)
                .findFirst()
                .orElse(-1);
        System.out.println("Second Highest: " + secondHighest);

        // 9️⃣ Convert list of strings to uppercase
        List<String> upper = names.stream()
                .map(String::toUpperCase)
                .toList();
        System.out.println("Uppercase: " + upper);

        // 🔟 Find sum of all numbers
        int sum = numbers.stream()
                .mapToInt(Integer::intValue)
                .sum();
        System.out.println("Sum: " + sum);

    }
}
