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
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * the main activity
 */
public class HeadsUpDisplay extends Activity {
    private Stopwatch lapWatch, totalWatch;
    public static LinkedList<Long> laptimes=new LinkedList<>();
    int laps = 0;

    private final Gson gson = new Gson();

    // layout references
    public TextView console;

    /**
     * create the service connection when the framework builds this class
     * <p/>
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
                    if (resultCode == 100) {
                        Toast.makeText(getApplicationContext(), resultData.getString("message"), Toast.LENGTH_SHORT).show();
                    } else if (resultCode == 200) {
                        String data = resultData.getString("node");
                        Log.i("DATA_NODE", data);
                        edu.msoe.smv.raspi.DataNode node = gson.fromJson(data, edu.msoe.smv.raspi.DataNode.class);
                        Toast.makeText(getApplicationContext(), Double.toString(node.getRpm()), Toast.LENGTH_SHORT).show();
                    } else if (resultCode == 300) {
                        String data = resultData.getString("count");
                    }
                }
            });
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headsupdisplay);

        console = (TextView) findViewById(R.id.console);

        // get public IP from Wifi Manager
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        updateConsole("my ip: " + ip);

        // Create the vehicle connection service
        Intent serviceBindingIntent = new Intent(getBaseContext(), VehicleConnectionService.class);
        serviceBindingIntent.putExtra("receiver", resultReceiver);
        startService(serviceBindingIntent);

        /*
         * bind the service to the activity - only for bound services. We want a started service that
         * starts when the application starts and continues in the background, and allows multiple clients.
         */
//        bindService(serviceBindingIntent, connection, Context.BIND_AUTO_CREATE);

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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     */
    @Override
    public void onDestroy() {
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
            System.out.println(console);
            console.append(e.toString());
        }
    }

    public void startClick(View v) {
        if(!lapWatch.isRunning()) {
            lapWatch.start();
            totalWatch.start();
        }
    }

    public void stopClick(View v) {
        lapWatch.stop();
        totalWatch.stop();
        updateGUI();
        laps=0;
    }

    public void pauseClick(View v) {
        lapWatch.pause();
        totalWatch.pause();
        updateGUI();
    }

    private void updateGUI() {
        String lap = Stopwatch.toTimeString(lapWatch.getDuration()), total = Stopwatch.toTimeString(totalWatch.getDuration());
        ((TextView) findViewById(R.id.currLapTimeLbl)).setText(lap.substring(3, lap.length() - 2));
        ((TextView) findViewById(R.id.totalTimeLbl)).setText("Total: " + total.substring(0, total.length() - 2));
        ((TextView) findViewById(R.id.numLapsLbl)).setText("" + laps);
        ((TextView) findViewById(R.id.mphLbl)).setText(""+ getMPH());
    }

    private int getMPH() {
        return 99;
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