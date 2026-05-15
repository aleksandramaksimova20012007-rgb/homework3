import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {

    private static final int MAX_NUMBER = 10_000_000;

    public static void main(String[] args) throws Exception {

        int[] threadCounts = {1, 2, 4, 8, 16, 32, 64, 128};

        File resultsDir = new File("results");
        if (!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter("results/collatz_parallel_results.csv")) {

            writer.write("numbers_count,threads,total_steps,average_steps,execution_time_seconds\n");

            for (int threads : threadCounts) {

                long startTime = System.nanoTime();

                ExecutorService executor = Executors.newFixedThreadPool(threads);

                int batchSize = MAX_NUMBER / threads;

                List<Future<Long>> futures = new ArrayList<>();

                for (int i = 0; i < threads; i++) {
                    int start = i * batchSize + 1;
                    int end;

                    if (i == threads - 1) {
                        end = MAX_NUMBER;
                    } else {
                        end = (i + 1) * batchSize;
                    }

                    Callable<Long> task = new CollatzTask(start, end);
                    futures.add(executor.submit(task));
                }

                long totalSteps = 0;

                for (Future<Long> future : futures) {
                    totalSteps += future.get();
                }

                executor.shutdown();

                long endTime = System.nanoTime();

                double executionTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
                double averageSteps = (double) totalSteps / MAX_NUMBER;

                writer.write(MAX_NUMBER + "," +
                        threads + "," +
                        totalSteps + "," +
                        averageSteps + "," +
                        executionTimeSeconds + "\n");

                System.out.println("Threads: " + threads);
                System.out.println("Total steps: " + totalSteps);
                System.out.println("Average steps: " + averageSteps);
                System.out.println("Execution time: " + executionTimeSeconds + " seconds");
                System.out.println("-----------------------------------");
            }
        }
    }

    private static class CollatzTask implements Callable<Long> {

        private final int start;
        private final int end;

        public CollatzTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public Long call() {
            long sumSteps = 0;

            for (int number = start; number <= end; number++) {
                sumSteps += countCollatzSteps(number);
            }

            return sumSteps;
        }
    }

    private static int countCollatzSteps(long number) {
        int steps = 0;

        while (number != 1) {
            if (number % 2 == 0) {
                number = number / 2;
            } else {
                number = 3 * number + 1;
            }

            steps++;
        }

        return steps;
    }
}