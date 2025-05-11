package com.example.weather;

import android.util.Log;

public class WeatherUtils {
    private static final String TAG = "WeatherUtils";

    public static String mapVisualCrossingIconToOpenWeatherMap(String vcIcon) {
        if (vcIcon == null) {
            Log.e(TAG, "Icon is null, cannot map to OpenWeatherMap");
            return null;
        }
        String baseUrl = "https://openweathermap.org/img/wn/";
        String iconCode;
        boolean isDay = !vcIcon.contains("night");

        switch (vcIcon.toLowerCase()) {
            case "clear-day":
            case "clear-night":
                iconCode = "01" + (isDay ? "d" : "n");
                break;
            case "partly-cloudy-day":
            case "partly-cloudy-night":
                iconCode = "02" + (isDay ? "d" : "n");
                break;
            case "cloudy":
                iconCode = "03d";
                break;
            case "overcast-day":
            case "overcast-night":
                iconCode = "04" + (isDay ? "d" : "n");
                break;
            case "rain":
            case "rain-day":
            case "rain-night":
                iconCode = "10" + (isDay ? "d" : "n");
                break;
            case "thunderstorm":
            case "thunderstorm-day":
            case "thunderstorm-night":
                iconCode = "11" + (isDay ? "d" : "n");
                break;
            case "snow":
            case "snow-day":
            case "snow-night":
                iconCode = "13" + (isDay ? "d" : "n");
                break;
            case "fog":
            case "fog-day":
            case "fog-night":
                iconCode = "50" + (isDay ? "d" : "n");
                break;
            default:
                Log.e(TAG, "Unknown icon: " + vcIcon);
                return null;
        }
        return baseUrl + iconCode + "@4x.png";
    }
}