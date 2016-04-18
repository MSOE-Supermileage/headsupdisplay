package edu.msoe.smv.service;

/**
 * Created by austin on 1/31/16.
 */
public class ConnectionThreadBase extends Thread {

    private static final int MIN_ERROR_PERIOD_MILLIS = 500;

    protected MasterPublisher masterPublisher;

    // unix time
    private long lastErrorTime = 0;

    public ConnectionThreadBase(MasterPublisher masterPublisher) {
        this.masterPublisher = masterPublisher;
        lastErrorTime = System.currentTimeMillis();
    }

    //region Helpers

    /**
     * check to ensure the current error time isn't too soon since the last error
     * if it is, then wait a bit
     * this is mostly to avoid spinlock, which can consume a bunch of resources
     * constantly requesting tcp connections and whatever
     */
    protected void connectionError(String message) {
        if (System.currentTimeMillis() - lastErrorTime >= MIN_ERROR_PERIOD_MILLIS) {
            lastErrorTime = System.currentTimeMillis();
//            masterPublisher.postMessage(message);
            masterPublisher.notifyDataCollectorConnectedStatus(false);
        } else {
            try {
                Thread.sleep(MIN_ERROR_PERIOD_MILLIS);
            } catch (InterruptedException e) {
//                Thread.interrupted();
            }
        }
    }

    //endregion Helpers
}
