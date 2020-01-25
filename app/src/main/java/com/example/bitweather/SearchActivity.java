package com.example.bitweather;

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
       // ImageView searchIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon); wrong id
        //searchIcon.setColorFilter(Color.WHITE);

        layoutResults = findViewById(R.id.ll_table_results);

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                searchView.clearFocus();
                searchView.requestFocus();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
               // Log.d(TAG, "onQueryTextSubmit: " + query);
                geoCodeCityName(query);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    //Log.d(TAG, "onQueryTextChange: " + newText);
                    if (newText.length() < 2){
                        resultMap.clear();
                        layoutResults.removeAllViews();
                    }
                    if(newText.length() > 2) {
                        geoCodeCityName(newText);
                    }
                }, 700);
                return false;
            }
        });


        searchView.setOnCloseListener(() -> {
            //Log.d(TAG, "OnCloseListener.onClose()");
            resultMap.clear();
            layoutResults.removeAllViews();
            return false;
        });

    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, ".onResume()");

        //* SOLUTION: focusing on searchview
//        searchView.setFocusableInTouchMode(true);
//        searchView.setFocusable(true);
//        searchView.requestFocus();
        searchView.onActionViewExpanded();
        searchView.performClick();

    }


    private void geoCodeCityName(String searchStr){
        mLocationSrv.getAddressFromCityName(searchStr, new CompletionString() {
            @Override
            public void completionStringOk(String str) {
                //Log.d(TAG, "found City:: " + addressStr);
              //  String addressStr = str.replace("null,", "").replace("NULL", "");


                mLocationSrv.getLocationFromCityName(str, new CompletionGeoLocation() {
                    @Override
                    public void completionLocationOk(Location location) {
                       addResultToList(str, location);
                    }

                    @Override
                    public void completionLocationError(String error) {
                        Log.d(TAG, "geoCodeCityName error, inner");

                    }
                });

            }

            @Override
            public void completionStringError(String error) {
                Log.d(TAG, "geoCodeCityName error");

            }
        });
    }


    private void addResultToList(String addressStr, Location location){

        if(resultMap.containsKey(addressStr)){
            return;
        }
        resultMap.put(addressStr, location);

        runOnUiThread(() -> {
            //add new city to the list

            TextView tv = new TextView(SearchActivity.this);
            tv.setText(addressStr);
            tv.setTextColor(getResources().getColor(R.color.snowColor));

            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lllp.setMargins(8,8,8,8);
            tv.setLayoutParams(lllp);

            tv.setTextSize(18);
            tv.setOnClickListener(v -> {
                String key = tv.getText().toString();
                prepareWeatherRequest(resultMap.get(key));
                finish();
            });
            layoutResults.addView(tv);
        });



    }

    private void prepareWeatherRequest(Location location){
        AppShared.setLocation(location);
        AppShared.setIsGps(false);
        AppShared.setNeedsRefresh(true);
        AppShared.setNeedsGpsWeather(false);
        //Log.d(TAG, "appshared.location = " + location.toString());
    }


}
