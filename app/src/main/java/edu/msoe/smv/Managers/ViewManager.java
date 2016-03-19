package edu.msoe.smv.Managers;

import android.widget.TextView;

import org.json.JSONObject;

import edu.msoe.smv.service.Stopwatch;
import edu.msoe.smv.view.GoldilocksBar;

/**
 * Created by Blake on 3/19/2016.
 */
public class ViewManager {
    private static ViewManager mViewManager;
    private TextView mphLabel,rpmLabel;
    private GoldilocksBar engineTempBar,oxygenBar;
    private Stopwatch mStopwatch;
    private boolean isGasCar=true;

    public static ViewManager getInstance() {
        if (mViewManager == null) {
            mViewManager = new ViewManager();
        }
        return mViewManager;
    }
    //region ACCESSORS AND MUTATORS


    public void setMphLabel(TextView mphLabel) {
        this.mphLabel = mphLabel;
    }

    public void setRpmLabel(TextView rpmLabel) {
        this.rpmLabel = rpmLabel;
    }

    public void setEngineTempBar(GoldilocksBar engineTempBar) {
        this.engineTempBar = engineTempBar;
    }

    public void setOxygenBar(GoldilocksBar oxygenBar) {
        this.oxygenBar = oxygenBar;
    }

    public void setIsGasCar(boolean isGasCar) {
        this.isGasCar = isGasCar;
    }

    //endregion

    public ViewManager() {

    }

    public void updateData(JSONObject o){
        if(isGasCar){

        }
    }
    public void startTimer(){
        mStopwatch.start();
    }
    public void pauseTimer(){
        mStopwatch.pause();
    }
    public void doLapTimer(){
        mStopwatch.lap();
    }
    public void getLapTimes(){
        mStopwatch.getLapTimes();
    }
}
