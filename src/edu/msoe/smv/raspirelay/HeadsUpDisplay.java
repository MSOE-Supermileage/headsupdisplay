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
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Formatter;
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
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip  = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        updateView("my ip: " + ip);

        AsyncTask task = new ServerAsyncTask();
        // TODO: unchecked call
        task.execute(this);

    }

    public void sendDataToWebServer(String data) throws IOException {
        if (data == null) {
            updateView("data null");
        }
        Socket socket = new Socket("155.92.69.95", 8000);
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        outToServer.writeUTF("HI BLAKE!!!");
        outToServer.close();
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
                ServerSocket websock = new ServerSocket(1112);
                Socket sock = websock.accept();
                DataOutputStream outstream = new DataOutputStream(sock.getOutputStream());
                String outstreamdata = "faux data\r\n";
                outstream.writeUTF(outstreamdata);


                serverSocket = new ServerSocket(1111);
                Socket echoSocket = serverSocket.accept();
                if (echoSocket == null) {
                    updateView("Socket connection failed");
                } else {
                    while (SystemClock.currentThreadTimeMillis() < startTime + 30000) {
                        line = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())).readLine();
                        // line can be null for end of message, let's fix that and
                        final String output = line = line != null ? line : "";

                        // update ui
                        if (!line.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    display.updateView(output);
                                }
                            });
                            outstreamdata += output;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            display.updateView("done receiving - now sent to blake");
                        }
                    });
                }
                // send to web server
                outstream.writeUTF(outstreamdata + "\r\n");


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}