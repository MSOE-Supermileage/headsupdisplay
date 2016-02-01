package edu.msoe.smv;

import android.app.Activity;
import android.os.Bundle;

public class ConnectionActivity extends Activity {
    //The port that is forwarded by ADB, the pi connects to us through this port.
    private static final int PORT = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
    }
}
