package com.example.weather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WeatherApp";
    private TextView locationTextView, temperatureTextView, conditionsTextView, humidityTextView, windSpeedTextView, date, uvIndex;
    private ImageView weatherIcon;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button forecastButton;

    private TextView hour1TempTextView, hour1TimeTextView;
    private ImageView hour1IconImageView;
    private TextView hour2TempTextView, hour2TimeTextView;
    private ImageView hour2IconImageView;
    private TextView hour3TempTextView, hour3TimeTextView;
    private ImageView hour3IconImageView;
    private TextView hour4TempTextView,  hour4TimeTextView;
    private ImageView hour4IconImageView;

    private ArrayList<String> forecastDays = new ArrayList<>();
    private ArrayList<String> forecastIcons = new ArrayList<>();
    private ArrayList<String> forecastConditions = new ArrayList<>();
    private ArrayList<String> forecastTemps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            locationTextView = findViewById(R.id.locationTextView);
            temperatureTextView = findViewById(R.id.temperatureTextView);
            conditionsTextView = findViewById(R.id.conditionsTextView);
            humidityTextView = findViewById(R.id.humidityTextView);
            windSpeedTextView = findViewById(R.id.windSpeedTextView);
            date = findViewById(R.id.date);
            uvIndex = findViewById(R.id.uvIndex);
            weatherIcon = findViewById(R.id.weatherIcon);
            forecastButton = findViewById(R.id.forecastButton);
            hour1TimeTextView = findViewById(R.id.hour1TimeTextView);
            hour1TempTextView = findViewById(R.id.hour1TempTextView);
            hour1IconImageView = findViewById(R.id.hour1IconImageView);
            hour1IconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            hour1IconImageView.setAdjustViewBounds(true);
            hour1IconImageView.setPadding(0, 0, 0, 0);
            hour2TimeTextView = findViewById(R.id.hour2TimeTextView);
            hour2TempTextView = findViewById(R.id.hour2TempTextView);
            hour2IconImageView = findViewById(R.id.hour2IconImageView);
            hour2IconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            hour2IconImageView.setAdjustViewBounds(true);
            hour2IconImageView.setPadding(0, 0, 0, 0);
            hour3TimeTextView = findViewById(R.id.hour3TimeTextView);
            hour3TempTextView = findViewById(R.id.hour3TempTextView);
            hour3IconImageView = findViewById(R.id.hour3IconImageView);
            hour3IconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            hour3IconImageView.setAdjustViewBounds(true);
            hour3IconImageView.setPadding(0, 0, 0, 0);
            hour4TimeTextView = findViewById(R.id.hour4TimeTextView);
            hour4TempTextView = findViewById(R.id.hour4TempTextView);
            hour4IconImageView = findViewById(R.id.hour4IconImageView);
            hour4IconImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            hour4IconImageView.setAdjustViewBounds(true);
            hour4IconImageView.setPadding(0, 0, 0, 0);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            Toast.makeText(this, "Error initializing UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Swipe to refresh triggered");
                new FetchWeatherTask().execute();
                swipeRefreshLayout.setRefreshing(false);
            });
        } else {
            Log.e(TAG, "SwipeRefreshLayout is null, cannot set listener");
            Toast.makeText(this, "Swipe to refresh not available", Toast.LENGTH_LONG).show();
        }

        if (forecastButton != null) {
            forecastButton.setOnClickListener(v -> {
                if (forecastDays.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No forecast data available. Please try refreshing.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ForecastActivity.class);
                intent.putStringArrayListExtra("days", forecastDays);
                intent.putStringArrayListExtra("icons", forecastIcons);
                intent.putStringArrayListExtra("conditions", forecastConditions);
                intent.putStringArrayListExtra("temps", forecastTemps);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Forecast button is null, cannot set listener");
            Toast.makeText(this, "Forecast button not available", Toast.LENGTH_LONG).show();
        }


        new FetchWeatherTask().execute();
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, String> {
        private String temperature, conditions, humidity, windSpeed, resolvedAddress, icon, dateTime, uvIndexValue;
        private String[] times = new String[4];
        private String[] hourlyTemps = new String[4];
        private String[] hourlyConditions = new String[4];
        private String[] hourlyIcons = new String[4];

        @Override
        protected String doInBackground(Void... params) {

            long currentTimeMillis = System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date(currentTimeMillis));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(currentTimeMillis));
            calendar.add(Calendar.DAY_OF_YEAR, 14);
            String endDate = dateFormat.format(calendar.getTime());

            String apiUrl = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/30.8616%2C29.5919/" +
                    currentDate + "/" + endDate + "?unitGroup=metric&key=" + getString(R.string.api_key) + "&contentType=json";

            try {
                Log.d(TAG, "Fetching weather data from URL: " + apiUrl);
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    conn.disconnect();

                    Log.d(TAG, "API Response: " + response.toString());

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    resolvedAddress = jsonResponse.optString("resolvedAddress", "Unknown");

                    double latitude = 30.8616;
                    double longitude = 29.5919;

                    JSONArray days = jsonResponse.getJSONArray("days");
                    if (days.length() > 0) {
                        JSONObject currentDay = days.getJSONObject(0);
                        dateTime = currentDay.optString("datetime", "N/A");
                    } else {
                        dateTime = "N/A";
                        Log.e(TAG, "No days data found in API response");
                    }

                    JSONObject currentConditions = jsonResponse.optJSONObject("currentConditions");
                    if (currentConditions != null) {
                        String tempString = currentConditions.optString("temp", "N/A");
                        if (!tempString.equals("N/A")) {
                            try {
                                double tempValue = Double.parseDouble(tempString);
                                temperature = String.valueOf(Math.round(tempValue)) + "°C";
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing temperature: " + e.getMessage());
                                temperature = "N/A°C";
                            }
                        } else {
                            temperature = "N/A°C";
                            Log.e(TAG, "currentConditions is null in API response: " + jsonResponse.toString());
                        }

                        conditions = currentConditions.optString("conditions", "N/A");
                        humidity = currentConditions.optString("humidity", "N/A") + "%";
                        windSpeed = currentConditions.optString("windspeed", "N/A") + " km/h";
                        icon = currentConditions.optString("icon", null);
                        uvIndexValue = currentConditions.optString("uvindex", "N/A");
                    } else {
                        Log.e(TAG, "currentConditions object is null");
                        return "Error: No current conditions data";
                    }

                    List<JSONObject> allHours = new ArrayList<>();
                    for (int d = 0; d < days.length(); d++) {
                        JSONArray hours = days.getJSONObject(d).getJSONArray("hours");
                        for (int h = 0; h < hours.length(); h++) {
                            allHours.add(hours.getJSONObject(h));
                        }
                    }
                    Log.d(TAG, "Total hours across all days: " + allHours.size());

                    SimpleDateFormat debugTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Log.d(TAG, "Current time: " + debugTimeFormat.format(new Date(currentTimeMillis)));
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    int startIndex = -1;
                    for (int i = 0; i < allHours.size(); i++) {
                        JSONObject hourData = allHours.get(i);
                        long epochTime = hourData.getLong("datetimeEpoch") * 1000L;
                        Log.d(TAG, "Hour " + i + ": " + hourData.optString("datetime") + ", epochTime: " + epochTime + ", currentTimeMillis: " + currentTimeMillis);
                        if (epochTime >= currentTimeMillis) {
                            startIndex = i;
                            break;
                        }
                    }

                    if (startIndex != -1) {
                        for (int i = 0; i < 4; i++) {
                            int index = startIndex + i;
                            if (index < allHours.size()) {
                                JSONObject hourData = allHours.get(index);
                                long epochTime = hourData.getLong("datetimeEpoch") * 1000L;
                                times[i] = timeFormat.format(new Date(epochTime));
                                String tempString = hourData.optString("temp", "N/A");
                                if (!tempString.equals("N/A")) {
                                    try {
                                        double tempValue = Double.parseDouble(tempString);
                                        hourlyTemps[i] = String.valueOf(Math.round(tempValue)) + "°C";
                                    } catch (NumberFormatException e) {
                                        Log.e(TAG, "Error parsing forecast temp: " + e.getMessage());
                                        hourlyTemps[i] = "N/A";
                                    }
                                } else {
                                    hourlyTemps[i] = "N/A";
                                }
                                hourlyConditions[i] = hourData.optString("conditions", "N/A");
                                hourlyIcons[i] = hourData.optString("icon", null);
                                Log.d(TAG, "Forecast hour " + (i + 1) + ": time=" + times[i] + ", temp=" + hourlyTemps[i] + ", conditions=" + hourlyConditions[i] + ", icon=" + hourlyIcons[i]);
                            } else {
                                times[i] = "N/A";
                                hourlyTemps[i] = "N/A";
                                hourlyConditions[i] = "N/A";
                                hourlyIcons[i] = null;
                                Log.w(TAG, "No data for forecast hour " + (i + 1) + ", setting to N/A");
                            }
                        }
                    } else {
                        for (int i = 0; i < 4; i++) {
                            times[i] = "N/A";
                            hourlyTemps[i] = "N/A";
                            hourlyConditions[i] = "N/A";
                            hourlyIcons[i] = null;
                        }
                        Log.e(TAG, "No future hours found in API response. Last hour: " + allHours.get(allHours.size() - 1).optString("datetime"));
                    }


                    forecastDays.clear();
                    forecastIcons.clear();
                    forecastConditions.clear();
                    forecastTemps.clear();

                    if (days.length() > 0) {
                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                        for (int i = 0; i < 5 && i < days.length(); i++) {
                            JSONObject dayData = days.getJSONObject(i);
                            long epochTime = dayData.getLong("datetimeEpoch") * 1000L;
                            String day = dayFormat.format(new Date(epochTime));
                            String condition = dayData.optString("conditions", "N/A");
                            String dayIcon = dayData.optString("icon", null);
                            String tempString = dayData.optString("temp", "N/A");
                            String temp;
                            if (!tempString.equals("N/A")) {
                                try {
                                    double tempValue = Double.parseDouble(tempString);
                                    temp = String.valueOf(Math.round(tempValue)) + "°C";
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing 5-day forecast temp: " + e.getMessage());
                                    temp = "N/A";
                                }
                            } else {
                                temp = "N/A";
                            }

                            forecastDays.add(day);
                            forecastIcons.add(dayIcon != null ? dayIcon : "");
                            forecastConditions.add(condition);
                            forecastTemps.add(temp);
                            Log.d(TAG, "5-day forecast day " + (i + 1) + ": day=" + day + ", condition=" + condition + ", icon=" + dayIcon + ", temp=" + temp);
                        }
                    } else {
                        Log.e(TAG, "No days data available for 5-day forecast");
                    }

                    resolvedAddress = getCityNameAndCountry(latitude, longitude);

                    return "Success";
                } else {
                    Log.e(TAG, "HTTP request failed with response code: " + responseCode);
                    conn.disconnect();
                    return "HTTP Error: " + responseCode;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        }

        private String getCityNameAndCountry(double latitude, double longitude) {

            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String city = address.getLocality();
                    String country = address.getCountryName();
                    if (city != null && !city.isEmpty() && country != null && !country.isEmpty()) {
                        return city + ", " + country;
                    }
                }
                Log.w(TAG, "Geocoder returned no valid address for Lat: " + latitude + ", Lon: " + longitude);
            } catch (IOException e) {
                Log.e(TAG, "Geocoder error: " + e.getMessage());
            }
            return "Unknown Location";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Success")) {
                locationTextView.setText(resolvedAddress);
                temperatureTextView.setText(temperature);
                conditionsTextView.setText(conditions);
                humidityTextView.setText(humidity);
                windSpeedTextView.setText(windSpeed);
                date.setText(dateTime);
                uvIndex.setText(uvIndexValue);

                String iconUrl = WeatherUtils.mapVisualCrossingIconToOpenWeatherMap(icon);
                if (iconUrl != null) {
                    new FetchIconTask(weatherIcon).execute(iconUrl);
                } else {
                    weatherIcon.setImageBitmap(null);
                }

                Log.d(TAG, "Setting forecast hour 1: temp=" + hourlyTemps[0] + ", conditions=" + hourlyConditions[0] + ", time=" + times[0] + ", icon=" + hourlyIcons[0]);
                hour1TimeTextView.setText(times[0]);
                hour1TempTextView.setText(hourlyTemps[0]);
                fetchIcon(hourlyIcons[0], hour1IconImageView);

                Log.d(TAG, "Setting forecast hour 2: temp=" + hourlyTemps[1] + ", conditions=" + hourlyConditions[1] + ", time=" + times[1] + ", icon=" + hourlyIcons[1]);
                hour2TimeTextView.setText(times[1]);
                hour2TempTextView.setText(hourlyTemps[1]);
                fetchIcon(hourlyIcons[1], hour2IconImageView);

                Log.d(TAG, "Setting forecast hour 3: temp=" + hourlyTemps[2] + ", conditions=" + hourlyConditions[2] + ", time=" + times[2] + ", icon=" + hourlyIcons[2]);
                hour3TimeTextView.setText(times[2]);
                hour3TempTextView.setText(hourlyTemps[2]);
                fetchIcon(hourlyIcons[2], hour3IconImageView);

                Log.d(TAG, "Setting forecast hour 4: temp=" + hourlyTemps[3] + ", conditions=" + hourlyConditions[3] + ", time=" + times[3] + ", icon=" + hourlyIcons[3]);
                hour4TimeTextView.setText(times[3]);
                hour4TempTextView.setText(hourlyTemps[3]);
                fetchIcon(hourlyIcons[3], hour4IconImageView);
            } else {
                Log.e(TAG, "Fetch weather failed: " + result);
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
                locationTextView.setText("Location: Unavailable");
                temperatureTextView.setText("_ _");
                conditionsTextView.setText("Conditions: N/A");
                humidityTextView.setText("N/A");
                windSpeedTextView.setText("N/A");
                date.setText("N/A");
                uvIndex.setText("N/A");
                weatherIcon.setImageBitmap(null);
                hour1TimeTextView.setText("N/A");
                hour1TempTextView.setText("N/A");
                hour1IconImageView.setImageBitmap(null);
                hour2TimeTextView.setText("N/A");
                hour2TempTextView.setText("N/A");
                hour2IconImageView.setImageBitmap(null);
                hour3TimeTextView.setText("N/A");
                hour3TempTextView.setText("N/A");
                hour3IconImageView.setImageBitmap(null);
                hour4TimeTextView.setText("N/A");
                hour4TempTextView.setText("N/A");
                hour4IconImageView.setImageBitmap(null);
            }
        }

        private void fetchIcon(String vcIcon, ImageView imageView) {
            if (vcIcon == null) {
                Log.e(TAG, "Icon is null, setting default placeholder");
                imageView.setImageResource(android.R.drawable.ic_menu_help);
                return;
            }
            String iconUrl = WeatherUtils.mapVisualCrossingIconToOpenWeatherMap(vcIcon);
            if (iconUrl != null) {
                new FetchIconTask(imageView).execute(iconUrl);
            } else {
                Log.e(TAG, "Failed to map icon: " + vcIcon + ", setting default placeholder");
                imageView.setImageResource(android.R.drawable.ic_menu_help);
            }
        }

        private class FetchIconTask extends AsyncTask<String, Void, Bitmap> {
            private ImageView imageView;

            FetchIconTask(ImageView imageView) {
                this.imageView = imageView;
            }

            @Override
            protected Bitmap doInBackground(String... urls) {
                String url = urls[0];
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(input, null, options);
                    input.close();
                    connection.disconnect();
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.connect();
                    input = connection.getInputStream();

                    options.inJustDecodeBounds = false;
                    options.inSampleSize = calculateInSampleSize(options, 100, 100);
                    options.inScaled = true;
                    options.inDensity = options.outWidth;
                    options.inTargetDensity = 160 * options.inSampleSize;
                    return BitmapFactory.decodeStream(input, null, options);
                } catch (IOException e) {
                    Log.e(TAG, "Error fetching icon: " + e.getMessage());
                    return null;
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
                final int height = options.outHeight;
                final int width = options.outWidth;
                int inSampleSize = 1;

                if (height > reqHeight || width > reqWidth) {
                    final int halfHeight = height / 2;
                    final int halfWidth = width / 2;

                    while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                        inSampleSize *= 2;
                    }
                }
                return inSampleSize;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageBitmap(null);
                }
            }
        }
    }
}