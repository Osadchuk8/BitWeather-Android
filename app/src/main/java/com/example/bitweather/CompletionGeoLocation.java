package com.example.bitweather;

import android.location.Location;

public interface CompletionGeoLocation {

    public void completionLocationOk(Location location);
    public void completionLocationError(String error);
}
