package com.example.bitweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class LocationService {

    private final String TAG = "--location--";

    private LocationManager locationManager;
    private Geocoder geocoder;
    private Context ctx;
    private static LocationService instance;
    // private com.google.android.gms.location.FusedLocationProviderClient locationClient;

    private LocationService(Context context){
        this.ctx = context;
        geocoder = new Geocoder(ctx, Locale.getDefault());
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
//        locationClient = LocationServices.getFusedLocationProviderClient(context);

    }

    public static LocationService getInstance(Context context){
        if (instance == null) {
            instance = new LocationService(context);
        }
            return instance;
    }


    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

  /*
  * Terminology
  * geocode():  getting coordinate location from address :   address(cityName) -> location2d
  * reverseGeocode() : getting address from location coordinates: location2d -> address(cityName)
  */




//TODO: remove
    public void getCityFromCurrentLocation(CompletionString completion){

        //TODO: bg thread -> in activity
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//
//            }
//        };
//        thread.start();


//        getLocation(new CompletionGeoLocation() {
//            @Override
//            public void completionLocationOk(Location loc) {
//                String city = "";
//                try{
//                    if (loc == null ) {return;}
//                    double lat = loc.getLatitude();
//                    double lon = loc.getLongitude();
//                    List <Address> addresses = geocoder.getFromLocation(lat, lon, 1);
//                    Address firstAddr = addresses.get(0);
//                    city = firstAddr.getLocality();
//                    //Log.d(TAG, "getCityFromCurrentLocation() => " + city);
//                }catch(IOException e){
//                    e.printStackTrace();
//                }finally{
//                    if(city != null){
//                        completion.completionStringOk(city);
//                    }
//                }
//            }
//
//            @Override
//            public void completionLocationError(String error) {
//                Log.i(TAG, "getLocation() error");
//
//            }
//        });
    }


    public void getCityFromLocation(Location location, CompletionString completion){

        String city = "";
        try{
            Location loc = location;
            if (loc == null ) {return;}
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            List <Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            Address firstAddr = addresses.get(0);
            city = firstAddr.getLocality();
            //Log.d(TAG, "getCityFromLocation() => " + city);

        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(city != null){
                completion.completionStringOk(city);
            }
        }

    }

    public void getLocationFromCityName(String locationName, CompletionGeoLocation completion){

        Location location = new Location("");
        try{
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses.isEmpty()){
                return;
            }
            double lat = addresses.get(0).getLatitude();
            double lon = addresses.get(0).getLongitude();
            location.setLatitude(lat);
            location.setLongitude(lon);
            completion.completionLocationOk(location);

        }catch(IOException e) {
            e.printStackTrace();
        }

    }

    public void getAddressFromCityName(String locationName, CompletionString completion){

        try{
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses.isEmpty()){
                return;
            }
            Address addr = addresses.get(0);
            StringBuilder sb = new StringBuilder();
            if(null != addr.getLocality()) sb.append(addr.getLocality()+ ", ");
            if(null != addr.getAdminArea()) sb.append(addr.getAdminArea()+ ", ");
            if(null != addr.getCountryName()) sb.append(addr.getCountryName());
            completion.completionStringOk(sb.toString());

        }catch(IOException e) {
            e.printStackTrace();
        }

    }


    //Not used in the current implementation
    // build.gradle: uncomment gms play-services-location api for this dependency
    /*
    public void getFusedLocation(CompletionGeoLocation completion) {
        Location l;
        if ( ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
             ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {
            Log.i(TAG, " PackageManager.PERMISSION_GRANTED==FALSE");

             locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    if(location!=null){
                        completion.completionLocationOk(location);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                 @Override
                 public void onFailure(@NonNull Exception e) {
                     Log.d(TAG, "getFusedLocation  onFailure()");
                     e.printStackTrace();
                 }
             });

            }
    }
    */


    //TODO:
    /*
    * 1) build PROVIDER LIST
    * 2) check if providers enabled
    * 3) check permissions
    * 4) request permission
    * 5) use lastKnown or requestLocation if null
    * */




// Change to completion type
    //TODO:
    //split to : initLocation (on service start)
    //: getLocation (services ok)
    public void getLocation(CompletionGeoLocation completion, String provider) {

        Location l;

        if (    ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            l = locationManager.getLastKnownLocation(provider);

            //Location manager doesn't hold any recent location updates, need to request
            if (l == null) {
                //update location

                Log.d(TAG, "requestSingleUpdate");

                locationManager.requestSingleUpdate(provider, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "onLocationChanged!!");
                        completion.completionLocationOk(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }, Looper.getMainLooper());

            } else {
                completion.completionLocationOk(l);
            }

        }else{
            //no permissions granted, inform user
            Log.d(TAG, "requesting permissions");
            ActivityCompat.requestPermissions((MainActivity) ctx, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }



    }








}
