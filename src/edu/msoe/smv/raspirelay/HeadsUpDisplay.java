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
import android.view.View;
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
    public void sendData_onclick(View v){
        try{
            sendDataToWebServer("hello there");
            updateView("Sent");
        }catch(Exception e){
            updateView(e.toString());
        }
    }
    public void sendDataToWebServer(String data) throws IOException {
        if (data == null) {
            updateView("data null");
        }
        outstream.write(data);
        outstream.flush();
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
    BufferedWriter outstream;
    ServerSocket websock = null;
    Socket sock=null;
    public void updateView(String line) {
        try {
            console.append(line + "\n");
        }catch (Exception e){
            System.out.println(console);
            console.append(e.toString());
        }
    }
    public void instantiateWriter(OutputStream stream){
        outstream = new BufferedWriter(new OutputStreamWriter(stream));
    }
    public class ServerAsyncTask extends AsyncTask {
        public void updateUI(final String s){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(s);
                }
            });
        }
        @Override
        public Object doInBackground(final Object... params) {
            final HeadsUpDisplay display = ((HeadsUpDisplay)params[0]);
            String line = "This fucker is null";
            try {
                websock = new ServerSocket(1112);
                sock = websock.accept();
                updateUI("PitView v1.0");
                updateUI("Connected to PitView (IP=" + sock.getRemoteSocketAddress().toString().substring(1) + ":1112)");
                instantiateWriter(sock.getOutputStream());
                String outstreamdata = "faux data\r\n";
                sendDataToWebServer("testing web server1", outstream);
                //outstream.flush();
                /*outstream.write(outstreamdata);
                outstream.flush();
                outstream.write(outstreamdata);
                outstream.flush();
                outstream.flush();
                outstream.write(outstreamdata);
                outstream.flush();
                outstream.write(outstreamdata);*/

                serverSocket = new ServerSocket(1111);
                Socket echoSocket = serverSocket.accept();
                if (echoSocket == null) {
                    updateUI("Socket connection failed");
                } else {
                    while (SystemClock.currentThreadTimeMillis() < startTime + 30000) {
                        line = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())).readLine();
                        // line can be null for end of message, let's fix that and
                        final String output = line = line != null ? line : "";

                        // update ui
                        if (!line.isEmpty()) {
                            updateUI(output);
                            outstreamdata += output;
                        }
                    }
                    updateUI("done receiving - now sent to blake");
                }
                // send to web server
                outstream.write((outstreamdata + "\r\n"));
                outstream.flush();

                sendDataToWebServer("testing web server",outstream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        public void sendDataToWebServer(String data,BufferedWriter stream) throws IOException {
            if (data == null) {
                updateUI("data null");
            }
            stream.write(data);
            stream.flush();
        }
    }
}