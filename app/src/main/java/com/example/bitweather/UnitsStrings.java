package com.example.bitweather;

//TODO: remove, use of static analog UnitsHelper.UnitsStrings2
public class UnitsStrings {

    UnitsHelper.UnitSystems sys;
    String tempStr;
    String speedStr;
    String precipRainStr;
    String precipSnowStr;
    String pressureStr;
    String distanceStr;

    public UnitsStrings(UnitsHelper.UnitSystems system) {
        switch (system) {
            case us:
                sys = UnitsHelper.UnitSystems.us;
                tempStr = "°F";
                speedStr = "mph";
                precipRainStr = "in";
                precipSnowStr = "in";
                pressureStr = "mb";
                distanceStr = "mi";

            case ca:
                sys = UnitsHelper.UnitSystems.ca;
                tempStr = "°C";
                speedStr = "km/h";
                precipRainStr = "mm";
                precipSnowStr = "cm";
                pressureStr = "mb";
                distanceStr = "km";

        }
    }
}
