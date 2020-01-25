package com.example.bitweather;

import android.location.Location;

import java.util.TimeZone;

public class AppShared {

    private static boolean isGps = false;
    private static TimeZone zone = null;
    private static Location location = null;
    private static UnitsHelper.UnitSystems unitSys =  null;
    private static boolean needsGpsWeather = true;
    private static boolean needsRefresh = true;

    private AppShared(){}

    public static boolean getIsGps() {
        return isGps;
    }

    public static void setIsGps(boolean isGps) {
        AppShared.isGps = isGps;
    }

    public static TimeZone getZone() {
        return zone;
    }

    public static void setZone(TimeZone zone) {
        AppShared.zone = zone;
    }

    public static Location getLocation() {
        return location;
    }

    public static void setLocation(Location location) {
        AppShared.location = location;
    }

    public static UnitsHelper.UnitSystems getUnitSys() {
        return unitSys;
    }

    public static void setUnitSys(UnitsHelper.UnitSystems unitSys) {
        AppShared.unitSys = unitSys;
    }

    public static boolean getNeedsGpsWeather() {
        return needsGpsWeather;
    }

    public static void setNeedsGpsWeather(Boolean needsGpsWeather) {
        AppShared.needsGpsWeather = needsGpsWeather;
    }

    public static boolean getNeedsRefresh() {
        return needsRefresh;
    }

    public static void setNeedsRefresh(boolean needsRefresh) {
        AppShared.needsRefresh = needsRefresh;
    }
}
