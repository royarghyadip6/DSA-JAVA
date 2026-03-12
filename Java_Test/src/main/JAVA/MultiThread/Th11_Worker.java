package MultiThread;

public class Th11_Worker implements Runnable {

    private final String message;
    public Th11_Worker(String thName) {
        this.message = thName;
    }

    @Override
    public void run() {

        System.out.println(Thread.currentThread().getName() +" Started. "+ message);
        processMessage();
        System.out.println(Thread.currentThread().getName() +" End.");
    }

    private void processMessage() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException caught");
        }
    }
}
