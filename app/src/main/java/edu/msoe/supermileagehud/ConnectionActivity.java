package edu.msoe.supermileagehud;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import edu.msoe.supermileagehud.RaspberryPi.ConnectionThread;

public class ConnectionActivity extends AppCompatActivity {
    //The port that is forwarded by ADB, the pi connects to us through this port.
    private static final int PORT = 5001;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        gestureDetector = new GestureDetector(this, new GestureListener());

        getWindow().getDecorView().getRootView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);

                return true;
            }
        });

        //Example of a floating button
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CloudConnection cloud = new CloudConnection();

                new ConnectionThread(ConnectionActivity.this, PORT).start();

                fab.hide();
            }
        });
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            //Long pressed, do something here!
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Double tapped, show settings screen!

            return true;
        }
    }
}
