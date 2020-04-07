package github.metalshark.cloudwatch.runnables;

public class TickRunnable implements Runnable {

    private static long lastTimeMillis = System.currentTimeMillis();
    public static double maxElapsedMillis = 0;
    public static double numberOfTicks = 0;

    @Override
    public synchronized void run() {
        final long timeMillis = System.currentTimeMillis();
        final long elapsedMillis = lastTimeMillis - timeMillis;
        if (elapsedMillis > maxElapsedMillis) maxElapsedMillis = elapsedMillis;
        lastTimeMillis = timeMillis;
        numberOfTicks++;
    }

}
