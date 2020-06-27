package com.example.android.weather;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by apoorv on 29-Mar-20.
 */

class TemperatureAsyncTask extends AsyncTask<String, Void, WeatherCondition> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public interface AsyncResponse {
        void SyncTaskFinish(WeatherCondition weatherCondition);
        void setProgressBarStatus(boolean visible);
    }

    private AsyncResponse asyncResponse;

    TemperatureAsyncTask(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // TODO: 20-Jun-20 start the indeterminate progress button
        asyncResponse.setProgressBarStatus(true);
    }

    @Override
    protected WeatherCondition doInBackground(String... strings) {
        URL url = createURL(strings[0]);
        WeatherCondition weatherCondition = null;
        try{
            String weatherConditionStr = getWeatherFromAPI(url);
            weatherCondition = parseJSON(weatherConditionStr);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "UNKNOWN EXCEPTION OCCURRED: " + ex.toString());
        }

        return weatherCondition;
    }

    @Override
    protected void onPostExecute(WeatherCondition weatherCondition) {
        super.onPostExecute(weatherCondition);
        try {
            asyncResponse.SyncTaskFinish(weatherCondition);
            asyncResponse.setProgressBarStatus(false);
        } catch(Exception ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }

    private URL createURL(String urlString){
        URL url = null;
        try{
            url = new URL(urlString);
        } catch(Exception ex) {
            Log.e(LOG_TAG, "ERROR WHILE CREATING URL: "+ex.toString());
        }
        return url;
    }

    private String getWeatherFromAPI(URL url) throws IOException{
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(30000 /* milliseconds */);
            urlConnection.setConnectTimeout(35000 /* milliseconds */);
            urlConnection.connect();
            if(urlConnection.getResponseCode()==200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else
                jsonResponse = "";
        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR: ",e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        try {
            Log.d("JSON result: ", jsonResponse);
        }catch(Exception e) {
            Log.e(LOG_TAG,"ERROR: "+e);
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private WeatherCondition parseJSON(String json)
    {
        WeatherCondition weatherCondition = new WeatherCondition();
        Log.v(LOG_TAG, json);
        try {
            JSONObject baseJsonResponse = new JSONObject(json);
            int cod = baseJsonResponse.getInt("cod");
            if(cod!=200)
                return null;

            int id = baseJsonResponse.getInt("id");
            String cityName = baseJsonResponse.getString("name");

            JSONArray typesArray = baseJsonResponse.getJSONArray("weather");
            Log.v(LOG_TAG, typesArray.toString());
            String weatherStatus="", description="", iconId="";
            for(int i=0;i<typesArray.length();i++)
            {
                JSONObject arrayElement = typesArray.getJSONObject(i);
                //Log.d(LOG_TAG, arrayElement.toString());
                weatherStatus = arrayElement.getString("main");
                description = arrayElement.getString("description");
                iconId = arrayElement.getString("icon");
            }

            JSONObject mainObj = baseJsonResponse.getJSONObject("main");
            double curTemp = mainObj.getDouble("temp")-273.15; // to celsius
            double feelsLike = mainObj.getDouble("feels_like")-273.15; // to celsius
            double pressure = mainObj.getDouble("pressure");
            double humidity = mainObj.getDouble("humidity");

            JSONObject windObj = baseJsonResponse.getJSONObject("wind");
            double windSpeed = windObj.getDouble("speed");
            double windDirection = windObj.getDouble("deg");

            weatherCondition.initializeObject(id, curTemp, feelsLike, pressure, windSpeed,humidity,
                    description, cityName, weatherStatus, iconId, windDirection);

        } catch (JSONException e) {
            Log.v("JSON OP: ", json);
            Log.e(LOG_TAG, "Problem parsing the weather JSON results", e);
        }

        return weatherCondition;
    }
}
