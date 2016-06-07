package edu.msoe.smv.utility;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import edu.msoe.smv.service.LapTimerService;
import edu.msoe.smv.service.VehicleConnectionService;

/**
 * Created by austin on 1/31/16.
 *
 */
@SuppressWarnings("unused")
public class Utility {

    private Utility() {}

    public static void startVehicleConnectionService(Context context, ResultReceiver receiver) {
        Log.d("debug", "starting vehicle service...");
        Intent serviceBindingIntent = new Intent(context, VehicleConnectionService.class);
        serviceBindingIntent.putExtra("receiver", receiver);
        context.startService(serviceBindingIntent);
        Log.d("debug", "service started");
    }

    public static void stopVehicleConnectionService(Context context) {
        Log.d("debug", "stopping vehicle connection service...");
        Intent stopIntent = new Intent(context, VehicleConnectionService.class);
        context.stopService(stopIntent);
        Log.d("debug", "service stopped");
    }

    public static void startLapTimerService(Context context, ResultReceiver receiver) {
        Log.d("debug", "starting lap timer service...");
        Intent serviceBindingIntent = new Intent(context, LapTimerService.class);
        serviceBindingIntent.putExtra("receiver", receiver);
        context.startService(serviceBindingIntent);
        Log.d("debug", "service started");
    }

    public static void stopLapTimerService(Context context) {
        Log.d("debug", "stopping the lap timer service...");
        Intent stopIntent = new Intent(context, LapTimerService.class);
        context.stopService(stopIntent);
        Log.d("debug", "service stopped");
    }

    public static Bundle parseDataNode(JsonReader reader) throws IOException {
        // Bundle is effectively a hash map - the view can seek what it wants
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
        if (!args.isEmpty())
            builder.append(args.get(args.size() - 1));
        return builder.toString();
    }
}
