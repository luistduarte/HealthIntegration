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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class Signup extends AppCompatActivity {

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
        setContentView(R.layout.activity_signup);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void doSignUp(View v) {


        EditText username = (EditText) findViewById(R.id.username);
        EditText password1 = (EditText) findViewById(R.id.password1);
        EditText password2 = (EditText) findViewById(R.id.password2);


        String usernameStr = username.getText().toString();
        String passwordStr1 = password1.getText().toString();
        String passwordStr2 = password2.getText().toString();

        if(usernameStr.length() < 4) {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Username too short!", Toast.LENGTH_LONG);
            toast.show();
        } else if(! passwordStr1.equals(passwordStr2) ) {
            Toast toast = Toast.makeText(this.getApplicationContext(), "Passwords must be equals!", Toast.LENGTH_LONG);
            toast.show();
        } else if (passwordStr1.length() < 6){
            Toast toast = Toast.makeText(this.getApplicationContext(), "Password too short", Toast.LENGTH_LONG);
            toast.show();
        } else {
            int test = 0;
            try {
                test = new checkCredentialsDBTask().execute(usernameStr, passwordStr1).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (test==1) {
                Toast toast = Toast.makeText(this.getApplicationContext(), "Signed UP a new user " + usernameStr, Toast.LENGTH_LONG);
                toast.show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (test == 2) {
                Toast toast = Toast.makeText(getApplicationContext(), "Already exist, Choose another one!", Toast.LENGTH_LONG);
                toast.show();
            } else if (test == 3) {
                Toast toast = Toast.makeText(getApplicationContext(), "Network problems?! Try Again!", Toast.LENGTH_LONG);
                toast.show();
            }
        }


    }

    private class checkCredentialsDBTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {

            JSONObject json=new JSONObject();
            String domain = getResources().getString(R.string.server_ip);

            try {
                json.put("username", params[0]).put("password", params[1]);

                URL url = new URL("http://"+domain+":8082/users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                String input = json.toString();
                Log.d("new user:",input);

                OutputStream os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                    System.out.println("created with Success");
                    conn.disconnect();
                    return 1;
                }
                else if (conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){
                    conn.disconnect();
                    return 2;

                } else {
                    System.out.println("Error:"+conn.getResponseCode());
                    conn.disconnect();
                    return 3;
                }
            } catch (JSONException | UnsupportedEncodingException | MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;

        }

    }

}
