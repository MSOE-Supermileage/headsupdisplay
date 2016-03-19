package edu.msoe.smv.service;

import java.util.LinkedList;

/**
 * Created by Blake on 5/2/2015.
 */
public class Stopwatch {
    private long startTime = 0, duration = 0;
    private Runnable action;
    private boolean isRunning = false;
    private long sleepFor = 0;
    private LinkedList<Long> lapTimes;

    public Stopwatch(Runnable r, long sleepFor) {
        action = r;
        this.sleepFor = sleepFor;
        lapTimes =new LinkedList<>();
    }

    public Stopwatch() {
    }

    public void start() {
        startTime = System.currentTimeMillis();

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

    private long getCurrentTime() {
        return System.currentTimeMillis() - startTime;
    }

    public static String toTimeString(long t) {
        long ms = (t % 1000), s = (t / 1000) % 60, m = (t / 60000) % 60, h = t / 3600000;
        return String.format("%02d:%02d:%02d.%03d", h, m, s, ms);
    }

    public long getDuration() {
        return isRunning ? duration + getCurrentTime() : duration;
    }

    public void stop() {
        if(isRunning) {
            duration = 0;
        }
        isRunning = false;
    }

    public void pause() {
        if(isRunning) {
            duration += getCurrentTime();
        }
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public long lap() {
        long res=getDuration();
        startTime = System.currentTimeMillis();
        duration=0;
        lapTimes.add(res);
        return res;
    }

    public LinkedList<Long> getLapTimes() {
        return lapTimes;
    }
}