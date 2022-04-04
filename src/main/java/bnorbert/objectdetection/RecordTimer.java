package bnorbert.objectdetection;

import java.util.concurrent.TimeUnit;

public class RecordTimer {

    private RecordTimer() {
        throw new SecurityException("Utility class");
    }

    private static long startTime;
    private static long stopTime;

    public static void reset() {
        startTime = 0;
        stopTime = 0;
    }

    public static void start() {
        startTime = System.nanoTime();
    }

    public static void stop() {
        stopTime = System.nanoTime();
    }

    public static double getTimeInSec() {
        long elapsedTime = stopTime - startTime;
        return (double)elapsedTime / 1_000_000_000;
    }

    public static long getTimeInMin() {
        long elapsedTime = stopTime - startTime;
        return TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }
}
