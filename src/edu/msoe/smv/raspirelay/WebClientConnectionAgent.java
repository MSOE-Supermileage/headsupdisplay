/**
 * Project: AndroidRelay
 * Author: Austin Hartline
 * Date: 12/7/14
 */

package edu.msoe.smv.raspirelay;

import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author austin & Blake
 * @version 2014.12.14
 */
public class WebClientConnectionAgent extends AsyncTask<Void, Void, Void> {
    private HeadsUpDisplay display;
    private ServerSocket serverSocket;
    private Socket dataSocket;
    private BufferedWriter serverOutput;
    public boolean isRunning;
    // the message queue string
    private String pendingData = "";

    public void startServer() {
        isRunning = true;
        this.execute();
    }

    public void stopServer() {
        isRunning = false;
        try {
            serverSocket.close();
            dataSocket.close();
            serverOutput.close();
        }catch(Exception e){
            updateUI("Error! (" + e.toString()+")");
        }
        updateConnected(false);
        updateUI("--Pitview Connection Stopped--");
    }

    public WebClientConnectionAgent(HeadsUpDisplay h) {
        display = h;
    }

    /**
     * appends the string to the message queue
     *
     * @param s
     */
    public void sendData(String s) {
        //make sure there's no empty line if it's the first message in the queue
        if (pendingData.equals(""))
            pendingData = s;
        else
            pendingData += "\n" + s;
        updateUI("Sent '"+s+"' to PitView");
    }

    /**
     * updates the console text with the specified string
     */
    private void updateUI(final String s) {
        //cannot access UI stuff on a non-UI thread
        display.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                display.updateConsole(s);
            }
        });
    }

    /**
     * updates the Web Listener concoction indicator with the specified value
     */
    private void updateConnected(final boolean val) {
        //cannot access UI stuff on a non-UI thread
        display.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                display.setWebConnected(val);
            }
        });
    }

    /**
     * stops the web server
     */

    private void instantiateWriter(OutputStream stream) {
        serverOutput = new BufferedWriter(new OutputStreamWriter(stream));
    }

    /**
     * task's main method, initiates connection/writer/sockets, continuous while loop
     * overrides the default doInBackground method
     *
     * @param params an arbitrary number (0..*) of Void objects.
     *               Yep, void freaking objects. don't pass this anything.
     * @return void.
     * yes, you can in fact return void type objects in java. don't do this in graded code.
     */
    @Override
    protected Void doInBackground(Void... params) {
        try {
            //update the console display
            updateUI("--Pitview Connection Started--");

            //instantiate the server socket for PitView to connect to on port 1112
            serverSocket = new ServerSocket(1112);
            //instantiate the connection with the PitView web server as the accepted socket from the PitView connection request
            //      CODE WAITS HERE UNTIL PITVIEW SENDS CONNECTION REQUEST
            dataSocket = serverSocket.accept();
            //update the connection indicator
            updateConnected(true);
            //welcome message
            updateUI("PitView v1.0");
            updateUI("Connected to PitView (IP = " + dataSocket.getRemoteSocketAddress().toString().substring(1));
            //instantiate the data writer to relay the data to PitView
            instantiateWriter(dataSocket.getOutputStream());
            //add data to the message queue
            sendData("Connection initialized");
            //repeat while server is on
            while (isRunning) {
                //if queue isn't empty
                if (!pendingData.equals("")) {
                    //write the data
                    serverOutput.write(pendingData);
                    // IMPORTANT: write a new line, necessary to actually send the data
                    serverOutput.newLine();
                    //send the data
                    serverOutput.flush();
                    //clear the queue
                    pendingData = "";
                }
            }
        } catch (SocketException se) {
            //connection was interrupted, closed on one end, or nullified
            updateUI("WebServer connection interrupted, shutting down...");
            stopServer();
        } catch (Exception e) {
            updateUI("Error: " + e.toString());
        }
        return null;
    }
}
