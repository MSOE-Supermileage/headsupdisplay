package edu.msoe.smv.RaspberryPi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.msoe.smv.ConnectionActivity;

public class ConnectionThread extends Thread {
    private ConnectionActivity activity;

    private ServerSocket serverSocket;

    /**
     * The thread that waits for the connection from the raspberry pi
     */
    public ConnectionThread(ConnectionActivity activity, int port) {
        this.activity = activity;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                //Start new thread for the communication
                new CommunicationThread(activity, clientSocket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
