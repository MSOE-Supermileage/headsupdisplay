package edu.msoe.smv.service;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by austin on 1/31/16.
 */
public class ConnectionThreadBase extends Thread {

    private static final int MIN_ERROR_PERIOD_MILLIS = 500;

    protected ReceiverNotifier notifier;

    // unix time
    private long lastErrorTime = 0;

    private Queue<Long> times = new LinkedList<>();

    public ConnectionThreadBase(ReceiverNotifier notifier) {
        this.notifier = notifier;
        lastErrorTime = System.currentTimeMillis();
    }

    //region Helpers

    /**
     * check to ensure the current error time isn't too soon since the last error
     * if it is, then wait a bit
     */
    protected void connectionError(String message) {
        if (System.currentTimeMillis() - lastErrorTime >= MIN_ERROR_PERIOD_MILLIS) {
            lastErrorTime = System.currentTimeMillis();
            notifier.postMessage(message);
            notifier.notifyDataCollectorConnectedStatus(false);
        } else {
            try {
                Thread.sleep(MIN_ERROR_PERIOD_MILLIS);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    protected void trackSocketTime(long time) {
        if (times.size() >= 100) {
            times.poll();
        }
        times.offer(time);
    }

    //endregion Helpers
}
