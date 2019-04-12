package com.example.bitweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

//TODO: add @NONNULL or check services !=null
public class LocationService {

    private final String TAG = "--location";
    private LocationManager locationManager;
    private Geocoder geocoder;
    private Context ctx;
    private static LocationService instance;

    private LocationService(Context context){
        this.ctx = context;
        geocoder = new Geocoder(ctx, Locale.getDefault());
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    public static LocationService getInstance(Context context){
        if (instance == null) {
            instance = new LocationService(context);
        }
            return instance;
    }

    // TODO:
    /*
    * Move to non-ui thread geocoder query calls
    * asynctask ...
     */


  /*
  * Terminology
  * geocode():  getting coordinate location from address :   address(cityName) -> location2d
  * reverseGeocode() : getting address from location coordinates: location2d -> address(cityName)
  */

    //TODO: Permissions procedure: (API >23 needs manual)
    /*
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
    PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
    PackageManager.PERMISSION_GRANTED) {
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    } else {
        Toast.makeText(this, R.string.error_permission_map, Toast.LENGTH_LONG).show();
    }
    */


    public String getCityFromCurrentLocation(){
        String city = "";
        Location loc = getGPSLocation();
        if (loc != null ) {

            double lat = loc.getLatitude();
            double lon = loc.getLongitude();

            try {
                List <Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                Address firstAddr = addresses.get(0);
                city += firstAddr.getLocality();
                Log.d(TAG, "getCityFromCurrentLocation() => " + city);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return city;
    }


    public void getCityFromCurrentLocation(CompletionGeoAddress completion){

        Thread thread = new Thread() {
            @Override
            public void run() {
                String city = "";
                try{
                    Location loc = getGPSLocation();
                    if (loc == null ) {return;}
                    double lat = loc.getLatitude();
                    double lon = loc.getLongitude();
                    List <Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    Address firstAddr = addresses.get(0);
                    city = firstAddr.getLocality();
                    Log.d(TAG, "getCityFromCurrentLocation() => " + city);

                }catch(IOException e){
                    e.printStackTrace();
                }finally{
                    if(city != null){
                        completion.completionGeocoderOk(city);
                    }
                }
            }
        };
        thread.start();
    }


    public void getCityFromLocation(Location location, CompletionGeoAddress completion){

        Thread thread = new Thread() {
            @Override
            public void run() {
                String city = "";
                try{
                    Location loc = location;
                    if (loc == null ) {return;}
                    double lat = loc.getLatitude();
                    double lon = loc.getLongitude();
                    List <Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    Address firstAddr = addresses.get(0);
                    city = firstAddr.getLocality();
                    Log.d(TAG, "getCityFromLocation() => " + city);

                }catch(IOException e){
                    e.printStackTrace();
                }finally{
                    if(city != null){
                        completion.completionGeocoderOk(city);
                    }
                }
            }
        };
        thread.start();
    }

    //TODO: run on non-ui thread
    public Location getLocationFromCityName(String locationName){
        Location loc = new Location("");
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses.isEmpty()){
                return loc;
            }
            double lat = addresses.get(0).getLatitude();
            double lon = addresses.get(0).getLongitude();
            loc.setLatitude(lat);
            loc.setLongitude(lon);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return loc;

    }

    public void getLocationFromCityName(String locationName, CompletionGeoLocation completion){

        Thread thread = new Thread() {
            @Override
            public void run() {
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
                    completion.completionGeocoderOk(location);

                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void getAddressFromCityName(String locationName, CompletionGeoAddress completion){

        Thread thread = new Thread() {
            @Override
            public void run() {
                String r = "";
                try{
                    List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                    if (addresses.isEmpty()){
                        return;
                    }
                    Address addr = addresses.get(0);
                    Locale lo = addr.getLocale();
                    r = addr.getLocality() + ", " + addr.getAdminArea() + ", " + addr.getCountryName();
                    completion.completionGeocoderOk(r);


                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }




    public Location getGPSLocation() {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(ctx, "Location not available.", Toast.LENGTH_SHORT).show();
            Log.i(TAG,"COARSE_LOCATION not available");
            return null;
        }
        //FINE_LOCATION not in use (GPS provider only)
        //Location lastKnownLocationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Location lastKnownLocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        if (lastKnownLocationPassive != null) {
            Log.d(TAG, "location: "+lastKnownLocationPassive.toString());
            return lastKnownLocationPassive;
        }else{
            Log.d(TAG, "location unreachable");
            return null;
        }


    }










}
