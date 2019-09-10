package com.example.bitweather;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class UnitsHelper {


    public enum UnitSystems {us, ca}

    public static UnitsStrings2 initStrings(UnitsHelper.UnitSystems system){
        return new UnitsStrings2(system);
    }

    public static double convertFarenheit(double temp){
        return (temp-32)*(5/9);
    }


    public static String convDegreesToCardinal(double degrees) {

        String[] names = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int count = names.length;

        //let dir:Int = remainder(round((degrees/360) * count),count)
        double fl = (degrees / 360) * (double) count;
        int ro = (int) (Math.round(fl));
        int re = ro % count;
        if (re < 0) {
            re += count;
        }
        return names[re];
    }

    public static String timeStampFrom(long millis){
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
        String str = sdf.format(date);
        return ""+str;
    }

    public static int hourFrom(int unixTime) {
        int hour = 0;
        //use Calendar for integer values
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) unixTime * 1000);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        //Log.d("**TIMESTAMP to HOUR:", "stmp: " + unixTime + " hour: " + hour);
        return hour;

    }

    // Specific timezone hour value (not client local)
    public static int hourNonLocalFrom(int unixTime, TimeZone zone)  {
        int hour = -1;
        //use Calendar for integer values
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) unixTime * 1000);
        calendar.setTimeZone(zone);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    //TODO: device locale-based format (24H vs 12h)
    public static String hourMinutesStringFrom(int unixTime) {
        Date date = new Date((long)unixTime*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
        String str = sdf.format(date);
        return str;
    }

    //TODO: device locale-based format (24H vs 12h)
    public static String hourMinutesNonLocalFrom(int unixTime, TimeZone zone) {
        Date date = new Date((long)unixTime*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
        sdf.setTimeZone(zone);
        String str = sdf.format(date);
        return str;
    }

    public static String dateEMMMddFrom(int unixTime) {
        Date date = new Date((long)unixTime*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("E, MMM dd");
        String str = sdf.format(date);
        return str;
    }

    public static String dateFull(int unixTime){
        Date date = new Date((long)unixTime*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss -- z");
        String str = sdf.format(date);
        return str;
    }




    public static class UnitsStrings2 {

        UnitsHelper.UnitSystems sys;
        String tempStr;
        String speedStr;
        String precipRainStr;
        String precipSnowStr;
        String pressureStr;
        String distanceStr;

        public UnitsStrings2(UnitsHelper.UnitSystems system) {
            switch (system) {
                case us:
                    sys = UnitsHelper.UnitSystems.us;
                    tempStr = "°F";
                    speedStr = "mph";
                    precipRainStr = "in";
                    precipSnowStr = "in";
                    pressureStr = "mb";
                    distanceStr = "mi";
                    break;

                case ca:
                    sys = UnitsHelper.UnitSystems.ca;
                    tempStr = "°C";
                    speedStr = "km/h";
                    precipRainStr = "mm";
                    precipSnowStr = "cm";
                    pressureStr = "mb";
                    distanceStr = "km";
                    break;

            }
        }
    }




}

