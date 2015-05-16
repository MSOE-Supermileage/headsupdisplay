/**
 * Project: Raspberry Pi Data Hub
 * Date: 10/25/14
 */

package edu.msoe.smv.raspirelay;

/**
 * @author austin
 * @version 0.0001
 */

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.format.Formatter;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * the main activity
 */
public class HeadsUpDisplay extends Activity {

    private static final DecimalFormat speedFormatter = new DecimalFormat("0.0");


    private Stopwatch lapWatch, totalWatch;
    public static LinkedList<Long> laptimes = new LinkedList<>();
    int laps = 0;

    // layout references
    private TextView console;
    private TextView mphLabel;
    private Button lapButton;

    private static double currentSpeed = 99.0;

    /**
     * create the service connection when the framework builds this class
     *
     * Generic service connection
     */
    private ServiceConnection connection;

    private ServiceReceiver resultReceiver = new ServiceReceiver(new Handler());

    /**
     * Handle for services to update the UI (message receiver)
     */
    public class ServiceReceiver extends ResultReceiver {

        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler null
         */
        public ServiceReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultCode == VehicleConnectionService.MESSAGE_CODE) {
                        Toast.makeText(getApplicationContext(),
                                resultData.getString(VehicleConnectionService.MESSAGE),
                                Toast.LENGTH_SHORT).show();

                    } else if (resultCode == VehicleConnectionService.DATA_NODE_CODE) {
                        String data = resultData.getString(VehicleConnectionService.DATA_NODE);
                        Log.i("DATA_NODE", data);
                        try {
                            currentSpeed = Double.parseDouble(data);
                            mphLabel.setText(speedFormatter.format(currentSpeed));
                        } catch (Exception e) {
                            // we got some invalid data, return
                            Log.e("DATA_NODE", "invalid speed format: " + data);
                        }
                    } else if (resultCode == VehicleConnectionService.PI_CONNECTED_CODE) {
                        // parse state of whether or not pi is connected
                        boolean connected = resultData.getBoolean(VehicleConnectionService.PI_CONNECTED);

                        // draw it on the gui
                        if (lapButton != null) {
                            // green if connected, red if not
                            lapButton.setBackgroundColor(connected ? 0xf700ff00 : 0xf7ff0000);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set the layout
        setContentView(R.layout.headsupdisplay);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        console = (TextView) findViewById(R.id.console);
        mphLabel = (TextView) findViewById(R.id.mphLbl);
        lapButton = (Button) findViewById(R.id.lapBtn);

        startVehicleConnectionService();

        lapWatch = new Stopwatch(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGUI();
                    }
                });
            }
        }, 100);
        totalWatch = new Stopwatch();

        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                updateConsole(line);
                Log.e("arp", line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startVehicleConnectionService() {
        Log.d("debug", "starting vehicle service...");
        // Create the vehicle connection service
        Intent serviceBindingIntent = new Intent(getBaseContext(), VehicleConnectionService.class);
        serviceBindingIntent.putExtra("receiver", resultReceiver);
        startService(serviceBindingIntent);

        /*
         * bind the service to the activity - only for bound services. We want a started service that
         * starts when the application starts and continues in the background, and allows multiple clients.
         */
//        bindService(serviceBindingIntent, connection, Context.BIND_AUTO_CREATE);

        Log.d("debug", "service started");
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        // stop the vehicle connection service
        Intent stopIntent = new Intent(getBaseContext(), VehicleConnectionService.class);
        stopService(stopIntent);
        super.onDestroy();
    }

    /**
     * update the console text display view with the specified text
     *
     * @param line the line of text to write to the console
     */
    public void updateConsole(String line) {
        try {
            console.append(line + "\n");
        } catch (Exception e) {
            console.append(e.toString());
        }
    }

    public void startClick(View v) {
        if(!lapWatch.isRunning()) {
            lapWatch.start();
            totalWatch.start();
            laps = 0;
            ((TextView) findViewById(R.id.numLapsLbl)).setText("" + laps);
        }
    }

    public void stopClick(View v) {
        lapWatch.stop();
        totalWatch.stop();
        updateGUI();
        laps = 0;
    }

//    public void pauseClick(View v) {
//        lapWatch.pause();
//        totalWatch.pause();
//        updateGUI();
//    }

    private void updateGUI() {
        String lap = Stopwatch.toTimeString(lapWatch.getDuration()), total = Stopwatch.toTimeString(totalWatch.getDuration());
        ((TextView) findViewById(R.id.currLapTimeLbl)).setText(lap.substring(3, lap.length() - 2));
        ((TextView) findViewById(R.id.totalTimeLbl)).setText("Total: " + total.substring(0, total.length() - 2));
//        ((TextView) findViewById(R.id.numLapsLbl)).setText("" + laps);
    }

    public void lapClick(View v) {
        if(lapWatch.isRunning()) {
            laptimes.add(lapWatch.lap());
            laps++;
            ((TextView) findViewById(R.id.numLapsLbl)).setText("" + laps);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i=new Intent(this,LapTimesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}