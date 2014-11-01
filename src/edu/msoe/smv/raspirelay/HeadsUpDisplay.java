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
import android.os.SystemClock;
import android.widget.TextView;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HeadsUpDisplay extends Activity {

    public TextView console;
    private ServerSocket serverSocket;
    private long startTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headsupdisplay);

        startTime = SystemClock.currentThreadTimeMillis();
//        String hostName = "155.92.179.102";

        console = (TextView)findViewById(R.id.console);

        AsyncTask task = new ServerAsyncTask();
        // TODO: unchecked call
        task.execute(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void updateView(String line) {
        console.append(line + "\n");
    }

    private class ServerAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(final Object[] params) {
            final HeadsUpDisplay display = ((HeadsUpDisplay)params[0]);
            String line = "This fucker is null";
            try {
                serverSocket = new ServerSocket(1111);
                Socket echoSocket = serverSocket.accept();
                if (echoSocket == null) {
                    updateView("Socket connection failed");
                } else {
                    while (SystemClock.currentThreadTimeMillis() < startTime + 30000) {
                        line = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())).readLine();
                        // line can be null for end of message, let's fix that and
                        final String output  = line = line != null ? line : "";
                        // update ui
                        if (!line.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    display.updateView(output);
                                }
                            });
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}