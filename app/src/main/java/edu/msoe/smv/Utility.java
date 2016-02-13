package edu.msoe.smv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import edu.msoe.smv.service.VehicleConnectionService;

/**
 * Created by austin on 1/31/16.
 */
public class Utility {

    private Utility() {}

    public static void startVehicleConnectionService(Context context, ResultReceiver receiver) {
        Log.d("debug", "starting vehicle service...");
        // Create the vehicle connection service
        Intent serviceBindingIntent = new Intent(context, VehicleConnectionService.class);
        serviceBindingIntent.putExtra("receiver", receiver);
        context.startService(serviceBindingIntent);
        Log.d("debug", "service started");
    }

    public static void stopVehicleConnectionService(Context context) {
        // stop the vehicle connection service
        Intent stopIntent = new Intent(context, VehicleConnectionService.class);
        context.stopService(stopIntent);
        Log.d("debug", "service stopped");
    }

    public static Bundle parseDataNode(JsonReader reader) throws IOException {
        Bundle data = new Bundle();

        reader.beginObject();
        while (reader.hasNext()) {
            data.putDouble(reader.nextName(), reader.nextDouble());
        }
        reader.endObject();

        return data;
    }

    public static long execWithTimer(Runnable runnable) {
        long startTime = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - startTime;
    }

    public static String stringJoin(List<String> args, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.size() - 1; i++) {
            builder.append(args.get(i));
            builder.append(delimiter);
        }
        builder.append(args.get(args.size() - 1));
        return builder.toString();
    }
}
