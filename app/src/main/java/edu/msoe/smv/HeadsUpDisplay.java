/**
 * @author austin
 * @version 1.01
 */
package edu.msoe.smv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.text.DecimalFormat;

import edu.msoe.smv.service.VehicleConnectionService;

/**
 * Example Activity from legacy DAQ to show how to use VehicleConnectionService
 *
 * @see VehicleConnectionService
 */
public class HeadsUpDisplay extends Activity {

    private TextView mphLabel;

    private ServiceReceiver resultReceiver = new ServiceReceiver(new Handler());

    /**
     * Handle for services to update the UI (message receiver)
     */
    @SuppressLint("ParcelCreator")
    // API change in android 6.0 lints public member "CREATOR" - what are you doing google?
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

        private final DecimalFormat speedFormatter = new DecimalFormat("0.0");

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
                            double currentSpeed = Double.parseDouble(data != null ? data : "0.0");
                            mphLabel.setText(speedFormatter.format(currentSpeed));
                        } catch (Exception e) {
                            // we got some invalid data, return
                            Log.e("DATA_NODE", "invalid speed format: " + data);
                        }
                    }
                }
            });
        }
    }

    //region Activity API Callbacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set the layout
        setContentView(R.layout.activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mphLabel = (TextView) findViewById(R.id.speedLabel);

        initFABMenu();
    }
    public void initFABMenu(){
        final FloatingActionsMenu fab = (FloatingActionsMenu) findViewById(R.id.fab);
        FloatingActionButton startbtn = (FloatingActionButton) findViewById(R.id.startFAB);
        startbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.startVehicleConnectionService(getApplicationContext(), resultReceiver);
                fab.toggle();
            }
        });

        FloatingActionButton stopbtn = (FloatingActionButton) findViewById(R.id.stopFAB);
        stopbtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.stopVehicleConnectionService(getApplicationContext());
                fab.toggle();

            }
        });
        FloatingActionButton startStopwatch = (FloatingActionButton) findViewById(R.id.stopFAB);
        startStopwatch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onDestroy() {
        Utility.stopVehicleConnectionService(getApplicationContext());
        super.onDestroy();
    }

    //endregion


}