import java.util.concurrent.*;
import java.util.*;
import java.io.*;

class Task {
    int id;
    Task(int id) { this.id = id; }

    void process() throws InterruptedException {
        Thread.sleep(500);  // Simulate processing
        System.out.println("Processed Task: " + id);
    }
}

class Worker implements Runnable {
    private BlockingQueue<Task> queue;
    private List<String> results;

    Worker(BlockingQueue<Task> queue, List<String> results) {
        this.queue = queue;
        this.results = results;
    }

    public void run() {
        try {
            while (true) {
                Task task = queue.poll(1, TimeUnit.SECONDS);
                if (task == null) break;
                task.process();
                synchronized (results) {
                    results.add("Result of Task " + task.id);
                }
            }
        } catch (Exception e) {
            System.err.println("Worker error: " + e.getMessage());
        }
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
        List<String> results = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 10; i++) queue.add(new Task(i));

        for (int i = 0; i < 4; i++) executor.submit(new Worker(queue, results));

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        try (PrintWriter out = new PrintWriter("output.txt")) {
            for (String line : results) out.println(line);
        }
    }
}