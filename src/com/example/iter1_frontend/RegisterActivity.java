package com.example.iter1_frontend;

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
import android.widget.Toast;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: dyf
 * Date: 3/7/14
 * Time: 7:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegisterActivity extends Activity {

    private SharedPreferences mPreferences;
    private TextView message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        message = (TextView) findViewById(R.id.textView);

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
    }

    public void sign_up(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RegisterTask().execute(MyActivity.BASE_URL + MyActivity.REGISTER_URL);
        } else {
            message.setText("No network connection available.");
        }
    }




    public void clearText() {
        ((EditText) findViewById(R.id.username)).setText("");
        ((EditText) findViewById(R.id.password)).setText("");
        ((EditText) findViewById(R.id.password2)).setText("");
    }




    private class RegisterTask extends AsyncTask<String, Void, JSONObject> {
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
                EditText et_un = (EditText) findViewById(R.id.username);
                EditText et_pw = (EditText) findViewById(R.id.password);
                EditText et_pw2 = (EditText) findViewById(R.id.password2);
                String un = et_un.getText().toString();
                String pw = et_pw.getText().toString();
                String pw2 = et_pw2.getText().toString();

                userObj.put("email", un);
                userObj.put("password", pw);
                userObj.put("password_confirmation", pw2);
                holder.put("user", userObj);
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
                clearText();

                if (json.getBoolean("success")) {
                    // everything is ok
                    SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    JSONObject user = json.getJSONObject("data");
                    editor.putString("token", user.getString("auth_token"));
                    editor.putString("email", user.getString("email"));
                    editor.commit();

                    // launch the HomeActivity and close this one
                    Intent intent = new Intent(getApplicationContext(), DisplayUserInfo.class);
                    intent.putExtra("auth_info", "?token=" + mPreferences.getString("token", "") +
                            "&email=" + mPreferences.getString("email", ""));
                    startActivity(intent);
                    finish();
                }
                message.setText("sign up failed: " + json.getString("info"));
            } catch (Exception e) {
                message.setText("exception: " + e.getMessage());
            }
        }
    }








}