package com.example.bitweather;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class DarkSkyForecast {

    public Currently currentlyPoint;
    public List<Daily> dailyBlock;
    public List<Hourly> hourlyBlock;
    public String summaryNextHours;
    public TimeZone zone;

    public DarkSkyForecast() {

        this.currentlyPoint = new Currently();
        this.dailyBlock = new ArrayList<Daily>();
        this.hourlyBlock = new ArrayList<Hourly>();
        this.summaryNextHours = "";
    }

    public Currently initCurrently() {
        return new Currently();
    }

    public Daily initDaily() {
        return new Daily();
    }

    public Hourly initHourly() {
        return new Hourly();
    }


    public enum DSPrecipType {
        rain, snow, sleet, freezing_rain
    }

    public enum DSIcons {
        //DS Api response string format has "-" separator -> needs to be replaced with matching "_" symbol
        clear_day, clear_night, rain, snow, sleet, hail, wind, fog, cloudy, partly_cloudy_day, partly_cloudy_night
    }

    public enum Condition {
        //evaluated condition
        //sleet: snow+rain
        clear, partly_cloudy, cloudy, fog, wind, light_rain, light_snow, rain, snow, sleet, hail, freezing_rain
    }


    public class Currently {
        // ["currently"] -> [String:Any]
        //currently block doesn't have precipAccumulation field
        public int time = 0;
        public String summary = "";
        public String icon = ""; //must be String for GSON parser
        public double precipIntensity = 0.0;
        public double precipProbability = 0.0;
        public DSPrecipType precipType;
        public double temperature = 0.0;
        public double apparentTemperature = 0.0;
        public double humidity = 0.0;
        public double pressure = 0.0;
        public double windSpeed = 0.0;
        public double windGust = 0.0;
        public double windBearing = 0.0;
        public double visibility = 0.0;
        public double cloudCover = 0.0;

        public DSIcons dsIcon = DSIcons.partly_cloudy_day;

        public void getDsIcon(){
            dsIcon =  DSIcons.valueOf(icon.replace("-","_"));
        }



    }


    public class Daily {
        // ["daily"] -> [[String:Any]]
        public int time = 0;
        public String summary = "";
        public String icon = "";
        public double temperatureHigh = 0.0;
        public double temperatureLow = 0.0;
        public double precipProbability = 0.0;
        public double precipIntensityMax = 0.0;
        public double precipAccumulation = 0.0;
        public DSPrecipType precipType;
        public int sunriseTime = 0;
        public int sunsetTime = 0;
        public double windSpeed = 0.0;
        public double windGust = 0.0;
        public double windBearing = 0.0;
        public double cloudCover = 0.0;

        public DSIcons dsIcon = DSIcons.partly_cloudy_day;

        public void getDsIcon(){
            dsIcon =  DSIcons.valueOf(icon.replace("-","_"));
        }

    }

    public class Hourly {
        // ["hourly"] -> [[String:Any]]
        public int time = 0;
        public String summary = "";
        public String icon = "";
        public double precipIntensity = 0.0;
        public double precipProbability = 0.0;
        public double precipAccumulation = 0.0;  //  snow accumulation: unit per hour
        public DSPrecipType precipType;
        public double temperature = 0.0;
        public double apparentTemperature = 0.0;
        public double windSpeed = 0.0;
        public double windGust = 0.0;
        public double windBearing = 0.0;
        public double cloudCover = 0.0;

        public DSIcons dsIcon = DSIcons.partly_cloudy_day;

        public void getDsIcon(){
            dsIcon =  DSIcons.valueOf(icon.replace("-","_"));
        }
    }

    public static String formatTemperatureString(double temperature, String unit) {
        String r = "";
        if (temperature < -1000 || temperature > +1000){ return ""; }
        int tempInt = (int)temperature;
        // display '+' prefix for non-negative, zero included :  " +0ºC "
        r += tempInt < 0 ? tempInt + unit : "+" + tempInt + unit;
        return r;
    }


    //CLASS END
}
