/**
 * @author austin
 * @version 1.2
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
import java.util.Locale;

import edu.msoe.smv.service.LapTimerService;
import edu.msoe.smv.service.VehicleConnectionService;
import edu.msoe.smv.utility.Utility;
import edu.msoe.smv.view.GoldilocksBar;
import edu.msoe.smv.view.VerticalGoldilocksBar;

/**
 * @see VehicleConnectionService
 */
public class HeadsUpDisplay extends Activity {

    private TextView mphLabel;
    private TextView rpmLabel;
    private TextView engineTempLabel;
    private SeekBar engineTempBar;

    private TextView totalTimeLabel;
    private TextView currentLapTimeLabel;

    private Thread timerThread;
    private boolean isRunning = false;
    private long startTime;
    private long lapStartTime;

    private ServiceReceiver serviceReceiver = new ServiceReceiver(new Handler());

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
                    } else if (resultCode == LapTimerService.LAP) {
                        lapStartTime = System.currentTimeMillis();
                        timerThread.interrupt();
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
        }
    }

    private final DecimalFormat doubleFormatter = new DecimalFormat("0.0");

    public void updateRPM(double rpm) {
        rpmLabel.setText(doubleFormatter.format(rpm));
    }

    public void updateSpeed(double speed) {
        mphLabel.setText(doubleFormatter.format(speed));
    }

    public void updateEngineTemp(double engineTemp) {
        String engineTempText = doubleFormatter.format(engineTemp) + "  \u00B0F";
        engineTempLabel.setText(engineTempText);
        engineTempBar.setProgress((int) (engineTemp / 200.0 * 100.0));
    }

    public void updateLapTime(long millis) {
        currentLapTimeLabel.setText(formatMillisDiff(millis));
    }

    public void updateTotalTime(long millis) {
        totalTimeLabel.setText(formatMillisDiff(millis));
    }

    private String formatMillisDiff(long millis) {
        long minutes = millis / (60 * 1000) % 60;
        long seconds = millis / 1000 % 60;
        // invariant locale
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void startTimer() {
        startTime = lapStartTime = System.currentTimeMillis();
        isRunning = true;
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateLapTime(System.currentTimeMillis() - lapStartTime);
                            updateTotalTime(System.currentTimeMillis() - startTime);
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            }
        });
        timerThread.start();
    }

    private void stopTimer() {
        isRunning = false;
        try {
            timerThread.join();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    private void initializeActivity() {
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
    }

    //region Activity API Callbacks

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeActivity();

        mphLabel = (TextView) findViewById(R.id.speedLabel);
        rpmLabel = (TextView) findViewById(R.id.rpmLabel);
        engineTempLabel = (TextView) findViewById(R.id.engineTempLabel);
        totalTimeLabel = (TextView) findViewById(R.id.totalTime);
        currentLapTimeLabel = (TextView) findViewById(R.id.lapTime);
        if (findViewById(R.id.engineTempBar) instanceof GoldilocksBar) {
            engineTempBar = (GoldilocksBar) findViewById(R.id.engineTempBar);
        } else {
            engineTempBar = (VerticalGoldilocksBar) findViewById(R.id.engineTempBar);
        }

        final FloatingActionButton timerToggleButton = (FloatingActionButton) findViewById(R.id.startStopwatch);
        timerToggleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isRunning) {
                    timerToggleButton.setIcon(R.drawable.ic_media_stop);
                    startTimer();
                } else {
                    timerToggleButton.setIcon(R.drawable.ic_media_embed_play);
                    stopTimer();
                }
            }
        });

        Utility.startVehicleConnectionService(getApplicationContext(), serviceReceiver);
        Utility.startLapTimerService(getApplicationContext(), serviceReceiver);
    }

    @Override
    public void onDestroy() {
        Utility.stopVehicleConnectionService(getApplicationContext());
        Utility.stopLapTimerService(getApplicationContext());
        super.onDestroy();
    }

    //endregion
}