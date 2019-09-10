package com.example.bitweather;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

import static com.example.bitweather.DarkSkyForecast.Condition.clear;
import static com.example.bitweather.DarkSkyForecast.Condition.cloudy;
import static com.example.bitweather.DarkSkyForecast.Condition.light_snow;
import static com.example.bitweather.DarkSkyForecast.Condition.partly_cloudy;
import static com.example.bitweather.UnitsHelper.UnitSystems.ca;
import static com.example.bitweather.UnitsHelper.UnitSystems.us;


public class WeatherSrv {

    private final String TAG = "--WeatherSrv: ";

    private final String baseUrlStr = "https://api.darksky.net/forecast/";
    private final String apiKey = "519982b83ceb95db10a5d638eee9d5eb";

    private GsonBuilder gsonBuilder = new GsonBuilder();
    private Gson gson = gsonBuilder.create();

    //singleton
    private static WeatherSrv instance;

    private WeatherSrv(){ }

    public static WeatherSrv getInstance(){
        if (instance == null){
            instance = new WeatherSrv();
        }
        return instance;
    }
    //



    public void requestWeather(Context ctx, UnitsHelper.UnitSystems units, Location location, CompletionForecast completion) {

        String unitsSpec = "?units=auto";
        switch (units) {
        case ca:
            unitsSpec = "?units=ca";
            break;
        case us:
            unitsSpec = "?units=us";
            break;
            default:
                unitsSpec = "?units=auto";
        }

        if (location == null){
            completion.completionError("Location not reachable");
            return;
        }
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        RequestQueue queue = Volley.newRequestQueue(ctx);

        String requestStr = ""+baseUrlStr+apiKey+"/"+ lat + "," + lon +unitsSpec;
        Log.d("--DS http request:   ", requestStr);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                requestStr, null, new Response.Listener<JSONObject>() {


                    public void onResponse(JSONObject response) {
                       // textView.setText("Response: " + response.toString());
                        DarkSkyForecast result = processData(response);
                        if (result == null) {
//                            Log.d(TAG, "DarkSkyForecast == null" );
                            completion.completionError("Invalid JSON from server");
                        }else{
                            Log.d(TAG, "DarkSkyForecast response ok" );
                            completion.completionOk(result);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString());
                        completion.completionError("onResponse ERROR");

                    }
                });

        queue.add(jsonObjectRequest);

    }




    private DarkSkyForecast processData(JSONObject jsonData) {

        //TODO: nonnull check
        if (jsonData==null) { return null; }

        DarkSkyForecast decodedForecast = new DarkSkyForecast();
        DarkSkyForecast.Currently currently = decodedForecast.initCurrently();
        DarkSkyForecast.Daily daily = decodedForecast.initDaily();
        DarkSkyForecast.Hourly hourly = decodedForecast.initHourly();


        try {
            String zoneStr = (String) jsonData.optString("timezone");
            if(zoneStr!=null){
                TimeZone zone = TimeZone.getTimeZone(zoneStr);
                decodedForecast.zone = zone;
            }

            JSONObject currentlyJson = (JSONObject) jsonData.get("currently");
            currently = gson.fromJson(currentlyJson.toString(), DarkSkyForecast.Currently.class);
            currently.getDsIcon();
            decodedForecast.currentlyPoint = currently;


            JSONObject dailyJson = (JSONObject) jsonData.optJSONObject("daily");
            JSONArray dailyJsonArr = (JSONArray) dailyJson.optJSONArray("data");
            for (int i=0; i<=dailyJsonArr.length()-1; i++){
                JSONObject day = dailyJsonArr.getJSONObject(i);
                daily = gson.fromJson(day.toString(), DarkSkyForecast.Daily.class);
                daily.getDsIcon();
                decodedForecast.dailyBlock.add(daily);
            }

            JSONObject hourlyJson = (JSONObject) jsonData.optJSONObject("hourly");
            String summary = (String) hourlyJson.opt("summary");
            decodedForecast.summaryNextHours = summary;
            JSONArray hourlyArr = (JSONArray) hourlyJson.optJSONArray("data");
            for (int i=0; i<=hourlyArr.length()-1; i++){
                JSONObject hour = hourlyArr.getJSONObject(i);
                hourly = gson.fromJson(hour.toString(), DarkSkyForecast.Hourly.class);
                hourly.getDsIcon();
                decodedForecast.hourlyBlock.add(hourly);
            }


//            Log.d(TAG, currently.toString().replace(",", "\n"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return decodedForecast;

    }

//TODO: move to DS api specific class
    public DarkSkyForecast.Condition evaluateCondition(
            double probability, double temp, double intensity, double accumulation,
            double cover, DarkSkyForecast.DSIcons apiIcon, UnitsHelper.UnitSystems unitSystem) {

        DarkSkyForecast.Condition r = clear;

        //DS API metric units: precipitation intens: mm/hr, accumulation: cm of snow

        double intens = 0;
        double accum = 0;
        double tmp = 0;

        // equalized intensities  -> mm/hour rain and cm/hour snow
        if (unitSystem == UnitsHelper.UnitSystems.us) {
            intens = intensity * 25.2; //in->mm
            accum = accumulation * 2.5; //in->cm
            tmp = UnitsHelper.convertFarenheit(temp);
        }else{
            intens = intensity;
            accum = accumulation;
            tmp = temp;
        }

        if (cover < 0.2){
            r = DarkSkyForecast.Condition.clear;
        }else if (cover >= 0.2 && cover < 0.7){
            r = DarkSkyForecast.Condition.partly_cloudy;
        }else if (cover >= 0.7 ){
            r = DarkSkyForecast.Condition.cloudy;
        }

        if (apiIcon == DarkSkyForecast.DSIcons.fog) { r = DarkSkyForecast.Condition.fog; }
        if (apiIcon == DarkSkyForecast.DSIcons.wind) { r = DarkSkyForecast.Condition.wind; }

        //moderate drizzle: 0.5mm (0.04"); heavy drizzle: 1mm(0.04");
        //slight rain: 0.5-1mm (0.02-0.04"); light rain : 1-2mm (0.04"-0.07"); moderate: 2-10mm (0.07-0.4"); heavy: >10 (>0.4")
        //snow: same numbers, but in cm

        Boolean isApiPrecip = (apiIcon == DarkSkyForecast.DSIcons.snow
                || apiIcon == DarkSkyForecast.DSIcons.rain
                || apiIcon == DarkSkyForecast.DSIcons.sleet );
        if (isApiPrecip && probability > 0.2 && (accum > 0.2 || intens > 0.2)){
            r = apiIcon == DarkSkyForecast.DSIcons.sleet ? DarkSkyForecast.Condition.sleet
                    : (apiIcon == DarkSkyForecast.DSIcons.rain ? DarkSkyForecast.Condition.light_rain
                    : DarkSkyForecast.Condition.snow);
            if (accum > 2 || intens > 2) {
                r = apiIcon == DarkSkyForecast.DSIcons.rain ? DarkSkyForecast.Condition.rain
                        : DarkSkyForecast.Condition.snow;
            }
        }else{
            if(probability > 0.8 && !isApiPrecip){
                if (accum > 1 && intens > 1) { r = DarkSkyForecast.Condition.sleet; }
                else if (accum > 1) { r = DarkSkyForecast.Condition.snow; }
                else if (intens > 1) { r = DarkSkyForecast.Condition.rain; }
            }
        }

        // freezing rain correction:
        if ((r == DarkSkyForecast.Condition.rain ||
                r == DarkSkyForecast.Condition.light_rain)  && tmp < +3 && accum > 0.0) {
            r = DarkSkyForecast.Condition.freezing_rain;
        }

        return r;
    }

    public String formatPrecipString(UnitsHelper.UnitsStrings2 uStrings, DarkSkyForecast.Condition condition, double value){
        String r = "";
        String unit = "";
        Boolean isSnow = false;

        if (       condition == DarkSkyForecast.Condition.snow
                || condition == DarkSkyForecast.Condition.light_snow
                || condition == DarkSkyForecast.Condition.sleet){
            unit = uStrings.precipSnowStr;
            isSnow = true;
        }else{
            unit = uStrings.precipRainStr;
            isSnow = false;
        }

        if (uStrings.sys == UnitsHelper.UnitSystems.ca) {
            if (isSnow){
                    if (value <= 0.2) { r = "flurries" ;}
                    else if (value > 0.2 && value <=1) { r="snow ~1 " + unit;}
                    else if (value > 1) { r="snow "+ String.format("%.0f", value) + unit;}  // ".. 2cm"
                    else {r="snow "+String.format("%.0f", value) + unit;}
            }else{
                if (value <= 1) { r = "light rain" ;}
                else if (value > 1 && value <=3) { r="rain " + String.format("%.0f", value) + unit;} // "... 2mm"
                else {r="rain "+String.format("%.0f", value) + unit;}
            }

        }else{ //us
            if (isSnow){
                if (value <= 0.2) { r = "flurries" ;}
                else if (value > 0.2 && value <=1) { r="snow ~1 " + unit;}
                else if (value > 1) { r="snow "+ String.format("%.0f", value) + unit;}
                else {r="snow "+String.format("%.0f", value) + unit;}
            }else{
                if (value <= 0.05) { r = "light rain" ;}
                else if (value > 0.05 && value <=0.1) { r="rain ~0.1in";}
                else if (value > 0.1) { r="rain "+String.format("%.1f", value) + unit;} // " ... 0.3in"
                else {r="rain "+String.format("%.1f", value) + unit;}
            }
        }

        if (condition == DarkSkyForecast.Condition.freezing_rain) { r = "freezing rain"; }

        return r;
    }


    public static String formatTemperatureString(Double value, String unitStr){
        if (value > 10000 || value < -10000)  {return "";}
        int intVal = value.intValue();
        //format: "%.0f", val
          return  intVal<=0 ? ("" + intVal + unitStr) : ("+" + intVal + unitStr);
    }

//CLASS END
}
