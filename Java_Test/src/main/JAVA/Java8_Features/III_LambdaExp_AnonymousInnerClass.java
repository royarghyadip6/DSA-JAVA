package Java8_Features;

public class III_LambdaExp_AnonymousInnerClass {
    public static void main(String[] args) {

        // Anonymous inner class => Creating anonymous class by implementing Runnable functional interface
        Runnable anonymousInnerClassObj = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    System.out.println("Anonymous inner class child thread "+i);
                }
            }
        };
        System.out.println(">> Main Thread");
        Thread thread = new Thread(anonymousInnerClassObj);     // Declearing thread
        System.out.println(">> Main Thread Before child thread start");
        thread.start();     // Starting Thread

        for (int i = 0; i < 10; i++) {
            System.out.println("Main thread "+i);
        }
    }
}

/***
 anonymous inner class != lambda expression
 If anonymous class implements a Functional Interface(Contains single abstract method),
 then only we can replace the anonymous inner class with lambda expression.
* */