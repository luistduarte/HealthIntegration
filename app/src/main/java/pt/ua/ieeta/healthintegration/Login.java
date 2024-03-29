package pt.ua.ieeta.healthintegration;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {

    Globals g = Globals.getInstance();
    private EditText username,password;
    private String usernameSrt,passwordStr;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();
    }
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void doLogin(View v) {


        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);

        String usernameStr = username.getText().toString();
        String passwordStr = password.getText().toString();
        Boolean test = false;
        try {
            test = new checkCredentialsDBTask().execute(usernameStr, passwordStr).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (test) {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Logged In with " + usernameStr, Toast.LENGTH_LONG);
            toast.show();
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
        else {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Try again, invalid credentials", Toast.LENGTH_LONG);
            toast.show();

        }

    }

    private class checkCredentialsDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            Log.d("ON AsyncTask","IN"+params[0]);
            JSONObject json=new JSONObject();
            JSONArray values = new JSONArray();

            String domain = getResources().getString(R.string.server_ip);
            StringBuilder received = new StringBuilder();
            try {

                URL url = new URL("http://"+domain+":8082/oauth/token");
                Map<String,Object> requestParams = new LinkedHashMap<>();
                requestParams.put("grant_type", "password");
                requestParams.put("username", params[0]);
                requestParams.put("password", params[1]);
                //requestParams.put("username", "omh1");
                //requestParams.put("password", "omh1");


                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : requestParams.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Accept","application/json");
                conn.setRequestProperty("Authorization","Basic dGVzdENsaWVudDp0ZXN0Q2xpZW50U2VjcmV0");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));


                for (int c; (c = in.read()) >= 0;)
                    received.append(Character.toChars(c));

                conn.disconnect();

                Log.d("ON AsyncTask-AFTERLOGIN", received.toString());

                String accessToken = new JSONObject(received.toString()).get("access_token").toString();


                Log.d("ON AsyncTask - token:",accessToken);
                g.setData(accessToken);
                g.setUsername(params[0]);

                if (! accessToken.equals("")) {
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

}
