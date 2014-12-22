/**
 * Project: AndroidRelay
 * Author: Austin Hartline
 * Date: 12/7/14
 */

package edu.msoe.smv.raspirelay;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * We need to implement a service, because threads & async tasks are not independent of activity.
 *
 * @author austin
 * @version 2014.12.07
 */
public class VehicleConnectionAgent extends Service {

    /**
     * This class will handle the vehicle connection.
     *
     * push a connection to the raspberry pi, start a heartbeat, in-stream data and route through
     * the OutboundDomain
     */

    private final IBinder localBinder = new VehicleConnectionServiceBinder();

    /**
     *
     */
    @Override
    public void onCreate() {
        // TODO: ?
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
        Log.i("@onStartCommand", "service started");
        Log.i("@intent: ", intent.toString());
        return START_STICKY;
    }

    /**
     *
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: ?
        return localBinder;
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
        Log.i("@onDestroy", "service stopped");
    }

    /**
     *
     */
    public class VehicleConnectionServiceBinder extends Binder {

        /**
         *
         * @return
         */
        public VehicleConnectionAgent getService() {
            Toast.makeText(VehicleConnectionAgent.this, "binding getService() call", Toast.LENGTH_SHORT).show();
            Log.i("@getService", "binding...");
            return VehicleConnectionAgent.this;
        }
    }
}
