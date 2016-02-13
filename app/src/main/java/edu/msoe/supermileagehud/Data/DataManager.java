package edu.msoe.supermileagehud.Data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Blake on 1/23/2016.
 */
public class DataManager {
    private static final String PATH="mydata.csv";
    private static Queue<String> lines;
    private static DataManager me;
    public static DataManager getInstance(){
        if(me==null){
            me=new DataManager();
        }
        return me;
    }
    private DataManager(){
        lines=new LinkedList<>();
    }
    public static void writeLine(JSONObject o){
        String line=getString(o);
        if(line.length()>0){
            lines.add(line);
        }
    }
private static String getString(JSONObject mObject) {
            String res="";
            Iterator<String> iter=mObject.keys();
            while(iter.hasNext()){
                try {
                    res+=mObject.get(iter.next())+",";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(res.length()>0) {
                return res.substring(0, res.length() - 1);
            }else{
                return res;
            }
        }
}
