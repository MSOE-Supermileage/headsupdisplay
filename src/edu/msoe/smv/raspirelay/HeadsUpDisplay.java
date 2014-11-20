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
import android.text.format.Formatter;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class HeadsUpDisplay extends Activity {

    public TextView console;
    public CheckedTextView webConnected,piConnected;
    private ServerSocket piServerSocket,webServerSocket ;
    public WebListener myWebListener;
    public PiListener myPiListener;
    public boolean isPiDataServerRunning=true,isWebDataServerRunning=true;
    //private long startTime;
    BufferedWriter webServerOutput;
    Socket webServerDataSocket =null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headsupdisplay);


        //startTime = SystemClock.currentThreadTimeMillis();
//        String hostName = "155.92.179.102";

        console = (TextView)findViewById(R.id.console);
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip  = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        updateView("my ip: " + ip);
        webConnected=(CheckedTextView)findViewById(R.id.webConnected);
        piConnected=(CheckedTextView)findViewById(R.id.piConnected);

        /*AsyncTask task = new ServerAsyncTask();
        // TODO: unchecked call
        task.execute(this);*/

        /*myPiListener=new PiListener();
        myPiListener.execute();*/
    }
    public void startWebClick(View v){
        startWebServerListener();
    }
    public void stopWebClick(View v){
        stopWebServerListener();
    }
    public void startPiClick(View v){
        startPiListener();
    }
    public void stopPiClick(View v){
        stopPiListener();
    }
    public void startWebServerListener(){
        if(myWebListener!=null)
            stopWebServerListener();
        isWebDataServerRunning=true;
        myWebListener=new WebListener();
        myWebListener.execute();
    }
    public void stopWebServerListener(){
        try {
            webServerSocket.close();
            webServerOutput.close();
        }catch(Exception e){
            updateView("Error: "+e.toString());
        }
        isWebDataServerRunning=false;
        myWebListener=null;
        setWebConnected(false);
        updateView("--WebListener stopped--");
    }
    public void startPiListener(){
        if(myPiListener!=null)
            stopPiListener();
        isPiDataServerRunning=true;
        myPiListener=new PiListener();
        myPiListener.execute();
    }
    public void stopPiListener(){
        try {
            piServerSocket.close();
        }catch(Exception e){
            updateView("Error: "+e.toString());
        }
        isPiDataServerRunning=false;
        myPiListener=null;
        setPiConnected(false);
        updateView("--PiListener stopped--");
    }
    public void sendData_onclick(View v){
        try{
            sendDataToWebServer("hello there");
            updateView("Sent");
        }catch(Exception e){
            updateView(e.toString());
        }
    }
    public void setWebConnected(boolean val){
        webConnected.setChecked(val);
    }
    public void setPiConnected(boolean val){
        piConnected.setChecked(val);
    }
    public void sendDataToWebServer(String data) throws IOException {
        if (data == null) {
            updateView("data null");
        }
        /*webServerOutput.write(data);
        webServerOutput.flush();*/
        myWebListener.sendData(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (piServerSocket != null) {
            try {
                piServerSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void updateView(String line) {
        try {
            console.append(line + "\n");
        }catch (Exception e){
            System.out.println(console);
            console.append(e.toString());
        }
    }
    public void instantiateWriter(OutputStream stream){
        webServerOutput = new BufferedWriter(new OutputStreamWriter(stream));
    }
    public class WebListener extends AsyncTask{
        String pendingData="";
        public void sendData(String s){
            if(pendingData.equals(""))
                pendingData=s;
            else
                pendingData+="\n"+s;
        }
        public void updateUI(final String s){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(s);
                }
            });
        }
        public void updateConnected(final boolean val){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWebConnected(val);
                }
            });
        }
        public void bail(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopWebServerListener();
                }
            });
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try{
                updateUI("Starting WebListener");
                webServerSocket = new ServerSocket(1112);
                webServerDataSocket = webServerSocket.accept();
                updateConnected(true);
                updateUI("PitView v1.0");
                updateUI("Connected to PitView (IP=" + webServerDataSocket.getRemoteSocketAddress().toString().substring(1) + ":1112)");
                instantiateWriter(webServerDataSocket.getOutputStream());
                sendData("Connection initialized");
                /*String outstreamdata = "faux data\r\n";
                sendDataToWebServer("testing web server1", webServerOutput);*/
                while(isWebDataServerRunning){
                    if(!pendingData.equals("")) {
                        webServerOutput.write(pendingData);
                        webServerOutput.newLine();
                        webServerOutput.flush();
                        pendingData = "";
                    }
                }
            }catch(SocketException se) {
                updateUI("WebServer connection interrupted, shutting down...");
                bail();
            }catch(Exception e){
                updateUI("Error: "+e.toString());
            }
            return null;
        }
    }

    public class PiListener extends AsyncTask{
        public void updateUI(final String s){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(s);
                }
            });
        }
        public void updateConnected(final boolean val){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPiConnected(val);
                }
            });
        }
        public void bail(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopPiListener();
                }
            });
        }
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                updateUI("Starting PiListener");
                piServerSocket = new ServerSocket(1111);
                Socket echoSocket = piServerSocket.accept();
                updateConnected(true);
                if (echoSocket == null) {
                    updateUI("Socket connection failed");
                } else {
                    while (isPiDataServerRunning) {
                        String line= new BufferedReader(new InputStreamReader(echoSocket.getInputStream())).readLine();
                        // line can be null for end of message, let's fix that and
                        final String output = line = line != null ? line : "";

                        // update ui
                        if (!line.isEmpty()) {
                            updateUI(output);
                            myWebListener.sendData(output);
                        }
                    }
                    updateUI("done receiving - now sent to blake");
                }
            }catch(SocketException se) {
                updateUI("Pi connection interrupted, shutting down...");
                bail();
            }catch(Exception e){
                updateUI("Error: "+e.toString());
            }
            return null;
        }
    }

    /*---Below is old code---*/

    /*public class ServerAsyncTask extends AsyncTask {
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
                webServerSocket = new ServerSocket(1112);
                webServerDataSocket = webServerSocket.accept();
                updateUI("PitView v1.0");
                updateUI("Connected to PitView (IP=" + webServerDataSocket.getRemoteSocketAddress().toString().substring(1) + ":1112)");
                instantiateWriter(webServerDataSocket.getOutputStream());
                String outstreamdata = "faux data\r\n";
                sendDataToWebServer("testing web server1", webServerOutput);
                //webServerOutput.flush();*/
                /*webServerOutput.write(outstreamdata);
                webServerOutput.flush();
                webServerOutput.write(outstreamdata);
                webServerOutput.flush();
                webServerOutput.flush();
                webServerOutput.write(outstreamdata);
                webServerOutput.flush();
                webServerOutput.write(outstreamdata);*/
                /*
                piServerSocket = new ServerSocket(1111);
                Socket echoSocket = piServerSocket.accept();
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
                webServerOutput.write((outstreamdata + "\r\n"));
                webServerOutput.flush();

                sendDataToWebServer("testing web server", webServerOutput);
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
    }*/
}