package utils;

import java.util.concurrent.TimeUnit;

public class TimeWatch {

    long start;

    private TimeWatch() {
        reset();
    }

    public static TimeWatch start() {
        return new TimeWatch();
    }

    public TimeWatch reset() {
        start = System.nanoTime();
        return this;
    }

    public long getElapsedTime() {
        long end = System.nanoTime();
        return end - start;
    }

    public long getElapsedTime(TimeUnit unit) {
        return unit.convert(getElapsedTime(), TimeUnit.NANOSECONDS);
    }
}