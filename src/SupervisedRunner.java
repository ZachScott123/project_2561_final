import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;

/**
 * Runs a worker task under supervision with restart/backoff behavior.
 *
 * The run() method loops while the supplier says the simulation is still active.
 * It catches RuntimeException and Exception, logs the failure, applies exponential
 * backoff, and restarts the worker until the restart budget is exceeded.
 */
public class SupervisedRunner implements Runnable {
    private final String name;
    private final Runnable work;
    private final BooleanSupplier simulationRunning;

    private long backoffMillis = 100;
    private final long maxBackoffMillis = 5000;
    private final long resetAfterMillis = 10_000;
    private final long restartWindowMillis = 30_000;
    private final int maxRestartsInWindow = 5;

    private final Deque<Long> failureTimestamps = new ArrayDeque<>();
    private long lastFailureTime = System.currentTimeMillis();

    public SupervisedRunner(String name, Runnable work, BooleanSupplier simulationRunning) {
        this.name = name;
        this.work = work;
        this.simulationRunning = simulationRunning;
    }

    @Override
    public void run() {
        while (simulationRunning.getAsBoolean()) {
            if (shouldResetBackoff()) {
                resetBackoff();
            }

            try {
                work.run();
            } catch (RuntimeException e) {
                if (handleFailure(e)) {
                    break;
                }
            } catch (Exception e) {
                if (handleFailure(e)) {
                    break;
                }
            }

            if (!simulationRunning.getAsBoolean()) {
                break;
            }
        }
    }

    private boolean shouldResetBackoff() {
        return backoffMillis != 100 && (System.currentTimeMillis() - lastFailureTime) >= resetAfterMillis;
    }

    private void resetBackoff() {
        System.out.println("[Worker:" + name + "] running successfully for 10s, resetting backoff to 100ms");
        backoffMillis = 100;
    }

    private boolean handleFailure(Throwable t) {
        long now = System.currentTimeMillis();
        failureTimestamps.addLast(now);
        while (!failureTimestamps.isEmpty() && failureTimestamps.peekFirst() < now - restartWindowMillis) {
            failureTimestamps.removeFirst();
        }

        if (failureTimestamps.size() >= maxRestartsInWindow) {
            String message = "worker \"" + name + "\" exceeded restart budget; will not be restarted.";
            System.err.println(message);
            Main.logToFile(message);
            return true;
        }

        System.err.println("[Worker:" + name + "] exception, restarting after " + backoffMillis + "ms");
        t.printStackTrace(System.err);
        lastFailureTime = now;

        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return true;
        }

        backoffMillis = Math.min(maxBackoffMillis, backoffMillis * 2);
        return false;
    }
}
