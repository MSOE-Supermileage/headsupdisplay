package edu.msoe.smv.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * We need to implement a service decoupled from the activity so it can run asynchronously in the background as
 * a separate process if the activity fails or crashes.
 *
 * @author austin
 * @version 2014.12.07
 */
public class VehicleConnectionService extends Service {

    public static final String MESSAGE = "message";
    public static final int MESSAGE_CODE = 100;
    public static final String DATA_NODE = "node";
    public static final int DATA_NODE_CODE = 200;
    public static final String PI_CONNECTED = "connected";
    public static final int PI_CONNECTED_CODE = 300;

    /**
     * the background thread for communicating with the raspberry pi
     */
    private Thread vehicleConnectionThread;

    //region Android API Service Callbacks

    /**
     * android API callback #1 - create the service, but do not start
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {

    }

    /**
     * android API callback #2 - start the service
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            final Handler handler;
            handler = new DataNodeHandler();

            ReceiverNotifier notifier = new ReceiverNotifier(
                    intent.<ResultReceiver>getParcelableExtra("receiver"), handler);

            if (vehicleConnectionThread == null) {
                vehicleConnectionThread = new VehicleConnectionThread(notifier);

            } else if (!vehicleConnectionThread.isAlive()) {
                // if the thread died, recreate the instance.
                if (vehicleConnectionThread.getState() == Thread.State.TERMINATED) {
                    vehicleConnectionThread = new VehicleConnectionThread(notifier);
                }
                vehicleConnectionThread.start();
            }

        } catch (IOException e) {
            Log.e(DataNodeHandler.class.getName(), e.getMessage());
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        while (vehicleConnectionThread != null && vehicleConnectionThread.isAlive()) {
            vehicleConnectionThread.interrupt();
            try {
                vehicleConnectionThread.join();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(
                getApplicationContext(),
                "Service attempted to be bound to - exiting...",
                Toast.LENGTH_LONG
        ).show();
        throw new UnsupportedOperationException("do not bind to this service.");
    }

    //endregion
}