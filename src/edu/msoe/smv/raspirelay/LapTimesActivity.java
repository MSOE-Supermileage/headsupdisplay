package edu.msoe.smv.raspirelay;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.util.List;

/**
 * Created by Blake on 5/2/2015.
 */
public class LapTimesActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laptime_activity);
        List<Long> times=HeadsUpDisplay.laptimes;
        LinearLayout list=(LinearLayout)findViewById(R.id.timeList);
        int i=0;
        for(Long l:times){
            i++;
            TimeView t=new TimeView(this);
            t.setText("Lap " + i + ":   " + Stopwatch.toTimeString(l));
            list.addView(t);
        }
    }
    private class TimeView extends TextView{

        public TimeView(Context context) {
            super(context);
            this.setTextSize(TypedValue.COMPLEX_UNIT_DIP,25);
        }
    }
}