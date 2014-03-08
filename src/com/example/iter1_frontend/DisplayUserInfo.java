package com.example.iter1_frontend;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: dyf
 * Date: 2/18/14
 * Time: 9:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class DisplayUserInfo extends Activity {

    TextView message;
    private SharedPreferences mPreferences;
    private String auth_url;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo);
        auth_url = getIntent().getStringExtra("auth_info");
        message = (TextView) findViewById(R.id.textView);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        message.setText("Welcome!");
    }



    public void logout(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new LogoutTask().execute(MyActivity.BASE_URL + MyActivity.LOGIN_URL + auth_url);
        } else {
            message.setText("No network connection available.");
        }
        this.finish();
    }

    public void myActivities(View view){
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RequestTask().execute(MyActivity.BASE_URL + MyActivity.ACTIVITY_URL + auth_url);
        } else {
            message.setText("No network connection available.");
        }

    }

    public void createActivities(View view){
        Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
        intent.putExtra("auth_info", auth_url);
        startActivity(intent);
    }


    private class RequestTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {

            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(urls[0]);
            JSONObject json = new JSONObject();
            String response = null;

            try {

                // setup the returned values in case
                // something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                // setup the request headers
                get.setHeader("Accept", "application/json");
                get.setHeader("Content-type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = (String) client.execute(get, responseHandler);
                json = new JSONObject(response);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Unable to retrieve web page. URL may be invalid.", "" + e);
            }

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try{
//                debug
                if (json.getBoolean("success")) {
                    // everything is ok

                    // launch the HomeActivity and close this one
                    if (json.getString("data") == null) {
                        message.setText("no activities for this user");
                    } else {
                        message.setText(json.getString("data"));
                    }
                } else{
                    message.setText("find my activities failed: " + json.getString("info"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                message.setText("exception: " + e.getMessage());
            }
        }
    }





    private class LogoutTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {

            DefaultHttpClient client = new DefaultHttpClient();
            HttpDelete delete = new HttpDelete(urls[0]);
            String response = null;
            JSONObject json = new JSONObject();

            try {

                // setup the returned values in case
                // something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                // setup the request headers
                delete.setHeader("Accept", "application/json");
                delete.setHeader("Content-type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = (String) client.execute(delete, responseHandler);
                json = new JSONObject(response);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Unable to retrieve web page. URL may be invalid.", "" + e);
            }

            return json;
        }


        @Override
        protected void onPostExecute(JSONObject json) {
            try{
                if (json.getBoolean("success")) {
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
//                    JSONObject user = json.getJSONObject("data").getJSONObject("user");
                    editor.remove("token");
                    editor.remove("email");
                    editor.commit();

                    // launch the HomeActivity and close this one
                    Intent intent = new Intent(getApplicationContext(), MyActivity.class);
//                    intent.putExtra("auth_info", "?auth_token=" + mPreferences.getString("token", "") +
//                            "&email=" + mPreferences.getString("email", ""));
                    startActivity(intent);
                    finish();
                }
                message.setText(json.getString("info"));
            } catch (Exception e) {
                message.setText(e.getMessage());
            }
        }
    }



}


