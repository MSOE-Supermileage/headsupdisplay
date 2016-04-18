/**
 * @author austin
 * @version 1.01
 */
package edu.msoe.smv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;

import edu.msoe.smv.managers.ViewManager;
import edu.msoe.smv.service.VehicleConnectionService;
import edu.msoe.smv.utility.Utility;
import edu.msoe.smv.view.GoldilocksBar;
import edu.msoe.smv.view.VerticalGoldilocksBar;

/**
 * Example Activity from legacy DAQ to show how to use VehicleConnectionService
 *
 * @see VehicleConnectionService
 */
public class HeadsUpDisplay extends Activity {

    private TextView mphLabel;
    private TextView rpmLabel;
    private TextView engineTempLabel;
    private SeekBar engineTempBar;
//    private TextView airFuelRatioText;
//    private GoldilocksBar airFuelRatioBar;

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
                        updateView(resultData);
                    }
                }
            });
        }

        private void updateView(Bundle resultData) {
            if (resultData.containsKey("speed")) {
                updateSpeed(resultData.getDouble("speed"));
            }
            if (resultData.containsKey("engine_rpm")) {
                updateRPM(resultData.getDouble("engine_rpm"));
            }
            if (resultData.containsKey("engine_temp")) {
                updateEngineTemp(resultData.getDouble("engine_temp"));
            }
//            if (resultData.containsKey("afr")) {
//                updateAirFuelRatio(resultData.getDouble("afr"));
//            }
        }
    }

    private final DecimalFormat doubleFormatter = new DecimalFormat("0.0");

    private void updateRPM(double rpm) {
        rpmLabel.setText(doubleFormatter.format(rpm));
    }

    private void updateSpeed(double speed) {
        mphLabel.setText(doubleFormatter.format(speed));
    }

    private void updateEngineTemp(double engineTemp) {
        String engineTempText = doubleFormatter.format(engineTemp) + "  \u00B0F";
        engineTempLabel.setText(engineTempText);
        engineTempBar.setProgress((int) (engineTemp / 200.0 * 100.0));
    }

//    private void updateAirFuelRatio(double afr) {
//        String afrText = doubleFormatter.format(afr) + "%";
//        airFuelRatioText.setText(afrText);
//        airFuelRatioBar.setProgress((int) afr);
//    }

    //region Activity API Callbacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set the layout
        setContentView(R.layout.activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        mphLabel = (TextView) findViewById(R.id.speedLabel);
        rpmLabel = (TextView) findViewById(R.id.rpmLabel);
        engineTempLabel = (TextView) findViewById(R.id.engineTempLabel);

        if (findViewById(R.id.engineTempBar) instanceof GoldilocksBar) {
            engineTempBar = (GoldilocksBar) findViewById(R.id.engineTempBar);
        } else {
            engineTempBar = (VerticalGoldilocksBar) findViewById(R.id.engineTempBar);
        }

//        airFuelRatioBar = (GoldilocksBar) findViewById(R.id.temp2);
//        airFuelRatioText = (TextView) findViewById(R.id.oxygenPercLabel);

        TextView laptime = (TextView) findViewById(R.id.lapTime);
        TextView tottime = (TextView) findViewById(R.id.totalTime);
        ViewManager v = ViewManager
                .getInstance()
                .setLapTimeLabel(laptime)
                .setTotalTimeLabel(tottime);
        v.setContext(this);

        initFABMenu();
        Utility.startVehicleConnectionService(getApplicationContext(), resultReceiver);
    }

    public void initFABMenu() {
        final FloatingActionButton startStopwatch = (FloatingActionButton) findViewById(R.id.startStopwatch);
        startStopwatch.setOnClickListener(new OnClickListener() {
            private boolean isRunning = false;

            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    ViewManager.getInstance().startTimer();
                    startStopwatch.setIcon(R.drawable.ic_media_pause);
                    startStopwatch.setColorNormal(R.color.yellow);
                    isRunning = true;
                } else {
                    ViewManager.getInstance().pauseTimer();
                    startStopwatch.setIcon(R.drawable.ic_media_embed_play);
                    startStopwatch.setColorNormal(R.color.green);
                    isRunning = false;
                }
            }
        });
        FloatingActionButton resetStopwatch = (FloatingActionButton) findViewById(R.id.resetStopwatch);
        resetStopwatch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewManager.getInstance().resetTimer();
                ((TextView) findViewById(R.id.totalTime)).setText(getString(R.string.zero_time));
                ((TextView) findViewById(R.id.lapTime)).setText(getString(R.string.zero_time));
            }
        });
        FloatingActionButton lapStopwatch = (FloatingActionButton) findViewById(R.id.lapStopwatch);
        lapStopwatch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                long lapTime = ViewManager.getInstance().lap();
                ((TextView) findViewById(R.id.lapTime)).setText(getString(R.string.zero_time));
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