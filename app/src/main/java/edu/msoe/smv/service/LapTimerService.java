package edu.msoe.smv.service;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * A service to manage lap times using GPS and report to a subscribing activity
 */
public class LapTimerService extends Service {

    public static final long MIN_LAP_TIME_SECONDS = 15;
    public static final int LAP = 1000;

    private Thread timerThread;

    private boolean runTimer = false;

    final LocationListener locationListener = new LocationListener() {

        private Location startLocation = null;
        private Thread timeoutThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(MIN_LAP_TIME_SECONDS * 1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        });

        private void lap() {

        }

        @Override
        public void onLocationChanged(Location location) {
            if (startLocation != null) {
                if (!timeoutThread.isAlive() && startLocation.distanceTo(location) > 50.0) {
                    lap();
                }
            } else {
                startLocation = location;
                timeoutThread.start();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };



    @Override
    public void onCreate() {
        timerThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while (runTimer) {
                    // collect current GPS location

                    // on GPS update

                    // check if approaching initial location
                    // compute the gradient of change, seek local minim
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runTimer = true;
        timerThread.start();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        runTimer = false;
        try {
            timerThread.join();
        } catch (InterruptedException e) {
            Thread.interrupted();
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }
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
}
