package edu.msoe.smv.managers;

import android.app.Activity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import edu.msoe.smv.utility.Stopwatch;
import edu.msoe.smv.view.GoldilocksBar;

/**
 * Created by Blake on 3/19/2016.
 */
public class ViewManager {
    private static ViewManager mViewManager;
    private Activity context;
    private TextView mphLabel, rpmLabel, oxyLabel, engineTempLabel, totalTimeLabel, lapTimeLabel;
    private GoldilocksBar engineTempBar, oxygenBar;
    private Stopwatch mStopwatch, totalStopwatch;
    private boolean isGasCar = true;

    public static ViewManager getInstance() {
        if (mViewManager == null) {
            mViewManager = new ViewManager();
        }
        return mViewManager;
    }

    public void setContext(Activity context) {
        this.context = context;
    }

    //region ACCESSORS AND MUTATORS
    public ViewManager setMphLabel(TextView mphLabel) {
        this.mphLabel = mphLabel;
        return this;
    }

    public ViewManager setRpmLabel(TextView rpmLabel) {
        this.rpmLabel = rpmLabel;
        return this;
    }

    public ViewManager setEngineTempBar(GoldilocksBar engineTempBar) {
        this.engineTempBar = engineTempBar;
        return this;
    }

    public ViewManager setOxygenBar(GoldilocksBar oxygenBar) {
        this.oxygenBar = oxygenBar;
        return this;
    }

    public ViewManager setIsGasCar(boolean isGasCar) {
        this.isGasCar = isGasCar;
        return this;
    }

    public ViewManager setEngineTempLabel(TextView engineTempLabel) {
        this.engineTempLabel = engineTempLabel;
        return this;
    }

    public ViewManager setOxyLabel(TextView oxyLabel) {
        this.oxyLabel = oxyLabel;
        return this;
    }

    public ViewManager setTotalTimeLabel(TextView totalTimeLabel) {
        this.totalTimeLabel = totalTimeLabel;
        return this;
    }

    public ViewManager setLapTimeLabel(TextView lapTimeLabel) {
        this.lapTimeLabel = lapTimeLabel;
        return this;
    }
    //endregion

    private ViewManager() {
        totalStopwatch = new Stopwatch();
        mStopwatch = new Stopwatch(new Runnable() {
            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (totalTimeLabel != null) {
                            totalTimeLabel.setText(Stopwatch.toTimeString(totalStopwatch.getDuration()));
                        }
                        if (lapTimeLabel != null) {
                            lapTimeLabel.setText(Stopwatch.toTimeString(mStopwatch.getDuration()));
                        }
                    }
                });
            }
        }, 71);
    }

    public void updateData(final JSONObject o) {
        if (o == null) {
            return;
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mphLabel != null) {

                    try {
                        mphLabel.setText("" + o.getDouble("speed"));
                    } catch (JSONException ignored) {
                    }
                }
                if (rpmLabel != null) {
                    try {
                        rpmLabel.setText("" + o.getDouble("rpm"));
                    } catch (JSONException ignored) {
                    }
                }
                if (isGasCar) {
                    if (oxygenBar != null) {
                        try {
                            oxygenBar.setProgress((int) o.getDouble("oxy"));
                        } catch (JSONException ignored) {
                        }
                    }
                    if (oxyLabel != null) {
                        try {
                            oxyLabel.setText(o.getDouble("oxy") + " %");
                        } catch (JSONException ignored) {
                        }
                    }
                    if (engineTempLabel != null) {
                        try {
                            engineTempLabel.setText(o.getDouble("temp") + " °F");
                        } catch (JSONException ignored) {
                        }
                    }
                    if (engineTempBar != null) {
                        try {
                            engineTempBar.setProgress((int) o.getDouble("temp"));
                        } catch (JSONException ignored) {
                        }
                    }
                }
            }
        });

    }

    public void startTimer() {
        mStopwatch.start();
        totalStopwatch.start();
    }

    public void pauseTimer() {
        mStopwatch.pause();
        totalStopwatch.pause();
    }

    public void doLapTimer() {
        mStopwatch.lap();
    }

    public void getLapTimes() {
        mStopwatch.getLapTimes();
    }
}
