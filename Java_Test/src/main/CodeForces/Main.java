package CodeForces;

//public class Main {
//    public static void main(String[] args) {
//
//
//    }
//}

abstract class Animal {
    Animal() {
        System.out.println("Animal constructor");
    }
}

class Dog extends Animal {
    Dog() {
        System.out.println("Dog constructor");
    }
}

public class Main {
    public static void main(String[] args) {
        Dog d = new Dog();
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