package CodeForces;

import java.util.*;

public class Main {
    public static void main(String[] args) {

//  Decleare Variable
        Scanner sc = new Scanner(System.in);
        String output;
//  Input number
        int number = sc.nextInt();
        sc.nextLine();
//  String input
        for (int i = 0; i < number ; i++) {
            String keyInput = sc.nextLine();
            if (keyInput.length() > 10) {
                output = ""+ keyInput.charAt(0) + (keyInput.length()-2) + keyInput.charAt(keyInput.length()-1);
            } else {
                output = keyInput;
            }
            System.out.println(output);
        }

    }
}


/*
https://codeforces.com/problemset/problem/71/A

INPUT:
4
word
localization
internationalization
pneumonoultramicroscopicsilicovolcanoconiosis

Output:
word
l10n
i18n
p43s

* */