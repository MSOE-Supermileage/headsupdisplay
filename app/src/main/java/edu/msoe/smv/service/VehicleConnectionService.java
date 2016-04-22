package edu.msoe.smv.service;

import android.app.Service;
import android.content.Intent;
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
        Toast.makeText(getApplicationContext(), "attempting to connect to DAQ...", Toast.LENGTH_LONG).show();

    }

    /**
     * android API callback #2 - start the service
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            final WebPublisher webPublisher = new WebPublisher();
            final LogFilePublisher logFilePublisher = new LogFilePublisher();

            MasterPublisher masterPublisher = new MasterPublisher(
                    intent.<ResultReceiver>getParcelableExtra("receiver"), webPublisher, logFilePublisher);

            if (vehicleConnectionThread == null) {
                vehicleConnectionThread = new VehicleConnectionThread(masterPublisher);
            } else if (!vehicleConnectionThread.isAlive()) {
                // if the thread died, recreate the instance.
                if (vehicleConnectionThread.getState() == Thread.State.TERMINATED) {
                    vehicleConnectionThread = new VehicleConnectionThread(masterPublisher);
                }
            }
            vehicleConnectionThread.start();

        } catch (IOException e) {
            Log.e(WebPublisher.class.getName(), e.getMessage());
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "detaching from DAQ...", Toast.LENGTH_LONG).show();
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