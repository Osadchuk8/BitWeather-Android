package com.example.bitweather;

import android.location.Location;

import java.util.TimeZone;

public class AppShared {

    public static Boolean isGps = false;
    public static TimeZone zone = null;
    public static Location location = null;
    //TODO: get saved value from shared prefs
    public static UnitsHelper.UnitSystems unitSys =  null;
    public static Boolean needsGpsWeather = true;
    public static Boolean needsRefresh = true;


}
