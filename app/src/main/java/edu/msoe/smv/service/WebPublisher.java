package edu.msoe.smv.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by austin on 1/31/16.
 *
 */
public class WebPublisher extends Handler {

    public static final String URL = "http://10.1.99.2/";

    private long lastSendTime;

    // default starts at half a second
    private int movingPublishPeriod = 500;

    private Socket webSocket;

    public WebPublisher() throws IOException {

        lastSendTime = System.currentTimeMillis();

        // TODO this is guaranteed to need some plug&play magic similar to
        // @see VehicleConnectionThread
        try {
            webSocket = IO.socket(URL);
            webSocket.connect();
        } catch (URISyntaxException e) {
            // why is this a checked exception
            // seriously
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    @Override
    public void handleMessage(Message message) {
        long curTime = System.currentTimeMillis();

        if (curTime - lastSendTime >= movingPublishPeriod) {
            // publish to pit view
            Bundle receive = message.getData();
            JSONObject objectSend = new JSONObject();
            for (String key : receive.keySet()) {
                try {
                    objectSend.put(key, receive.getDouble(key));
                } catch (JSONException e) {
                    // why is this a checked exception???
                    // rethrow unchecked. It's not like we're letting the user build the object.
                    throw new UnsupportedOperationException(e.getMessage());
                }
            }

            // TODO make this do what TCP does with windows and latency stuff
            // if we take more than half of our moving publish period to just send due to network
            // latency, we should increase our moving publish period and reduce the number
            // of times we publish per second.
            long emitStart = System.currentTimeMillis();
            webSocket.emit(objectSend.toString());
            if (System.currentTimeMillis() - emitStart > movingPublishPeriod / 2) {
                // increment by 100 ms until we hit the sweet spot
                movingPublishPeriod += 100;
            }
        }
    }
}
