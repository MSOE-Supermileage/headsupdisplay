package edu.msoe.smv.RaspberryPi;

import android.widget.TextView;

import edu.msoe.smv.Calculations.Calculator;
import edu.msoe.smv.ConnectionActivity;
import edu.msoe.smv.R;

/**
 * Created by Connor on 10/16/2015.
 */
public class UIUpdater {
    private ConnectionActivity activity;

    private TextView mSpeedView;
    private TextView mRpmView;
    private TextView mLatencyView;

    public UIUpdater(ConnectionActivity activity) {
        this.activity = activity;

        declareElements();
    }

    /**
     * Declares the elements for the layout
     */
    private void declareElements() {
        mSpeedView = (TextView) activity.findViewById(R.id.speedLabel);
        mRpmView = (TextView) activity.findViewById(R.id.rpmLabel);
        //mLatencyView = (TextView) activity.findViewById(R.id.latency);
    }

    public void updateUI(final Calculator calculator) {
        mSpeedView.post(new Runnable() {
            @Override
            public void run() {
                mSpeedView.setText(Math.round(calculator.getSpeed()) + "");
            }
        });

        mRpmView.post(new Runnable() {
            @Override
            public void run() {
                mRpmView.setText(Math.round(calculator.getRpms()) + "");
            }
        });
    }
}