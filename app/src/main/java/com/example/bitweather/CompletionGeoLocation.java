package com.example.bitweather;

import android.location.Location;

public interface CompletionGeoLocation {
    public void completionGeocoderOk(Location location);
    public void completionGeocoderError(String error);
}
