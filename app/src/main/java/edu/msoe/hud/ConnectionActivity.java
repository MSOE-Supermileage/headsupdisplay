package edu.msoe.hud;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import edu.msoe.hud.RaspberryPi.ConnectionThread;

public class ConnectionActivity extends AppCompatActivity {
    //The port that is forwarded by ADB, the pi connects to us through this port.
    private static final int PORT = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
    }

}
