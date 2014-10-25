/**
 * Project: Raspberry Pi Data Hub
 * Date: 10/25/14
 */

package edu.msoe.smv.raspirelay;

/**
 * @author austin
 * @version 0.0001
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HeadsUpDisplay extends Activity {

    public TextView console;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headsupdisplay);

//        String hostName = "155.92.179.102";

        console = (TextView)findViewById(R.id.console);

        AsyncTask task = new taskThing();
        // TODO: unchecked call
        task.execute(this);

    }

    public void updateView(String line) {
        console.append(line + "\r\n");
    }

    // TODO hacky garbage
    private class taskThing extends AsyncTask {

        @Override
        protected Object doInBackground(final Object[] params) {
            final HeadsUpDisplay display = ((HeadsUpDisplay)params[0]);
            ServerSocket me;
            String line = "";
            try {
                me = new ServerSocket(1111);
                Socket echoSocket = me.accept();
                while (!line.contains("quit")) {
                    final String output = line = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())).readLine();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            display.updateView(output);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}