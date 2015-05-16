/**
 * Project: AndroidRelay
 * Author: Austin Hartline
 * Date: 12/7/14
 */

package edu.msoe.smv.raspirelay;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.*;

/**
 * We need to implement a service decoupled from the activity so it can run asynchronously in the background as
 * a separate process if the activity fails or crashes.
 *
 * @author austin
 * @version 2014.12.07
 */
public class VehicleConnectionService extends Service {

    // the intent passed toe service on starting
    private Intent startIntent;

    private static InetAddress piInetAddress = null;

    private boolean requestData = true;

    /**
     * Message handling service to communicate with clients of this service
     */
    private ResultReceiver receiver;

    /**
     * the background thread for communicating with the raspberry pi
     */
    private Thread piBackgroundThread;

    /**
     * the runnable object used by the background thread
     */
    private final Runnable piCommunicationRunnable = new Runnable() {

        @Override
        public void run() {
            DatagramSocket raspberryPiSocket = null;
            try {
                raspberryPiSocket = new DatagramSocket();
                // we need a 10 second timeout
                raspberryPiSocket.setSoTimeout(10000);
            } catch (SocketException e) {
                messageClient("could not create socket. restart the app");
                // TODO handle better
                if (raspberryPiSocket != null) {
                    raspberryPiSocket.close();
                }
                return;
            }


            // notify the UI we created the socket and are waiting for a connection
            messageClient("waiting for raspi connection");

            try {
                piInetAddress = InetAddress.getByName("155.92.65.233");
            } catch (UnknownHostException e) {
                e.printStackTrace();
                messageClient("could not create the inetaddress. restart the app");
                // TODO handle better
                return;
            }

            while (requestData) {
                messageClient("requesting data...");
                byte[] recBuffer = new byte[512];

                // request data by sending a byte
                try {
                    raspberryPiSocket.send(new DatagramPacket(".".getBytes(), ".".getBytes().length, piInetAddress, 12100));
                } catch (IOException e) {
                    messageClient("failed to request data");
                    e.printStackTrace();
                }

                DatagramPacket receivePacket;
                receivePacket = new DatagramPacket(recBuffer, recBuffer.length);

                Thread socketTimeoutWatcher = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                            messageClient("lost connection, try again in 5 seconds...");
                        } catch (InterruptedException e) {
                            // then we must have received before timeout.
                        }
                    }
                });
                socketTimeoutWatcher.start();

                // blocking call
                try {
                    raspberryPiSocket.receive(receivePacket);
                    socketTimeoutWatcher.interrupt();

                    String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    Log.i("PACKET_RECEIVE", data);

                    sendNodeToClient(data);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


//            // create the connection socket at port 1111
//            ServerSocket raspberryPiSocketPlaceholder = new ServerSocket(1111);
//
//            // notify the UI we created the socket and are waiting for a connection
//            send.putString("message", "socket waiting @ port 9999");
//            receiver.send(100, send);
//
//            // block until we connect
//            raspberryPiSocket = raspberryPiSocketPlaceholder.accept();
//            raspberryPiSocket.getOutputStream().write("gimme some fucking data".getBytes());
//
//            // notify the UI we have established the connection
//            send = new Bundle();
//            send.putString("message", raspberryPiSocket.toString());
//            receiver.send(100, send);
//
//            GsonBuilder gsonBuilder = new GsonBuilder();
//            BufferedReader input = new BufferedReader(new InputStreamReader(raspberryPiSocket.getInputStream()));
//            while (true) {
//                StringBuilder objectBuilder = new StringBuilder("");
//                String nextLine = input.readLine();
//                do {
//                    nextLine = input.readLine();
//                    objectBuilder.append(nextLine);
//                } while (!nextLine.contains("}"));
//
//                JSONObject object = new JSONObject();
//                try {
//                    object = new JSONObject(objectBuilder.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    break;
//                }
//
//                send = new Bundle();
//                send.putString("message", object.toString());
//            }

        }
    };

    /**
     * send a message to the receiver
     * @param message the message to send
     */
    private void messageClient(String message) {
        Bundle send = new Bundle();
        send.putString("message", message);
        receiver.send(100, send);
    }

    /**
     *
     * @param jsonNode
     */
    private void sendNodeToClient(String jsonNode) {
        Bundle send = new Bundle();
        send.putString("node", jsonNode);
        receiver.send(200, send);

        // let clients deserialize like this:
//        edu.msoe.smv.raspi.DataNode node = gson.fromJson(data, edu.msoe.smv.raspi.DataNode.class);
    }

    private void processCount(long duration, int count) {

    }


    /**
     * the binder for binding to the service at application load time
     * create the binder at class loader time
     */
    private final IBinder localBinder = new VehicleConnectionServiceBinder();

    /**
     * android API callback #1 - create the service, but do not start
     */
    @Override
    public void onCreate() {

        Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_SHORT).show();

        // instantiate the thread that will communicate with the pi in the background
        piBackgroundThread = new Thread(piCommunicationRunnable);
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "onStartCommand()", Toast.LENGTH_SHORT).show();

        startIntent = intent;
        receiver = intent.getParcelableExtra("receiver");

        // need this check in the event the service is already started.
        if (!piBackgroundThread.isAlive()) {
            piBackgroundThread.start();
        }

        return START_REDELIVER_INTENT;
    }


    /**
     * I don't get called
     * @param intent
     * @return
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "The vehicle service was bound and Austin is going to be pissed.", Toast.LENGTH_SHORT).show();
        Log.e("service", "service was attempted to be bound to. ");
        return localBinder;
    }


    /**
     *
     */
    @Override
    public void onDestroy() {
        Toast.makeText(this, "onDestroy()", Toast.LENGTH_SHORT).show();
        Log.i("@onDestroy", "service stopped");
        requestData = false;
        try {
            piBackgroundThread.join();
            Toast.makeText(this, "thread stopped", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        super.onDestroy();
    }

    /**
     *
     */
    private class VehicleConnectionServiceBinder extends Binder {

        /**
         * @return
         */
        public VehicleConnectionService getService() {
            Toast.makeText(VehicleConnectionService.this, "getService()", Toast.LENGTH_SHORT).show();
            Log.i("@getService", "binding...");
            return VehicleConnectionService.this;
        }
    }

}