package com.example.bitweather;

import android.location.Location;

public interface CompletionForecast {
    public void completionOk(DarkSkyForecast forecast);
    public void completionError(String errorMessage);
}
