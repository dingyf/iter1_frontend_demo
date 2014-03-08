package com.example.iter1_frontend;


//import android.R;
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
import org.json.*;

public class MyActivity extends Activity {

    public final static String LOGIN_URL = "sessions";
    public final static String REGISTER_URL = "registrations";
    public final static String ACTIVITY_URL = "activities";


    public final static String BASE_URL = "http://192.168.56.1:3000/api/v1/";
    private SharedPreferences mPreferences;
    private TextView message;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        message = (TextView) findViewById(R.id.textView);
        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);

        if (mPreferences.contains("token")) {
            Intent intent = new Intent(getApplicationContext(), DisplayUserInfo.class);
            intent.putExtra("auth_info", "?token=" + mPreferences.getString("token", "") +
                                         "&email=" + mPreferences.getString("email", ""));
            startActivity(intent);
            finish();
        }

    }


    public void login(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new LoginTask().execute(BASE_URL + LOGIN_URL);
        } else {
            message.setText("No network connection available.");
        }

    }


    public void register(View view) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
        finish();
    }



    public void clearText() {
        ((EditText) findViewById(R.id.username)).setText("");
        ((EditText) findViewById(R.id.password)).setText("");
    }




    private class LoginTask extends AsyncTask<String, Void, JSONObject> {
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
                String un = et_un.getText().toString();
                String pw = et_pw.getText().toString();

                userObj.put("email", un);
                userObj.put("password", pw);
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
                message.setText("login failed: " + json.getString("info"));
            } catch (Exception e) {
                message.setText("exception: " + e.getMessage());
            }
        }
    }
}
