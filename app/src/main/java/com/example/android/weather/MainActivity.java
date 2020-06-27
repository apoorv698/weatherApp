package com.example.android.weather;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static String API_ID = APIKey.API_ID;
    private static String URL_CURRENT_TEMPERATURE_BY_CITY = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private static String URL_CURRENT_TEMPERATURE_BY_COORD = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s";
    private static String URL_ICON = "http://openweathermap.org/img/wn/";

    private static String LOG_TAG = MainActivity.class.getSimpleName();

    private EditText searchBox;
    private TextView currTempTextView, feelsLikeTempTextView, pressureTextView, windSpeedTextView;
    private TextView humidityTextView, descriptionTextView, cityNameTextView, generalItemTextView;
    private ImageView weatherIconImageView;
    private RelativeLayout mainRelativeLayout;
    private ProgressBar progressBar;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
        searchBox = (EditText) findViewById(R.id.search_box);
        currTempTextView = (TextView) findViewById(R.id.current_temp);
        feelsLikeTempTextView = (TextView) findViewById(R.id.feels_like);
        pressureTextView = (TextView) findViewById(R.id.pressure);
        windSpeedTextView = (TextView) findViewById(R.id.wind_speed);
        humidityTextView = (TextView) findViewById(R.id.humidity);
        descriptionTextView = (TextView) findViewById(R.id.description);
        cityNameTextView = (TextView) findViewById(R.id.city_name);
        generalItemTextView = (TextView) findViewById(R.id.general_item);
        weatherIconImageView = (ImageView) findViewById(R.id.icon_image);
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.main_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        Button currLocationWeatherButton = (Button) findViewById(R.id.get_location);

        generalItemTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setActivated(false);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format(URL_CURRENT_TEMPERATURE_BY_CITY, searchBox.getText().toString().trim(), API_ID);
                callTemperatureAsyncTask(url);
            }
        });


        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        currLocationWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationTrack = new LocationTrack(MainActivity.this);
                if (locationTrack.canGetLocation()) {
                    String longitude = locationTrack.getLongitude();
                    String latitude = locationTrack.getLatitude();
                    String url = String.format(URL_CURRENT_TEMPERATURE_BY_COORD, latitude, longitude, API_ID);
                    callTemperatureAsyncTask(url);
                    //Toast.makeText(getApplicationContext(), "Longitude:" + longitude + "\nLatitude:" + latitude, Toast.LENGTH_SHORT).show();
                    Log.v(LOG_TAG, url);
                } else {
                    locationTrack.showSettingsAlert();
                }

            }
        });

    }

    void callTemperatureAsyncTask(String url) {
        TemperatureAsyncTask temperatureAsyncTask = new TemperatureAsyncTask(new TemperatureAsyncTask.AsyncResponse() {
            @Override
            public void SyncTaskFinish(WeatherCondition weatherCondition) {
                dumpWeatherOnScreen(weatherCondition);
            }

            @Override
            public void setProgressBarStatus(boolean visible) {
                if (visible) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setActivated(true);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setActivated(false);
                    generalItemTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        // making a callback to get the weatherCondition object from the AsyncTask class
        temperatureAsyncTask.execute(url);
    }

    void dumpWeatherOnScreen(final WeatherCondition weatherCondition) {

        if(weatherCondition.getId()==-1)
        {
            Log.e(LOG_TAG, "WeatherCondition object not initialized.");
            //Toast.makeText(this,"Sorry.. but weather data is not available", Toast.LENGTH_LONG).show();
            return;
        }

        IconLoadAsyncTask iconLoadAsyncTask = new IconLoadAsyncTask(new IconLoadAsyncTask.AsyncResponse() {
            @Override
            public void SyncTaskFinish(Bitmap bitmap) {
                weatherIconImageView.setImageBitmap(bitmap);
                updateUI(weatherCondition.getWeatherStatus());
            }
        });
        iconLoadAsyncTask.execute(URL_ICON+weatherCondition.getIconId()+"@2x.png");

        currTempTextView.setText(String.valueOf((int)(weatherCondition.getCurTemp())) + "°C");
        feelsLikeTempTextView.setText("Feels like... " + String.valueOf((int)weatherCondition.getFeelsLikeTemp()) + "°C");
        pressureTextView.setText("Pressure "+String.valueOf(weatherCondition.getPressure())+" millibar");
        windSpeedTextView.setText("WindSpeed " + String.valueOf((int)(weatherCondition.getWindSpeed()) + "km/h " + weatherCondition.getWindDirection()));
        humidityTextView.setText("Humidity "+String.valueOf(weatherCondition.getHumidity())+"%");
        descriptionTextView.setText(weatherCondition.getDescription());
        cityNameTextView.setText(weatherCondition.getCityName());

    }

    void updateUI(String status) {
        int colorId;
        if(status.compareTo("Thunderstorm")==0)
            colorId = R.drawable.tstorm;
        else if(status.compareTo("Drizzle")==0)
            colorId = R.drawable.drizzle;
        else if(status.compareTo("Rain")==0)
            colorId = R.drawable.rain;
        else if(status.compareTo("Snow")==0)
            colorId = R.drawable.snow;
        else if(status.compareTo("Mist")==0)
            colorId = R.drawable.mist;
        else if(status.compareTo("smoke")==0)
            colorId = R.drawable.smoke;
        else if(status.compareTo("Dust")==0)
            colorId = R.drawable.dust;
        else if(status.compareTo("Fog")==0)
            colorId = R.drawable.fog;
        else if(status.compareTo("Haze")==0)
            colorId = R.drawable.haze;
        else if(status.compareTo("Sand")==0)
            colorId = R.drawable.sand;
        else if(status.compareTo("Tornado")==0)
            colorId = R.drawable.tornado;
        else if(status.compareTo("Clouds")==0)
            colorId = R.drawable.cloudy;
        else
            colorId = R.drawable.clear;
        mainRelativeLayout.setBackgroundResource(colorId);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<>();
        for(int i=0; i<wanted.size(); i++) {
            String perm = wanted.get(i);
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("Location permissions is required for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                        }
                    }

                }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this).setMessage(message)
                .setPositiveButton("OK", okListener).setNegativeButton("Cancel", null).create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }

}
