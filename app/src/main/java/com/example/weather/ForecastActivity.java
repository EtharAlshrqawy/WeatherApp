package com.example.weather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ForecastActivity extends AppCompatActivity {

    private static final String TAG = "ForecastActivity";
    private ListView forecastListView;
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> icons = new ArrayList<>();
    private ArrayList<String> conditions = new ArrayList<>();
    private ArrayList<String> temps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        try {
            // Initialize the ListView
            forecastListView = findViewById(R.id.forecastListView);

            // Get data from intent
            if (getIntent().hasExtra("days") && getIntent().hasExtra("icons") &&
                    getIntent().hasExtra("conditions") && getIntent().hasExtra("temps")) {

                days = getIntent().getStringArrayListExtra("days");
                icons = getIntent().getStringArrayListExtra("icons");
                conditions = getIntent().getStringArrayListExtra("conditions");
                temps = getIntent().getStringArrayListExtra("temps");

                Log.d(TAG, "Received data: " + days.size() + " days, " + icons.size() + " icons, " +
                        conditions.size() + " conditions, " + temps.size() + " temps");

                // Set up the adapter
                ForecastAdapter adapter = new ForecastAdapter();
                forecastListView.setAdapter(adapter);
            } else {
                Log.e(TAG, "Missing data in intent");
                Toast.makeText(this, "Error: Missing forecast data", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ForecastActivity: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class ForecastAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ForecastActivity.this).inflate(R.layout.forecast_row, parent, false);
            }

            TextView dayTextView = convertView.findViewById(R.id.dayTextView);
            TextView tempTextView = convertView.findViewById(R.id.tempTextView);
            TextView conditionsTextView = convertView.findViewById(R.id.conditionsTextView);
            ImageView iconImageView = convertView.findViewById(R.id.iconImageView);

            dayTextView.setText(days.get(position));
            tempTextView.setText(temps.get(position));
            conditionsTextView.setText(conditions.get(position));

            String icon = icons.get(position);
            if (icon != null && !icon.isEmpty()) {
                String iconUrl = WeatherUtils.mapVisualCrossingIconToOpenWeatherMap(icon);
                if (iconUrl != null) {
                    new FetchIconTask(iconImageView).execute(iconUrl);
                } else {
                    iconImageView.setImageResource(android.R.drawable.ic_menu_help);
                }
            } else {
                iconImageView.setImageResource(android.R.drawable.ic_menu_help);
            }

            return convertView;
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
                options.inSampleSize = 2;
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

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_help);
            }
        }
    }
}