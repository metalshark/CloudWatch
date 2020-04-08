package github.metalshark.cloudwatch.runnables;

public class TickRunnable implements Runnable {

    private static long lastTimeMillis = System.currentTimeMillis();
    private static double maxElapsedMillis = 0;
    private static double numberOfTicks = 0;

    @Override
    public synchronized void run() {
        final long timeMillis = System.currentTimeMillis();
        final long elapsedMillis = timeMillis - lastTimeMillis;
        if (elapsedMillis > maxElapsedMillis) maxElapsedMillis = elapsedMillis;
        lastTimeMillis = timeMillis;
        numberOfTicks++;
    }

    public double getMaxElapsedMillisAndReset() {
        final double oldMaxElapsedMillis = maxElapsedMillis;
        maxElapsedMillis = 0;
        return oldMaxElapsedMillis;
    }

    public double getNumberOfTicksAndReset() {
        final double oldNumberOfTicks = numberOfTicks;
        numberOfTicks = 0;
        return oldNumberOfTicks;
    }

}
