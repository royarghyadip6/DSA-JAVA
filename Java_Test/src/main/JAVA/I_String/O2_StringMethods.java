package I_String;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class O2_StringMethods {
    public static void main(String[] args) {

        String s = "hello";
        String s2 = "Hello";
        String str = "abc123";
        String csv = "a,b,c";

        // 🔹 Basic
        System.out.println("length: " + s.length());
        System.out.println("charAt: " + s.charAt(1));
        System.out.println("isEmpty: " + "".isEmpty());

        // 🔹 Substring
        System.out.println("substring(1): " + s.substring(1));
        System.out.println("substring(1,4): " + s.substring(1, 4));
        System.out.println("subSequence: " + s.subSequence(1, 4));

        // 🔹 Comparison
        System.out.println("equals: " + s.equals("hello"));
        System.out.println("equalsIgnoreCase: " + s.equalsIgnoreCase(s2));
        System.out.println("compareTo: " + "a".compareTo("b"));
        System.out.println("compareToIgnoreCase: " + "A".compareToIgnoreCase("a"));
        System.out.println("contentEquals: " + "abc".contentEquals("abc"));
        System.out.println("contentEquals SB: " + "abc".contentEquals(new StringBuffer("abc")));

        // 🔹 Searching
        System.out.println("contains: " + s.contains("ell"));
        System.out.println("indexOf: " + s.indexOf("l"));
        System.out.println("indexOf from: " + s.indexOf("l", 3));
        System.out.println("lastIndexOf: " + s.lastIndexOf("l"));
        System.out.println("startsWith: " + s.startsWith("he"));
        System.out.println("endsWith: " + s.endsWith("lo"));

        // 🔹 Replace
        System.out.println("replace: " + "aab".replace('a', 'x'));
        System.out.println("replace seq: " + "abc".replace("a", "x"));
        System.out.println("replaceAll: " + "a1b2".replaceAll("\\d", ""));
        System.out.println("replaceFirst: " + "a1b2".replaceFirst("\\d", "X"));

        // 🔹 Case
        System.out.println("toUpperCase: " + s.toUpperCase());
        System.out.println("toLowerCase: " + s2.toLowerCase());
        System.out.println("toUpperCase Locale: " + s.toUpperCase(Locale.US));
        System.out.println("toLowerCase Locale: " + s.toLowerCase(Locale.US));

        // 🔹 Trim
        System.out.println("trim: '" + " hi ".trim() + "'");

        // 🔹 Split & Join
        System.out.println("split: " + Arrays.toString(csv.split(",")));
        System.out.println("split limit: " + Arrays.toString(csv.split(",", 2)));
        System.out.println("join: " + String.join("-", "a", "b"));
        System.out.println("join iterable: " + String.join(",", List.of("a", "b")));

        // 🔹 Concat
        System.out.println("concat: " + "Hi".concat(" All"));

        // 🔹 Conversion
        System.out.println("toCharArray: " + Arrays.toString("ab".toCharArray()));
        System.out.println("getBytes: " + Arrays.toString("A".getBytes()));
        System.out.println("getBytes UTF8: " + Arrays.toString("A".getBytes(StandardCharsets.UTF_8)));

        char[] dest = new char[2];
        "ab".getChars(0, 2, dest, 0);
        System.out.println("getChars: " + Arrays.toString(dest));

        // 🔹 Unicode
        System.out.println("codePointAt: " + "A".codePointAt(0));
        System.out.println("codePointBefore: " + "ABC".codePointBefore(1));
        System.out.println("codePointCount: " + "ABC".codePointCount(0, 3));
        System.out.println("offsetByCodePoints: " + "ABC".offsetByCodePoints(0, 2));

        // 🔹 Regex
        System.out.println("matches: " + str.matches("[a-z]+\\d+"));

        // 🔹 Region match
        System.out.println("regionMatches: " + "hello".regionMatches(0, "he", 0, 2));

        // 🔹 Utility
        System.out.println("hashCode: " + "abc".hashCode());
        System.out.println("intern: " + "abc".intern());
        System.out.println("toString: " + s.toString());

        // 🔹 ValueOf
        System.out.println("valueOf int: " + String.valueOf(10));
        System.out.println("valueOf double: " + String.valueOf(2.5));
        System.out.println("valueOf boolean: " + String.valueOf(true));
        System.out.println("valueOf char: " + String.valueOf('a'));
        System.out.println("valueOf object: " + String.valueOf(123));

        // 🔹 CopyValueOf
        System.out.println("copyValueOf: " + String.copyValueOf(new char[]{'x','y'}));
        System.out.println("copyValueOf range: " + String.copyValueOf(new char[]{'a','b','c'},1,2));

        // 🔹 Format
        System.out.println("format: " + String.format("Hi %s", "A"));
        System.out.println("format locale: " + String.format(Locale.US, "%.2f", 1.23));

        // 🔹 Extra
        System.out.println("intern check: " + ("abc".intern() == "abc"));

    }
}
