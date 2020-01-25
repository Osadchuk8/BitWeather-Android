package com.example.bitweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.bitweather.UnitsHelper.UnitSystems.ca;
import static com.example.bitweather.UnitsHelper.UnitSystems.us;
import static com.example.bitweather.WeatherSrv.formatTemperatureString;

public class MainActivity extends AppCompatActivity {

    private LinearLayout rootLinearLayout;

    private final String TAG = "--main--";
    private LinearLayout layout24h;
    private LinearLayout layout6d1;
    private LinearLayout layout6d2;

    private ProgressBar progressWheel;
    private View coverView;
    private ImageButton btnGps;

    //Current
    private TextView tvCityName;
    private TextView tvCurrentTemp;
    private TextView tvCurrentDescription;
    private TextView tvCurrentFeelsLike;
    private TextView tvTempHigh;
    private TextView tvTempLow;
    private ImageView ivCurrentIcon;
    private ImageButton btnOptions;
    private Button btnCloseOptions;

    //Details
    private TextView tvDetailSummary;
    private Button btnToggleDetails;
    private LinearLayout layoutDetails;
    private boolean isDetailsHidden = false;
    private TextView tvDetailWind;
    private TextView tvDetailHumidity;
    private TextView tvDetailVisibility;
    private TextView tvDetailPressure;
    private TextView tvDetailSunrise;
    private TextView tvDetailSunset;

    private LinearLayout llMenuOptions;
    private ArrayList<Button> segmentedControlBtns = new ArrayList<>(3);
    private Button btnAuto;
    private Button btnC;
    private Button btnF;

    private LocationService locationSrv;
    private WeatherSrv weatherSrv;
    private UnitsHelper.UnitSystems currentSys = UnitsHelper.UnitSystems.ca;
    private UnitsHelper.UnitsStrings unitStrings;
    private static final int PERMISSION_CODE = 802;
    private boolean isDark = false;

    private boolean isNetworkEnabled = false;

    private boolean isLocationGranted = false;
    private boolean isLocationEnabled = false;
    private String locationProvider = "";
    private SharedPreferences prefs;

    private Timer requestTimer;
    private static final int PERIOD = 300;  //seconds
    private boolean needsCheckUps = true;
    private ImageButton btnSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        doLayoutInit();

        this.locationSrv = LocationService.getInstance(this);
        this.weatherSrv = WeatherSrv.getInstance();
        AppShared.setNeedsRefresh(true);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

        readPreferences();
    }

    private void readPreferences() {
        Log.d("````", "readPreferences()");
        if (prefs == null) {
            return;
        }
        Log.d(TAG, prefs.toString());
        String unitStr = prefs.getString("UNIT", "");
        if (unitStr.length() > 0) {
            switch (unitStr) {
                case "AUTO":
                    AppShared.setUnitSys(null);
                    btnAuto.performClick();
                    Log.d(TAG, "clicked btnAuto");
                    break;
                case "CA":
                    AppShared.setUnitSys(UnitsHelper.UnitSystems.ca);
                    btnC.performClick();
                    Log.d(TAG, "clicked btnC");
                    break;
                case "US":
                    AppShared.setUnitSys(us);
                    btnF.performClick();
                    Log.d(TAG, "clicked btnF");
                    break;
                default: break;
            }
        }
    }

    private void savePreferences() {
        Log.d("````", "readPreferences()");
        if (prefs == null) return;

        String unitStr;
        if (AppShared.getUnitSys() == ca) unitStr = "CA";
        else if (AppShared.getUnitSys() == us) unitStr = "US";
        else unitStr = "AUTO";

        Log.d(TAG, "savePreferences(): unitStr=" + unitStr + "  .currentSys= " + AppShared.getUnitSys());
        prefs.edit().putString("UNIT", unitStr).apply();
    }

    /**
     * POSSIBLE bg thread, use runOnUI for ui upd
     */
    private void checkLocationPermissions() {
        Log.d("````", "checkLocationPermissions()");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            this.isLocationGranted = true;

        } else {
            //no permissions granted, inform user
            Log.d(TAG, "requesting permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_CODE);
        }

        this.isLocationEnabled = true;
        this.btnGps.setVisibility(View.VISIBLE);

        LocationManager locationManager = locationSrv.getLocationManager();
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        } else {
            //no provider available, ask user to turn on Location services in the settings
            this.isLocationEnabled = false;
            this.btnGps.setVisibility(View.INVISIBLE);
        }
        Log.d(TAG, "locationProvider: " + locationProvider);

        if (!(this.isLocationGranted && this.isLocationEnabled)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
            builder.setMessage("BitWeather is unable to reach location, probably location services are disabled ? " +
                    "Use search to find weather by a city name");
            builder.setPositiveButton("OK", (dialog, which) -> MainActivity.this.launchSearchActivity(MainActivity.this.getContext()));
            AlertDialog dialog = builder.create();
            dialog.show();

            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            if (btn != null) {
                LinearLayout.LayoutParams btnParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                btnParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                btnParams.gravity = Gravity.CENTER;
                btn.setTextColor(Color.WHITE);
                btn.setLayoutParams(btnParams);
            }

        }
    }


    /**
     * Task executed periodically, every [PERIOD] seconds, after [delay] millis
     * Calls from run() gonna be executed in bg thread
     */
    private void startTimedRequest() {
//        Log.d(TAG, "```startTimedRequest,  isUI thread? : " + String.valueOf(Looper.myLooper() == Looper.getMainLooper()));
        this.requestTimer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                MainActivity.this.runOnUiThread(() -> progressWheel.setVisibility(View.VISIBLE));
                performRequests();
                Log.d("```timer task: ", UnitsHelper.timeStampFrom(System.currentTimeMillis()));
            }
        };

        this.requestTimer.scheduleAtFixedRate(task, 1L, PERIOD * 1000L);
    }

    protected void onResume() {
        super.onResume();

        this.setUnits();
        if (needsCheckUps || !isNetworkEnabled) {
            checkLocationPermissions();
            doNetworkCheckupAndTimedRequest();
        } else startTimedRequest();

    }

    private void doNetworkCheckupAndTimedRequest() {
        Log.d("````", "doNetworkCheckupAndTimedRequest()");
        /**
         * checks connectivity, stop if no internet. Starts weather sequence in bg thread if ok
         */
        Thread thread = new Thread() {
            @Override
            public void run() {

                ConnectivityHelper.isOnline(new CompletionString() {

                    @Override
                    public void completionStringOk(String msg) {

                        Log.d("````", "isOnline() -> OK");
                        if ((isLocationGranted && isLocationEnabled) || (isLocationGranted && !AppShared.getNeedsGpsWeather()))
                            startTimedRequest();
                        else Log.e(TAG, "```` location not grated ");
                        MainActivity.this.runOnUiThread(() -> btnSearch.setVisibility(View.VISIBLE));
                        isNetworkEnabled = true;
                        needsCheckUps = false;
                    }

                    @Override
                    public void completionStringError(String error) {
                        Log.d("````", "isOnline() -> ERROR");
                        MainActivity.this.runOnUiThread(() -> {
                            btnSearch.setVisibility(View.INVISIBLE);
                            btnGps.setVisibility(View.INVISIBLE);
                            isNetworkEnabled = false;

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
                            builder.setMessage("" + error);
                            builder.setPositiveButton("OK", (dialog, which) -> {
                                System.out.println(TAG + "``` clicked: " + which);
                            });

                            final AlertDialog dialog = builder.create();
                            dialog.show();

                            Button btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            //TODO: move following button layout setup to UI/gfx utils
                            if (btn != null) {
                                LinearLayout.LayoutParams btnParams = (LinearLayout.LayoutParams) btn.getLayoutParams();
                                btnParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                btnParams.gravity = Gravity.CENTER;
                                btn.setTextColor(Color.WHITE);
                                btn.setLayoutParams(btnParams);
                            }

                            //TODO: DISABLE flow here. Need to stop activity from request
                            MainActivity.this.stopRequest();

                        });
                    }
                });

            }
        };
        Log.d("````", "thread.start()");
        thread.start();
        Log.d("````", "end of startSeq..()");
    }


    protected void onPause() {
        stopRequest();
        savePreferences();
        super.onPause();
    }

    private void stopRequest(){
        if (requestTimer != null) requestTimer.cancel();
        this.progressWheel.setVisibility(View.INVISIBLE);
    }


    private void setUnits() {
        //Provided by Appshared.unitSys:

        if (AppShared.getUnitSys() != null) {
            //Value defined as .ca or .us
            // Log.d(TAG, "AppShared.currentSys: " + AppShared.currentSys.toString());
            this.currentSys = AppShared.getUnitSys();
        } else {
            //Means "auto", autodetect each time.
            String countryCode = Locale.getDefault().getCountry().toLowerCase();
            // Log.d(TAG, "countryCode = " + countryCode);
            if (countryCode.contains("us")) {
                this.currentSys = us;
            } else {
                this.currentSys = ca;
            }
        }


        this.unitStrings = UnitsHelper.initStrings(this.currentSys);
    }

    private void requestGpsWeather() {
        if (!isLocationEnabled) return;
        AppShared.setNeedsRefresh(true);
        AppShared.setNeedsGpsWeather(true);
        startTimedRequest();
    }


    private void performRequests() {
        Log.d(TAG, "performRequests().. isUI thread? : " + String.valueOf(Looper.myLooper() == Looper.getMainLooper()));

        if(!this.isNetworkEnabled){
            MainActivity.this.runOnUiThread(this::stopRequest);
            MainActivity.this.runOnUiThread(this::doNetworkCheckupAndTimedRequest);
        }


        if (AppShared.getNeedsGpsWeather()) {
            if(!this.isLocationEnabled) {
                MainActivity.this.runOnUiThread(this::checkLocationPermissions);
            }


            locationSrv.getLocation(new CompletionGeoLocation() {
                    @Override
                    public void completionLocationOk(Location location) {
                        AppShared.setLocation(location);
                        AppShared.setIsGps(true);

                        doWeatherRequest();

                        locationSrv.getCityFromLocation(AppShared.getLocation(), new CompletionString() {
                            @Override
                            public void completionStringOk(String string) {
                                MainActivity.this.runOnUiThread(() -> tvCityName.setText(string));
                            }

                            @Override
                            public void completionStringError(String error) {
                                Log.d(TAG, error);
                            }
                        });
                    }

                    @Override
                    public void completionLocationError(String error) {
                        Log.d(TAG, "completionLocationError");
                        return;
                    }
                }, LocationManager.NETWORK_PROVIDER
            );


        } else { //non-local
            // following is async
            doWeatherRequest();
            locationSrv.getCityFromLocation(AppShared.getLocation(), new CompletionString() {
                @Override
                public void completionStringOk(String string) {
                    MainActivity.this.runOnUiThread(() -> tvCityName.setText(string));
                }

                @Override
                public void completionStringError(String error) {
                    Log.d(TAG, error);
                }
            });
        }


    }

    private void doWeatherRequest() {
        weatherSrv.requestWeather(this, currentSys, AppShared.getLocation(), new CompletionForecast() {
            @Override
            public void completionOk(DarkSkyForecast forecast) {
                MainActivity.this.runOnUiThread(() -> displayWeather(forecast));
            }

            @Override
            public void completionError(String errorMessage) {
                MainActivity.this.runOnUiThread(() -> GfxHelper.displayToast(getContext(), errorMessage));
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult(..): ok");
            } else {
                Log.d(TAG, "onRequestPermissionsResult(..):  not granted");
            }
        }
    }


    private void displayCurrently(DarkSkyForecast forecast) {
        DarkSkyForecast.Currently currently = forecast.currentlyPoint;
        if (currently == null) {
            return;
        }

        //TODO: implement Location-based tomeZone solution
        //TimeZone, provided by weather api
        if (forecast.zone != null) {
            AppShared.setZone(forecast.zone);
        }

        tvCurrentDescription.setText(currently.summary);
        tvCurrentTemp.setText(formatTemperatureString(currently.temperature, unitStrings.tempStr));
        tvCurrentFeelsLike.setText("feels like " + formatTemperatureString(currently.apparentTemperature, ""));
        //weather icon: getting id value from DS icon

        String idString = "cond_" + currently.dsIcon.toString();
        //= "cond_"+currently.icon.replace("-","_");
        int id = getResources().getIdentifier(idString, "drawable", this.getPackageName());
        Drawable d = getResources().getDrawable(id);
        ivCurrentIcon.setImageDrawable(d);

        //details
        tvDetailSummary.setText(forecast.summaryNextHours);
        tvDetailHumidity.setText(String.format("%.0f", currently.humidity * 100) + "%");
        tvDetailPressure.setText(String.format("%.0f", currently.pressure) + this.unitStrings.pressureStr);
        tvDetailVisibility.setText(String.format("%.0f", currently.visibility) + this.unitStrings.distanceStr);
        tvDetailWind.setText(UnitsHelper.convDegreesToCardinal(currently.windBearing) + " " + String.format("%.0f", currently.windSpeed) + this.unitStrings.speedStr);

    }

    private void displayBgImageSetTemp(DarkSkyForecast forecast) {
        // background, detect night/day
        List<DarkSkyForecast.Daily> daily = forecast.dailyBlock;
        DarkSkyForecast.Currently currently = forecast.currentlyPoint;

        if (daily == null || daily.isEmpty()) {
            return;
        }


        int sr = daily.get(0).sunriseTime;
        int ss = daily.get(0).sunsetTime;

        if (!AppShared.getIsGps() && AppShared.getZone() != null) {
            tvDetailSunrise.setText(UnitsHelper.hourMinutesNonLocalFrom(sr, AppShared.getZone()));
            tvDetailSunset.setText(UnitsHelper.hourMinutesNonLocalFrom(ss, AppShared.getZone()));
        } else {

            tvDetailSunrise.setText("" + UnitsHelper.hourMinutesStringFrom(sr));
            tvDetailSunset.setText("" + UnitsHelper.hourMinutesStringFrom(ss));
        }

        double now = new Date().getTime() / 1000.0;
        this.isDark = now < sr || now > ss;


        //TODO: evaluate current background from current condition, use condition code instead of api icon.
        DarkSkyForecast.DSIcons icon = currently.dsIcon;
        int bgId = 0;
        if (icon == DarkSkyForecast.DSIcons.clear_day || icon == DarkSkyForecast.DSIcons.clear_night) {
            bgId = (this.isDark ? R.drawable.bg_clear_night : R.drawable.bg_clear_day);
        } else if (icon == DarkSkyForecast.DSIcons.partly_cloudy_day || icon == DarkSkyForecast.DSIcons.partly_cloudy_night) {
            bgId = (this.isDark ? R.drawable.bg_partially_cloudy_night : R.drawable.bg_partially_cloudy_day);
        } else {
            bgId = (this.isDark ? R.drawable.bg_cloudy_night : R.drawable.bg_cloudy_day);
        }

        this.rootLinearLayout.setBackground(getResources().getDrawable(bgId));

        tvTempHigh.setText(
//                "▲ " +
                        DarkSkyForecast.formatTemperatureString(daily.get(0).temperatureHigh, "") );
        tvTempHigh.setTextColor(getResources().getColor(R.color.snowColor));
        tvTempLow.setText(
//                "▼ " +
                        DarkSkyForecast.formatTemperatureString(daily.get(0).temperatureLow, ""));
        tvTempLow.setTextColor(getResources().getColor(R.color.snowColor));

    }

    private void display6daySection(DarkSkyForecast forecast) {

        List<DarkSkyForecast.Daily> daily = forecast.dailyBlock;
        if (daily.size() < 6) {
            return;
        }

        List<View6d> views = new ArrayList<>();

        for (int i = 0; i <= 2; i++) {
            View6d v6d = (View6d) layout6d1.getChildAt(i);
            views.add(v6d);
        }
        for (int i = 0; i <= 2; i++) {
            View6d v6d = (View6d) layout6d2.getChildAt(i);
            views.add(v6d);
        }

        for (int i = 0; i <= 5; i++) {

            View v = views.get(i);
            //((View6d) v).setChildViews(v);
            DarkSkyForecast.Daily day = daily.get(i + 1);

            TextView tvTitle = v.findViewById(R.id.tv_6day_title);
            TextView tvTempHigh = v.findViewById(R.id.tv_6day_temp_high);
            TextView tvTempLow = v.findViewById(R.id.tv_6day_temp_low);
            TextView tvPrecip = v.findViewById(R.id.tv_6day_precip);
            ImageView ivIcon = v.findViewById(R.id.iv_6day_icon);

            String dayName = UnitsHelper.dateEMMMddFrom(day.time);
            tvTitle.setText(dayName);

            String wind = "" + UnitsHelper.convDegreesToCardinal(day.windBearing)
                    + String.format("%.0f", day.windSpeed) + unitStrings.speedStr;
            DarkSkyForecast.Condition condition = weatherSrv.evaluateCondition(day.cloudCover, day.temperatureHigh,
                    day.precipIntensityMax, day.precipAccumulation, day.cloudCover, day.dsIcon, currentSys);

            this.displayEvaluatedCondition(false, condition, day.precipProbability, day.precipIntensityMax,
                    day.precipAccumulation, wind, ivIcon, tvPrecip);

            tvTempHigh.setText(formatTemperatureString(day.temperatureHigh, ""));
            tvTempLow.setText(formatTemperatureString(day.temperatureLow, ""));

        }

    }


    private void display24hSection(DarkSkyForecast forecast) {
        //HOURLY : 24h section

        List<DarkSkyForecast.Hourly> hourly = forecast.hourlyBlock;
        tvDetailSummary.setText(forecast.summaryNextHours);
        if (hourly.size() < 31) {
            return;
        }
        int currentHour = -1;
        if (Boolean.FALSE.equals(AppShared.getIsGps()) && AppShared.getZone() != null) {
            TimeZone zone = AppShared.getZone();
            currentHour = UnitsHelper.hourNonLocalFrom(hourly.get(0).time, zone);
        } else {
            currentHour = UnitsHelper.hourFrom(hourly.get(0).time);
        }
        if (currentHour < 0) {
            return;
        }
        int startIndex = 0;
        int endIndex = 0;
        int firstPartIndex = 0; // should be in 0...3
        String[] partTitleArr = new String[]{"night", "morning", "afternoon", "evening"};
        Boolean[] darkArr = new Boolean[]{true, false, false, true};

        int partEndHour = 6 * (1 + (currentHour / 6)) - 1;
        if (partEndHour - currentHour > 2) {
            //startIndex = 0;
            endIndex = partEndHour - currentHour - 1;
            firstPartIndex = (currentHour / 6) % 4;
        } else {
            startIndex = partEndHour - currentHour;
            endIndex = startIndex + 5;
            firstPartIndex = (currentHour / 6 + 1) % 4;
        }

        for (int viewInd = 0; viewInd <= 3; viewInd++) {
            View24h v = (View24h) layout24h.getChildAt(viewInd);

            int iFirst = startIndex + (viewInd) * 6;
            int iLast = (viewInd == 0 && endIndex - startIndex < 5) ? endIndex : iFirst + 5;
            int iPart = (firstPartIndex + viewInd) % 4; //cycle in 0...3

            double mIntensity = 0.0;
            double mTemp = 0.0;
            double accumulation = 0.0;
            double mProbability = 0.0;
            double mCover = 0.0;
            double mSpeed = 0.0;
            double mBearing = 0.0;

            double count = 0.0;

            for (int j = iFirst; j <= iLast; j++) {

                //precipitation parameters: MAX hourly value within 6hrs
                if (mIntensity < hourly.get(j).precipIntensity) {
                    mIntensity = hourly.get(j).precipIntensity;
                }
                if (mProbability < hourly.get(j).precipProbability) {
                    mProbability = hourly.get(j).precipProbability;
                }

                //summary for 6hrs for snow accumulation
                accumulation += hourly.get(j).precipAccumulation;
                mTemp += hourly.get(j).temperature;
                mCover += hourly.get(j).cloudCover;
                mSpeed += hourly.get(j).windSpeed;
                mBearing = hourly.get(j).windBearing;

                count += 1;

            }

            accumulation /= 1; //summary for given dayPart for snow accumulation
            mTemp /= count;
            mCover /= count;
            mSpeed /= count;
            mBearing /= count;

            this.isDark = darkArr[iPart];

            TextView tvTitle = v.findViewById(R.id.tv_24h_title);
            TextView tvTemp = v.findViewById(R.id.tv_24h_temp);
            TextView tvPrecip = v.findViewById(R.id.tv_24h_precip);
            ImageView iconView = v.findViewById(R.id.iv_24h_icon);
            LinearLayout levelView = v.findViewById(R.id.v_24h_level);
            levelView.removeAllViews();


            tvTitle.setText(partTitleArr[iPart]);
            tvTemp.setText(formatTemperatureString(mTemp, ""));

            String wind = UnitsHelper.convDegreesToCardinal(mBearing) + String.format("%.0f", mSpeed);
            DarkSkyForecast.Condition condition = weatherSrv.evaluateCondition(mProbability, mTemp, mIntensity, accumulation,
                    mCover, hourly.get(iFirst).dsIcon, currentSys);

            displayEvaluatedCondition(isDark, condition, mProbability, mIntensity,
                    accumulation, wind, iconView, tvPrecip);


            //precip levels display
            //TODO: extract to level up method

            Set<DarkSkyForecast.Condition> snowSet = new HashSet<>(Arrays.asList(
                    DarkSkyForecast.Condition.snow, DarkSkyForecast.Condition.light_snow, DarkSkyForecast.Condition.hail));
            Set<DarkSkyForecast.Condition> rainSet = new HashSet<>(Arrays.asList(
                    DarkSkyForecast.Condition.light_rain, DarkSkyForecast.Condition.rain,
                    DarkSkyForecast.Condition.freezing_rain, DarkSkyForecast.Condition.sleet));

            if (snowSet.contains(condition) || rainSet.contains(condition)) {

                double value = mIntensity;
                if (condition == DarkSkyForecast.Condition.snow || condition == DarkSkyForecast.Condition.light_snow) {
                    value = accumulation;
                }

                double maxValue = (currentSys == us) ? 8.0 : 20.0;
                double level = (value + 1 < maxValue) ? (value / maxValue + 0.1) : 1;

                int h = (int) (levelView.getMeasuredHeight() * level);
                int w = (int) levelView.getMeasuredWidth();
                double offset = levelView.getMeasuredHeight() - h;

                LinearLayout ll = new LinearLayout(this);
                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(w, h);

                ll.setLayoutParams(lllp);

                int snowId = getResources().getColor(R.color.snowColor);
                int rainId = getResources().getColor(R.color.rainColor);
                int colorId = snowSet.contains(condition) ? snowId : rainId;
                ll.setBackgroundColor(colorId);
                levelView.setGravity(Gravity.BOTTOM);
                levelView.addView(ll);

            }

        }
    }

    public void displayWeather(DarkSkyForecast forecast) {

        coverView.animate().alpha(0.1f).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                coverView.setVisibility(View.INVISIBLE);
            }
        });
        progressWheel.setVisibility(View.INVISIBLE);


        if (AppShared.getIsGps()) {
            btnGps.setVisibility(View.INVISIBLE);
            btnGps.setEnabled(false);
        } else {
            btnGps.setVisibility(View.VISIBLE);
            btnGps.setEnabled(true);
        }
        displayCurrently(forecast);
        displayBgImageSetTemp(forecast);
        display24hSection(forecast);
        display6daySection(forecast);

    }


    private void displayEvaluatedCondition(Boolean isDark, DarkSkyForecast.Condition condition, double probValue, double rainValue, double snowValue, String windString, ImageView targetIconView, TextView targetPrecipitationLabel) {

        displayEvaluatedIcon(isDark, condition, targetIconView);

        switch (condition) {
            case clear:
            case partly_cloudy:
            case cloudy:
            case wind:
                targetPrecipitationLabel.setText("" + String.format("%.0f", probValue * 100) + "%");
                break;
            case fog:
                targetPrecipitationLabel.setText("fog");
                break;
            case light_rain:
            case rain:
            case freezing_rain:
                targetPrecipitationLabel.setText(weatherSrv.formatPrecipString(unitStrings, condition, rainValue));
                break;
            case sleet:
            case light_snow:
            case snow:
            case hail:
                targetPrecipitationLabel.setText(weatherSrv.formatPrecipString(unitStrings, condition, snowValue));
                break;
        }
    }

    private void displayEvaluatedIcon(boolean isDark, DarkSkyForecast.Condition condition, ImageView targetIconView) {

        if (condition == DarkSkyForecast.Condition.clear || condition == DarkSkyForecast.Condition.partly_cloudy) {
            String iconName = isDark ? "cond_" + condition.toString() + "_night" : "cond_" + condition.toString() + "_day";
            int id = getResources().getIdentifier(iconName, "drawable", this.getPackageName());
            Drawable d = getResources().getDrawable(id);
            targetIconView.setImageDrawable(d);
        } else {
            String iconName = "cond_" + condition.toString();
            int id = getResources().getIdentifier(iconName, "drawable", this.getPackageName());
            Drawable d = getResources().getDrawable(id);
            targetIconView.setImageDrawable(d);
        }


    }


    // UI setup
    private void doLayoutInit() {
        //LAYOUT INIT >>>

        rootLinearLayout = findViewById(R.id.rootLinearLayout);

        btnToggleDetails = findViewById(R.id.btn_toggle_details);
        layoutDetails = findViewById(R.id.ll_details);
        layoutDetails.setVisibility(View.GONE);

        layout24h = findViewById(R.id.ll_24h);
        layout6d1 = findViewById(R.id.ll_6day1);
        layout6d2 = findViewById(R.id.ll_6day2);

        addTreeObserver(layout24h);

        for (int i = 1; i <= 4; i++) {
            View v = getLayoutInflater().inflate(R.layout.view_24h, (ViewGroup) layout24h, false);
            // v.setTag("v24h_"+i); //views in their container can be identified by order, so no need in tags.
            layout24h.addView(v);
        }

        for (int i = 1; i <= 3; i++) {
            View v = getLayoutInflater().inflate(R.layout.view_6d, (ViewGroup) layout6d1, false);
            layout6d1.addView(v);
        }

        for (int i = 1; i <= 3; i++) {
            View v = getLayoutInflater().inflate(R.layout.view_6d, (ViewGroup) layout6d2, false);
            layout6d2.addView(v);
        }


        //CURRENTLY
        tvCityName = findViewById(R.id.tv_current_city);
        tvCurrentTemp = findViewById(R.id.tv_current_temp);
        tvCurrentDescription = findViewById(R.id.tv_current_description);
        tvCurrentFeelsLike = findViewById(R.id.tv_current_feels);
        tvTempHigh = findViewById(R.id.tv_current_tmp_high);
        tvTempLow = findViewById(R.id.tv_current_tmp_low);
        ivCurrentIcon = findViewById(R.id.iv_currently_icon);


        //details
        tvDetailSummary = findViewById(R.id.tv_detail_summary);

        tvDetailWind = findViewById(R.id.tv_wind);
        tvDetailHumidity = findViewById(R.id.tv_humidity);
        tvDetailVisibility = findViewById(R.id.tv_visibility);
        tvDetailPressure = findViewById(R.id.tv_pressure);
        tvDetailSunrise = findViewById(R.id.tv_sunrise2);
        tvDetailSunset = findViewById(R.id.tv_sunset2);


        final Animation animSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        final Animation animSlideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);


        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(v -> launchSearchActivity(v.getContext()));

        btnGps = findViewById(R.id.btn_gps);
        btnGps.setOnClickListener(v -> requestGpsWeather());


        llMenuOptions = findViewById(R.id.ll_menu_options);
        //llMenuOptions.setVisibility(View.GONE);

        btnOptions = findViewById(R.id.btn_options);
        btnOptions.setOnClickListener(v -> {
            // commented ' android:animateLayoutChanges="true" ' in layout root node , using manual animation
            llMenuOptions.animate().y(MainActivity.this.getWindow().getDecorView().getBottom() - llMenuOptions.getHeight());
        });

        btnCloseOptions = findViewById(R.id.btn_close_options);
        btnCloseOptions.setOnClickListener(v -> {
            llMenuOptions.animate().y(MainActivity.this.getWindow().getDecorView().getBottom());
            if (AppShared.getNeedsRefresh()) {
                setUnits();
                startTimedRequest();
            }
        });

        btnAuto = findViewById(R.id.btn_select_auto);
        btnC = findViewById(R.id.btn_select_C);
        btnF = findViewById(R.id.btn_select_F);
        segmentedControlBtns.add(btnAuto);
        segmentedControlBtns.add(btnF);
        segmentedControlBtns.add(btnC);

        for (Button b : segmentedControlBtns) {
            b.setOnClickListener(v -> {
                //first all btns white bg
                for (Button b1 : segmentedControlBtns) {
                    b1.setBackground(getResources().getDrawable(R.drawable.bg_button_blueborder));
                    b1.setTextColor(getResources().getColor(R.color.bluetint));
                }

                Button vv = (Button) v;
                vv.setBackground(getResources().getDrawable(R.drawable.bg_button_whiteborder));
                vv.setTextColor(getResources().getColor(R.color.snowColor));

                switch (b.getId()) {
                    case R.id.btn_select_auto:
                        if (AppShared.getUnitSys() != null) {
                            AppShared.setUnitSys(null);
                            AppShared.setNeedsRefresh(true);
                        }
                        break;
                    case R.id.btn_select_F:
                        if (AppShared.getUnitSys() != us) {
                            AppShared.setUnitSys(us);
                            AppShared.setNeedsRefresh(true);
                        }
                        break;
                    case R.id.btn_select_C:
                        if (AppShared.getUnitSys() != UnitsHelper.UnitSystems.ca) {
                            AppShared.setUnitSys(UnitsHelper.UnitSystems.ca);
                            AppShared.setNeedsRefresh(true);
                        }
                        break;
                }
                //if .needsRefresh==true, state was changed -> refresh on click menu close
            });

        }

        //start with layout covered till data refresh
        coverView = findViewById(R.id.cover_view);
        coverView.setVisibility(View.VISIBLE);
        progressWheel = findViewById(R.id.progress_indicator);

        //LAYOUT INIT END
    }

    private void launchSearchActivity(Context ctx) {
        if (!isNetworkEnabled) return;
        Intent i = new Intent(MainActivity.this, SearchActivity.class);
        ctx.startActivity(i);
    }

    private void adjustInflatedViews() {

        //reference height: scrollview * 30%
        ScrollView sv = findViewById(R.id.scroll_view);
        LinearLayout llScrollContainer = findViewById(R.id.ll_scroll_container);
        int scrollViewHeight = sv.getHeight();
        int scrollViewWidth = sv.getWidth();
        // Log.d(TAG, "refHeight= " + scrollViewHeight );


        //det details layout
        layoutDetails.setMinimumHeight(scrollViewHeight * 3 / 10);


        //DEBUG
        int widthLayout24h = layout24h.getWidth();
        int heightLayout24h = layout24h.getHeight();
        // Log.d(TAG, "widthLayout24h: " + widthLayout24h + " :: heightLayout24h: " + heightLayout24h );
        // END DEBUG


        for (int i = 0; i < layout24h.getChildCount(); i++) {
            View v = layout24h.getChildAt(i);

            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(layout24h.getLayoutParams());
            lllp.width = scrollViewWidth * 24 / 100;
            lllp.height = scrollViewHeight * 25 / 100;
            lllp.setMargins(2, 0, 2, 0);
            v.setLayoutParams(lllp);

        }

        for (int j = 0; j < layout6d1.getChildCount(); j++) {
            View v1 = layout6d1.getChildAt(j);
            LinearLayout.LayoutParams lllp1 = new LinearLayout.LayoutParams(layout6d1.getLayoutParams());
            lllp1.width = scrollViewWidth / 3;
            lllp1.height = scrollViewHeight * 3 / 10;
            if (j != 1) {
                v1.setBackgroundColor(getResources().getColor(R.color.colorAlphaGrey10));
            }
            //lllp1.setMargins(8,8,8,8);
            v1.setLayoutParams(lllp1);
        }

        for (int j = 0; j < layout6d1.getChildCount(); j++) {
            View v2 = layout6d2.getChildAt(j);
            LinearLayout.LayoutParams lllp2 = new LinearLayout.LayoutParams(layout6d2.getLayoutParams());
            lllp2.width = scrollViewWidth / 3;
            lllp2.height = scrollViewHeight * 3 / 10;
            if (j == 1) {
                v2.setBackgroundColor(getResources().getColor(R.color.colorAlphaGrey10));
            }
            v2.setLayoutParams(lllp2);
        }

        //hide settings menu
        llMenuOptions.animate().y(this.getWindow().getDecorView().getBottom());

    }

    private void updateInflatedDispaly() {
    }


    public void onClickBtnDetails(View view) {
        if (isDetailsHidden) {
            layoutDetails.setVisibility(View.VISIBLE);
            btnToggleDetails.setBackgroundResource(R.drawable.btn_arrow_up);
        } else {
            layoutDetails.setVisibility(View.GONE);
            btnToggleDetails.setBackgroundResource(R.drawable.btn_arrow_down);
        }
        isDetailsHidden = !isDetailsHidden;
    }

    public Context getContext() {
        return this;
    }

    // UI Tweaks to be called after Activity layout job is done.
    private void addTreeObserver(final View view) {

        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // DO POST LAYOUT JOB
                adjustInflatedViews();
                updateInflatedDispaly();
            }
        });

    }

    // <<<<<<<<<<<
    //ACTIVITY END

}
