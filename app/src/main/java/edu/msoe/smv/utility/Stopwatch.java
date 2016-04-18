package edu.msoe.smv.utility;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Blake on 5/2/2015.
 */
public class Stopwatch {
    private long startTime = 0, lapStartTime = 0;
    private Runnable action;
    private boolean isRunning = false;
    private long sleepFor = 0;
    private List<Long> lapTimes;

    public Stopwatch(Runnable r, long sleepFor) {
        action = r;
        this.sleepFor = sleepFor;
        lapTimes = new LinkedList<>();
    }

    public Stopwatch() {

    }

    public void start() {
        startTime = System.currentTimeMillis();
        lapStartTime = System.currentTimeMillis();

        isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (action != null) {
                    while (isRunning) {
                        action.run();
                        try {
                            Thread.sleep(sleepFor);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public static String toTimeString(long t) {
        long ms = (t % 1000), s = (t / 1000) % 60, m = (t / 60000);
        return String.format("%02d:%02d", m, s);
    }

    public long getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }

    public long getCurrentLapTime() {
        return System.currentTimeMillis() - lapStartTime;

    }

    public void resetTimer() {
        startTime = 0;
        lapStartTime = 0;
        isRunning = false;
        lapTimes.clear();
    }

    public void pause() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long lap() {
        long lapTime = getCurrentLapTime();
        lapTimes.add(lapTime);
        lapStartTime = System.currentTimeMillis();
        return lapTime;
    }

    public List<Long> getLapTimes() {
        return lapTimes;
    }
}