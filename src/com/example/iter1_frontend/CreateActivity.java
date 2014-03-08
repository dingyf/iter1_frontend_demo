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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: dyf
 * Date: 3/8/14
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateActivity extends Activity {
    TextView message;
    private SharedPreferences mPreferences;
    private String auth_url;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creatactivity);
        auth_url = getIntent().getStringExtra("auth_info");
        message = (TextView) findViewById(R.id.message);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    public void createActivity(View view){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new CreateTask().execute(MyActivity.BASE_URL + MyActivity.ACTIVITY_URL + auth_url);
        } else {
            message.setText("No network connection available.");
        }
        this.finish();

    }

    private class CreateTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {

            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            try {

                // setup the returned values in case
                // something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");
                // add the user email and password to
                // the params
                EditText et_name = (EditText) findViewById(R.id.name);
                EditText et_location = (EditText) findViewById(R.id.location);
                EditText et_description = (EditText) findViewById(R.id.description);
                EditText et_visibility = (EditText) findViewById(R.id.visibility);
                String name = et_name.getText().toString();
                String location = et_location.getText().toString();
                String description = et_description.getText().toString();
                int visibility = Integer.parseInt( et_visibility.getText().toString() );

                userObj.put("name", name);
                userObj.put("location", location);
                userObj.put("description", description);
                userObj.put("visibility", visibility);
                holder.put("activity", userObj);
                StringEntity se = new StringEntity(holder.toString());
                post.setEntity(se);

                // setup the request headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = (String) client.execute(post, responseHandler);
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

//                    Intent intent = new Intent(getApplicationContext(), DisplayUserInfo.class);
//                    intent.putExtra("auth_info", auth_url);
//                    startActivity(intent);
                    finish();
                }
                message.setText("create activity failed: " + json.getString("info"));
            } catch (Exception e) {
                message.setText("exception: " + e.getMessage());
            }
        }
    }
}