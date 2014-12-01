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
        //call superclass method
        super.onCreate(savedInstanceState);

        //set the application display to the one we created
        setContentView(R.layout.headsupdisplay);


        //startTime = SystemClock.currentThreadTimeMillis();
//        String hostName = "155.92.179.102";

        //Text display for user notifications
        console = (TextView)findViewById(R.id.console);

        //get public IP from Wifi Manager
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip  = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        updateView("my ip: " + ip);

        //get connection status displays
        webConnected=(CheckedTextView)findViewById(R.id.webConnected);
        piConnected=(CheckedTextView)findViewById(R.id.piConnected);

        /*AsyncTask task = new ServerAsyncTask();
        // TODO: unchecked call
        task.execute(this);*/

        /*myPiListener=new PiListener();
        myPiListener.execute();*/
    }
    //onClick method for the startWebListener button
    public void startWebClick(View v){
        startWebServerListener();
    }
    //onClick method for the stopWebListener button
    public void stopWebClick(View v){
        stopWebServerListener();
    }
    //onClick method for the startPiListener button
    public void startPiClick(View v){
        startPiListener();
    }
    //onClick method for the stopPiListener button
    public void stopPiClick(View v){
        stopPiListener();
    }

    //starts a new instance of the Web Server Listener
    public void startWebServerListener(){
        //If the listener is running, stop it before creating another instance
        if(myWebListener!=null)
            stopWebServerListener();
        isWebDataServerRunning=true;
        //create a new listener object
        myWebListener=new WebListener();
        //start listening
        myWebListener.execute();
    }

    //stops the web server listener
    public void stopWebServerListener(){
        try {
            //close the sockets
            webServerSocket.close();
            webServerOutput.close();
        }catch(Exception e){
            updateView("Error: "+e.toString());
        }
        isWebDataServerRunning=false;
        myWebListener=null;
        //update the connected GUI
        setWebConnected(false);
        //output message
        updateView("--WebListener stopped--");
    }

    //starts a new instance of the Pi Listener
    public void startPiListener(){
        //if the listener is running, stop it before creating another instance
        if(myPiListener!=null)
            stopPiListener();
        isPiDataServerRunning=true;
        //create a new PI listener
        myPiListener=new PiListener();
        //start listening
        myPiListener.execute();
    }
    //stops the pi listener
    public void stopPiListener(){
        try {
            //close the sockets
            piServerSocket.close();
        }catch(Exception e){
            updateView("Error: "+e.toString());
        }
        isPiDataServerRunning=false;
        myPiListener=null;
        //update the connection GUI
        setPiConnected(false);
        //display a message
        updateView("--PiListener stopped--");
    }

    //onClick method for the sendMessage button
    public void sendData_onclick(View v){
        try{
            //add data to web server queue
            sendDataToWebServer("hello there");
            //display message
            updateView("Sent");
        }catch(Exception e){
            updateView(e.toString());
        }
    }
    //updates the connected GUI for the Web Server Listener to the specified value
    public void setWebConnected(boolean val){
        webConnected.setChecked(val);
    }
    //updates the connected GUI for the Pi Listener to the specified value
    public void setPiConnected(boolean val){
        piConnected.setChecked(val);
    }

    //adds DATA to the Web Server message queue
    public void sendDataToWebServer(String data) throws IOException {
        if (data == null) {
            //notify user of empty message
            updateView("data null");
        }
        //send DATA to message queue
        myWebListener.sendData(data);
    }

    //overrides the default application close event
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (piServerSocket != null) {
            try {
                //close the connections
                piServerSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    //update the console text display widget with the specified text
    public void updateView(String line) {
        try {
            //append the string to the text of the consoel
            console.append(line + "\n");
        }catch (Exception e){
            System.out.println(console);
            console.append(e.toString());
        }
    }

    //instantiates the webServerOutput Buffered Writer
    public void instantiateWriter(OutputStream stream){
        webServerOutput = new BufferedWriter(new OutputStreamWriter(stream));
    }

    //WebServer Listener Task for asynchronous communcation between the phone and the PitView Web Server
    public class WebListener extends AsyncTask{
        //pendingData is the message queue string
        String pendingData="";

        //appends S to the message queue
        public void sendData(String s){
            //make sure there's no empty line if it's the first message in the queue
            if(pendingData.equals(""))
                pendingData=s;
            else
                pendingData+="\n"+s;
        }
        //Updates the console text with the specified string
        public void updateUI(final String s){
            //cannot access UI stuff on a non-UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(s);
                }
            });
        }
        //updates the Web Listener conenction indicator with the specified value
        public void updateConnected(final boolean val){
            //cannot access UI stuff on a non-UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWebConnected(val);
                }
            });
        }
        //stops the web server
        public void bail(){
            //cannot access UI stuff on a non-UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopWebServerListener();
                }
            });
        }

        //task's main method, initiates connection/writer/sockets, continuous while loop
        //overrides the default doInBackground method
        @Override
        protected Object doInBackground(Object[] params) {
            try{
                //update the console display
                updateUI("Starting WebListener");
                //instantiate the server socket for PitView to connect to on port 1112
                webServerSocket = new ServerSocket(1112);
                //instantiate the connection with the PitView web server as the accepted socket from the PitView connection request
                //      CODE WAITS HERE UNTIL PITVIEW SENDS CONNECTION REQUEST
                webServerDataSocket = webServerSocket.accept();
                //update the connection indicator
                updateConnected(true);
                //welcome message
                updateUI("PitView v1.0");
                updateUI("Connected to PitView (IP=" + webServerDataSocket.getRemoteSocketAddress().toString().substring(1) + ":1112)");
                //instantiate the data writer to relay the data to PitView
                instantiateWriter(webServerDataSocket.getOutputStream());
                //add data to the message queue
                sendData("Connection initialized");
                //repeat while server is on
                while(isWebDataServerRunning){
                    //if queue isn't empty
                    if(!pendingData.equals("")) {
                        //write the data
                        webServerOutput.write(pendingData);
                        // IMPORTANT: write a new line, necessary to actually send the data
                        webServerOutput.newLine();
                        //send the data
                        webServerOutput.flush();

                        pendingData = "";
                    }
                }
            }catch(SocketException se) {
                //connection was interrupted, closed on one end, or nullified
                updateUI("WebServer connection interrupted, shutting down...");
                bail();
            }catch(Exception e){
                updateUI("Error: "+e.toString());
            }
            return null;
        }
    }

    //WebServer Listener Task for asynchronous communcation between the phone and the PitView Web Server
    public class PiListener extends AsyncTask{
        //Updates the console text with the specified string
        public void updateUI(final String s){
            //cannot access UI stuff on a non-UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(s);
                }
            });
        }
        //updates the Web Listener conenction indicator with the specified value
        public void updateConnected(final boolean val){
            //cannot access UI stuff on a non-UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPiConnected(val);
                }
            });
        }
        //stops the web server
        public void bail(){
            //cannot access UI stuff on a non-UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopPiListener();
                }
            });
        }

        //task's main method, initiates connection/reader/sockets, continuous while loop
        //overrides the default doInBackground method
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                //update the console display
                updateUI("Starting PiListener");
                //instantiate the server socket for the pi to connect to on port 1111
                piServerSocket = new ServerSocket(1111);
                //instantiate the connection with the pi as the accepted socket from the pi connection request
                //      CODE WAITS HERE UNTIL PITVIEW SENDS CONNECTION REQUEST
                Socket echoSocket = piServerSocket.accept();
                //update connection indicator
                updateConnected(true);
                if (echoSocket == null) {
                    updateUI("Socket connection failed");
                } else {
                    //while the server is running
                    while (isPiDataServerRunning) {
                        //read a line of the message
                        String line= new BufferedReader(new InputStreamReader(echoSocket.getInputStream())).readLine();
                        // line can be null for end of message, let's fix that and
                        final String output = line = line != null ? line : "";

                        // update ui
                        if (!line.isEmpty()) {
                            updateUI(output);
                            //append the data to the web server message queue
                            myWebListener.sendData(output);
                        }
                    }
                    updateUI("done receiving - now sent to blake");
                }
            }catch(SocketException se) {
                //connection was interrupted, closed on one end, or nullified
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