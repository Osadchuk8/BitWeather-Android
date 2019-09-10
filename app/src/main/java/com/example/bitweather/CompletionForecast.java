package com.example.bitweather;


public interface CompletionForecast {
    public void completionOk(DarkSkyForecast forecast);
    public void completionError(String errorMessage);
}
