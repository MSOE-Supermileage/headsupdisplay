package edu.msoe.supermileagehud.Calculations;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Connor on 10/21/2015.
 */
public class Calculator {
    private final double MULTIPLIER = (1000.0 / 1.0) * (3600.0 / 1.0) * (2.0 * Math.PI * 10.0) * (1.0 / 12.0) * (1.0 / 5280.0);
    private double speed = 0;
    private double rpms = 0;
    private JSONObject data = new JSONObject();

    public void updateTime(int timeDifference) {
        rpms = (1.0 / timeDifference);

        speed = rpms * MULTIPLIER;
        rpms *= 60000.0;

        try {
            updateJSON();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public double getSpeed() {
        return speed;
    }

    public double getRpms() {
        return rpms;
    }

    private void updateJSON() throws JSONException {
        data.put("speed", speed);
        data.put("rpm", rpms);
    }

    public String getJSON() {
        return data.toString();
    }
}
