package MultiThread;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Th11_ThreadPool {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            Th11_Worker worker = new Th11_Worker("Labour-"+i);
            executorService.execute(worker);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            System.out.println(executorService.toString());
        }
        System.out.println("Finished all threads");
    }
}


/*
* https://www.tpointtech.com/java-thread-pool
*
* */