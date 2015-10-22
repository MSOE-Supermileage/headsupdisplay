package edu.msoe.supermileagehud.Cloud;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Connor on 10/20/2015.
 */
public class CloudConnection
{
    private final String url = "http://supermileage.azurewebsites.net/";//"http://192.168.1.3:1337";

    private BlockingQueue<String> sendQueue = new LinkedBlockingQueue<>();

    public CloudConnection()
    {
        try
        {
            final Socket socket = IO.socket(url);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
            {
                @Override
                public void call(Object... args)
                {
                    new SocketCommunicationTask().execute(socket);
                }
            });

            socket.connect();
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    public void sendData(String data)
    {
        synchronized (sendQueue)
        {
            sendQueue.add(data);
        }
    }

    private class SocketCommunicationTask extends AsyncTask<Socket, Void, Void>
    {
        @Override
        protected Void doInBackground(Socket... socket)
        {
            while (true)
            {
                try
                {
                    String data = sendQueue.take();

                    socket[0].emit("data", data);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
