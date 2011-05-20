package com.joocy.inflation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InflationActivity extends Activity {
  
    private static final String SERVICE_URL = "http://ukinflation.appspot.com/rpi.json";
    
    private static final String TAG = "inflation";
    
    private TextView textView;
    private ProgressBar progressBar;
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        textView = (TextView)findViewById(R.id.inflationTextView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        
        // Fetch the inflation figure from the online service.
        // We do this on a backgroun thread.
        @SuppressWarnings("unused")
        AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {
          private String responseStr = "Unknown";
          @Override
          protected Void doInBackground(Void... params)
          {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(SERVICE_URL);
            try {
              HttpResponse response = client.execute(get);
              HttpEntity entity = response.getEntity();
              JSONObject jObject = new JSONObject(EntityUtils.toString(entity));
              responseStr = jObject.getString("rpi");
            }
            catch(Exception ex) {         
              Log.e(TAG, ex.toString());
            }
            return null;
          }          
          @Override
          protected void onPostExecute(Void unused) {
            textView.setText(responseStr);
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
          }
        }.execute();
    }
}