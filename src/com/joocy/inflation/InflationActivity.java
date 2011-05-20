package com.joocy.inflation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InflationActivity extends Activity {

    private static final String SERVICE_URL    = "http://ukinflation.appspot.com/rpi.json";
    private static final String TAG            = "inflation";
    private static final String PREFS          = "inflation_prefs";
    private static final String RPI_KEY        = "rpi";
    private static final String RPI_TSTAMP_KEY = "rpi_date";
    private static final String RPI_DEFAULT    = "unknown";
    private static final long   ONE_HOUR       = 3600000;

    private TextView            textView;
    private ProgressBar         progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView) findViewById(R.id.inflationTextView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        textView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String rpi = prefs.getString(RPI_KEY, RPI_DEFAULT);
        Long rpi_tstamp = prefs.getLong(RPI_TSTAMP_KEY, System.currentTimeMillis());
        if (rpi.equals(RPI_DEFAULT) || System.currentTimeMillis() - rpi_tstamp > ONE_HOUR) {
            Log.d(TAG, "Cache miss. Fetching from service");
            fetchRPI();
        }
        else {
            Log.d(TAG, "Using cached rpi");
            displayRPI(rpi);
        }
    }

    private void fetchRPI() {
        new AsyncTask<Void, Integer, Void>() {
            private String responseStr = "Unknown";

            @Override
            protected Void doInBackground(Void... params) {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(SERVICE_URL);
                try {
                    HttpResponse response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    JSONObject jObject = new JSONObject(EntityUtils.toString(entity));
                    responseStr = jObject.getString("rpi");
                    cacheRPI(responseStr);
                }
                catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                displayRPI(responseStr);
            }
        }.execute();
    }

    private void cacheRPI(final String rpi) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(RPI_KEY, rpi);
        editor.putLong(RPI_TSTAMP_KEY, System.currentTimeMillis());
        editor.commit();
    }

    private void displayRPI(final String rpi) {
        textView.setText(rpi);
        progressBar.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);
    }
}