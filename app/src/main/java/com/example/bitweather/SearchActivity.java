package com.example.bitweather;

import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private final String TAG = "--Search";
    SearchView searchView;
    LinearLayout layoutResults;
    LocationService mLocationSrv;
    Map<String, Location> resultMap = new HashMap<>();
    Handler handler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mLocationSrv = LocationService.getInstance(this);


        searchView = findViewById(R.id.sv_search);
        searchView.setFocusableInTouchMode(true);
        searchView.setFocusable(true);
        searchView.requestFocus();


        layoutResults = findViewById(R.id.ll_table_results);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                geoCodeCityName(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "onQueryTextChange: " + newText);
                        if (newText.length() < 2){
                            resultMap.clear();
                            layoutResults.removeAllViews();
                        }

                        if(newText.length() > 2) {
                            geoCodeCityName(newText);
                        }
                    }
                }, 700);

                return false;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "OnCloseListener.onClose()");
                resultMap.clear();
                layoutResults.removeAllViews();
                return false;
            }
        });


    }

    public void onStart(){
        super.onStart();
        //TODO: solve focusing on searchview
        searchView.setFocusableInTouchMode(true);
        searchView.setFocusable(true);
        searchView.requestFocus();
    }


    private void geoCodeCityName(String searchStr){
        mLocationSrv.getAddressFromCityName(searchStr, new CompletionGeoAddress() {
            @Override
            public void completionGeocoderOk(String addressStr) {
                Log.d(TAG, "found City:: " + addressStr);
                mLocationSrv.getLocationFromCityName(addressStr, new CompletionGeoLocation() {
                    @Override
                    public void completionGeocoderOk(Location location) {
                       addResultToList(addressStr, location);
                    }

                    @Override
                    public void completionGeocoderError(String error) {
                        Log.d(TAG, "geoCodeCityName error, inner");

                    }
                });

            }

            @Override
            public void completionGeocoderError(String error) {
                Log.d(TAG, "geoCodeCityName error");

            }
        });
    }


    private void addResultToList(String addressStr, Location location){

        if(resultMap.containsKey(addressStr)){
            return;
        }
        resultMap.put(addressStr, location);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //add new city to the list

                TextView tv = new TextView(SearchActivity.this);
                tv.setText(addressStr);
                tv.setTextColor(getResources().getColor(R.color.snowColor));

                LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lllp.setMargins(8,8,8,8);
                tv.setLayoutParams(lllp);

                tv.setTextSize(20);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key = tv.getText().toString();
                        prepareWeatherRequest(resultMap.get(key));
                        finish();
                    }
                });
                layoutResults.addView(tv);
            }
        });



    }

    private void prepareWeatherRequest(Location location){
        AppShared.location = location;
        AppShared.isGps = false;
        AppShared.needsRefresh = true;
        AppShared.needsGpsWeather = false;
        Log.d(TAG, "appshared.location = " + location.toString());
    }


}
