package com.example.android.weather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created for project Weather
 * by apoorv at 12:41 PM 20-Jun-20 .
 */

class IconLoadAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private static String LOG_TAG = MainActivity.class.getSimpleName();

    interface AsyncResponse {
        void SyncTaskFinish(Bitmap bitmap);
    }

    private AsyncResponse asyncResponse = null;

    IconLoadAsyncTask (AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        URL url = null;
        try {
            url = new URL(strings[0]);
        }catch (Exception ex) {
            Log.e(LOG_TAG, "URL CREATION ERROR: "+ex.toString());
        }

        HttpURLConnection connection = null;
        Bitmap bitmap = null;

        try{
            if(url != null) {
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(50000 /* milliseconds */);
                connection.setConnectTimeout(55000 /* milliseconds */);
                InputStream inputStream = null;
                if (connection.getResponseCode() == 200)
                    inputStream = connection.getInputStream();
                if(inputStream != null) {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                }
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "UNKNOWN EXCEPTION OCCURRED: " + ex.toString());
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        try {
            if (bitmap != null) {
                asyncResponse.SyncTaskFinish(Bitmap.createScaledBitmap(bitmap,250,250,false));
            } else {
                Log.e(LOG_TAG, "Weather icon bitmap is null!");
            }
        }catch (Exception ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }
}
