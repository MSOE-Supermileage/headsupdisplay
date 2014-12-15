package edu.msoe.smv.raspirelay;

import org.json.JSONStringer;

import java.util.Random;

/**
 * Created by Blake on 12/13/2014.
 *
 * Class to hold the data from the cars.
 */
public class Data {
    public float mpg=0.0f, batteryVoltage=0.0f,fuelRemaining=0.0f;

    public Data(){}

    public Data(float mpg,float batteryVoltage, float fuelRemaining){
        this.mpg=mpg;
        this.batteryVoltage=batteryVoltage;
        this.fuelRemaining=fuelRemaining;
    }

    public Data randomize(){
        Random r=new Random();
        mpg=r.nextFloat()*100;
        batteryVoltage=r.nextFloat()*12;
        fuelRemaining=r.nextFloat();
        return this;
    }

    @Override
    public String toString(){
        String data="{'mpg':"+mpg+",'batteryVoltage':"+batteryVoltage+
                ",'fuelRemaining':"+fuelRemaining+"}";
        return data;
    }
}
