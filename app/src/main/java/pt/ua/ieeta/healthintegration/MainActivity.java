package pt.ua.ieeta.healthintegration;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import Bio.Library.namespace.BioLib;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private BioLib lib;
    private View viewmain;
    FloatingActionButton stop;
    FloatingActionButton start;
    private boolean LoggedIN = false;

    Globals g = Globals.getInstance();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SettingOptions settingsOpts;
    private Chronometer chronometer;


    private int SessionID;
    private int hrReadNr;
    private int accReadNr;
    private int ecgReadNr;
    private boolean chronoStarted = false;
    private int countToHandle = 0;

    //get navigationView
    NavigationView navigationView;
    Menu menu;
    MenuItem nav_login;
    MenuItem nav_signup;
    ListView lv;

    TextView textViewUserName;

    StringBuffer responseHR = new StringBuffer();
    StringBuffer responseACC = new StringBuffer();
    StringBuffer responseECG = new StringBuffer();
    Structures dataSessions;


    private HashMap< Integer, HashMap< String, HashMap< Integer, JSONObject>>> allData;



    private final Handler dataHandler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case BioLib.MESSAGE_READ:
                    //Log.d("OnHANDLER - RECEIVED: " ,""+ msg.arg1);
                    break;
                case BioLib.MESSAGE_DATA_UPDATED:
                    if (!chronoStarted && (settingsOpts.isRecordHeartRate() || settingsOpts.isRecordAccelerometer())) {
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        chronoStarted = true;
                    }
                    countToHandle++;
                    if (countToHandle >100) {
                        countToHandle = 0;
                        BioLib.Output out = (BioLib.Output) msg.obj;
                        System.out.println("BATTERY:" + out.battery);
                        handleWithDataHR(out.pulse);
                        handleWithDataACC(out.accValues.X, out.accValues.Y, out.accValues.Z);
                    }
                    break;
                case BioLib.MESSAGE_ECG_STREAM:
                    if (!chronoStarted && settingsOpts.isRecordECG()) {
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        chronoStarted = true;
                    }
                    handleWithDataECG((byte[][]) msg.obj);
                    break;
                case BioLib.STATE_CONNECTED:
                    Snackbar.make(viewmain, "Connected with Device!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    SessionID = getCurrentSession();
                    hrReadNr = 0;
                    ecgReadNr = 0;
                    accReadNr = 0;
                    start.setEnabled(false);
                    start.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentGrey)));
                    stop.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    stop.setEnabled(true);

                    break;
                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    Snackbar.make(viewmain, "Unable to connect with device", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    break;
                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    Snackbar.make(viewmain, "Disconnected to device VJ", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    chronometer.stop();
                    chronometer.setVisibility(View.INVISIBLE);
                    chronoStarted = false;
                    fillPreviousSessions();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = getSharedPreferences("info", MODE_PRIVATE);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menu = navigationView.getMenu();
        nav_signup = menu.findItem(R.id.sign_up);
        start = (FloatingActionButton) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewmain = view;
                reloadSettingOptions();

                if (isBluetoothEnabled()) {
                    if(settingsOpts.isUseThisDevice()) {
                        if (LoggedIN) {
                            connectToDevice(settingsOpts.getDeviceAddress());
                        }else {
                            Snackbar.make(view, "You should Login first", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    } else {
                        Snackbar.make(view, "Enable Device at settings!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    Snackbar.make(view, "Bluetooth is disabled.. Turn it On!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        stop = (FloatingActionButton) findViewById(R.id.stop);
        stop.setEnabled(false);
        stop.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentGrey)));
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewmain = view;
                stop.setEnabled(false);
                stop.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccentGrey)));
                start.setEnabled(true);
                start.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                System.out.println("On stop");
                disconnectToDevice();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 1) { //Login activity result
            nav_login = menu.findItem(R.id.sign_in);
            nav_login.setChecked(false);
            if(resultCode == Activity.RESULT_OK){

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String physicalActivity = settings.getString("physical_activity", "at rest");

                textViewUserName = (TextView) findViewById(R.id.userTextName);
                textViewUserName.setText(g.getUsername() + " - " + physicalActivity);
                nav_login.setTitle("Logout");
                LoggedIN = true;
                fillPreviousSessions();
                nav_signup.setVisible(false);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        } else if ( requestCode == 2) { //sign up activity result

            nav_signup.setChecked(false);
        } else if (requestCode == 3) { //settings activity result
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String physicalActivity = settings.getString("physical_activity", "at rest");
            textViewUserName = (TextView) findViewById(R.id.userTextName);
            textViewUserName.setText(g.getUsername() + " - " + physicalActivity);
        }
    }

    private void reloadSettingOptions() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String device_address = settings.getString("sync_frequency", "1LO1100006");
        Boolean toUse = settings.getBoolean("device_connected", false);
        Boolean toUseHeartRate = settings.getBoolean("save_heart_rate", false);
        Boolean toUseECG= settings.getBoolean("save_ecg", false);
        Boolean toUseACC = settings.getBoolean("save_acc", false);
        String physicalActivity = settings.getString("physical_activity", "at rest");
        settingsOpts = new SettingOptions(device_address, toUse, toUseHeartRate, toUseECG, toUseACC,physicalActivity);
        }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent,3);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.sign_in) {
            if (! LoggedIN) {
                Intent intent = new Intent(this, Login.class);
                startActivityForResult(intent, 1);
            } else {
                nav_login.setTitle("Login");
                nav_login.setChecked(false);
                textViewUserName.setText("Do Login");
                LoggedIN = false;
                lv.setVisibility(View.INVISIBLE);
                nav_signup.setVisible(true);

            }

        } else if (id == R.id.sign_up) {
            Intent intent = new Intent(this, Signup.class);
            startActivityForResult(intent, 2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }

    public void connectToDevice(String address) {
        System.out.println("connecting to "+ address);
        try {
            lib = new BioLib(this, dataHandler);

            Boolean connected = lib.Connect(address,5);
            System.out.println("Connected!!!!!!!!!!!!!!!11"+connected);
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed!!!!!!!!!!!!!!!11");

        }
    }

    public void disconnectToDevice() {
        System.out.println("disconnecting");
        try {
            lib.Disconnect();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed!!!!!!!!!!!!!!!11");
        }
    }

    public int getCurrentSession (){

        int session = 0;
        try {
            session = pref.getInt(g.getUsername(), 0);
        }catch(Exception e){

        }

        if (session == 0) {
            editor = pref.edit();
            editor.putInt(g.getUsername(),1);
            editor.commit();
        }else {
            editor = pref.edit();
            editor.putInt(g.getUsername(),pref.getInt(g.getUsername(), 0) + 1);
            editor.commit();
        }
        return session;
    }

    public void handleWithDataHR(int heart_rate) {

       if(settingsOpts.isRecordHeartRate()) {
           new heartRateSendtoDBTask().execute("" + heart_rate);
       }

    }

    public void handleWithDataACC(byte accX, byte accY, byte accZ) {

        if (settingsOpts.isRecordAccelerometer()) {
            new accelerometerSendtoDBTask().execute( ""+accX, ""+accY, ""+accZ );
        }

    }

    public void handleWithDataECG(byte[][] ecg){
        if (settingsOpts.isRecordECG()) {
            byte[] ecg_data = ecg[0];
            int [] data = new int [500];
            for(int i=0;i<500;i++){
                data[i]=ecg_data[i] & 0xFF;
            }

            //TODO: Here send data to AsyncTask

            //System.out.println("OnHANDLER-ECG:"+Arrays.toString(data));
            new ecgSendtoDBTask().execute(Arrays.toString(data));
        }
    }

    private class heartRateSendtoDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            Log.d("ON AsyncTask","IN");
            JSONObject json=new JSONObject();
            JSONArray values = new JSONArray();

            String domain = getResources().getString(R.string.server_ip);
            StringBuilder received = new StringBuilder();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            String dateString = sdf.format(new Date());

            JSONObject data = null;
            try {
                JSONObject header = null;
                JSONObject provenance = null;
                JSONObject schema_id = null;
                JSONObject time_frame = null;
                JSONObject heart_rate = null;
                JSONObject body = null;
                provenance  = new JSONObject().put("source_name","MHealthIntegration-App").put("source_creation_date_time",dateString+"Z").put ("modality","sensed");
                schema_id = new JSONObject().put("namespace", "omh").put("name","heart-rate").put("version","1.0");

                header = new JSONObject().put("id", UUID.randomUUID().toString()).
                        put("acquisition_provenance",provenance).
                        put("schema_id",schema_id).put("user_id", g.getUsername());

                time_frame = new JSONObject().put("date_time",dateString+"Z");
                heart_rate = new JSONObject().put("unit","beats/min").
                        put("value",Integer.parseInt(params[0])).
                        put("session", SessionID).
                        put("part_number", ++hrReadNr);

                body = new JSONObject().put("effective_time_frame", time_frame).put("heart_rate", heart_rate).put("temporal_relationship_to_physical_activity", settingsOpts.getPhysicalActivity());

                data = new JSONObject().put("header", header).
                        put("body", body);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                URL url = new URL("http://"+domain+":8083/v1.0.M1/dataPoints");
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization","Bearer "+g.getToken());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                String input = data.toString();
                Log.d("PUT DataPoint:",input);

                OutputStream os=null;
                os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    System.out.println("Error:"+conn.getResponseCode());
                    return false;
                }
                else{
                    System.out.println("HR - success - "+SessionID+ " - " +hrReadNr);
                }

                conn.disconnect();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;

        }

    }

    private class accelerometerSendtoDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            Log.d("ON AsyncTask","IN");
            JSONObject json=new JSONObject();

            String domain = getResources().getString(R.string.server_ip);
            StringBuilder received = new StringBuilder();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            String dateString = sdf.format(new Date());

            JSONObject data = null;
            try {
                JSONObject header = null;
                JSONObject provenance = null;
                JSONObject schema_id = null;
                JSONObject time_frame = null;
                JSONObject accelerometer = null;
                JSONObject values = null;
                JSONObject body = null;
                provenance  = new JSONObject().put("source_name","MHealthIntegration-App").put("source_creation_date_time",dateString+"Z").put ("modality","sensed");
                schema_id = new JSONObject().put("namespace", "omh").put("name","accelerometer").put("version","1.0");

                header = new JSONObject().put("id", UUID.randomUUID().toString()).
                        put("acquisition_provenance",provenance).
                        put("schema_id",schema_id).put("user_id", g.getUsername());

                time_frame = new JSONObject().put("date_time",dateString+"Z");
                values = new JSONObject().put("x", Integer.parseInt(params[0])).put("y", Integer.parseInt(params[1])).put("z", Integer.parseInt(params[2]));

                accelerometer = new JSONObject().put("unit","mS").
                        put("values",values).
                        put("session", SessionID).
                        put("part_number", ++accReadNr);

                body = new JSONObject().put("effective_time_frame", time_frame).put("accelerometer", accelerometer).put("temporal_relationship_to_physical_activity", settingsOpts.getPhysicalActivity());

                data = new JSONObject().put("header", header).
                        put("body", body);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL("http://"+domain+":8083/v1.0.M1/dataPoints");
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization","Bearer "+g.getToken());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();

                String input = data.toString();
                Log.d("PUT DataPoint:",input);

                OutputStream os=null;
                os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    System.out.println("Error:"+conn.getResponseCode());
                    return false;
                }
                else{
                    System.out.println("ACC - success - "+SessionID+ " - " +accReadNr);
                }

                conn.disconnect();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;

        }

    }

    private class ecgSendtoDBTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            Log.d("ON AsyncTask","IN");
            JSONObject json=new JSONObject();
            JSONArray values = new JSONArray();

            String domain = getResources().getString(R.string.server_ip);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            String dateString = sdf.format(new Date());

            JSONObject header = null;
            JSONObject data = null;
            JSONObject provenance = null;
            JSONObject schema_id = null;
            JSONObject time_frame = null;
            JSONObject ecg_data = null;
            JSONObject body = null;
            Log.d("json array",params[0]);
            try {
                provenance  = new JSONObject().put("source_name","MHealthIntegration-App").put("source_creation_date_time",dateString+"Z").put ("modality","sensed");
                schema_id = new JSONObject().put("namespace", "omh").put("name","ecg").put("version","1.0");
                header = new JSONObject().put("id", UUID.randomUUID().toString()).
                        put("acquisition_provenance",provenance).
                        put("schema_id",schema_id);


                time_frame = new JSONObject().put("date_time",dateString+"Z");
                JSONArray array = new JSONArray(params[0]);

                ecg_data = new JSONObject().put("unit","uV").
                        put("values",array).put("part_number",++ecgReadNr).put("session", SessionID);

                body = new JSONObject().put("effective_time_frame", time_frame).put("ecg", ecg_data).put("temporal_relationship_to_physical_activity", settingsOpts.getPhysicalActivity());

                data = new JSONObject().put("header", header).
                        put("body", body);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL("http://"+domain+":8083/v1.0.M1/dataPoints");
                HttpURLConnection conn = null;
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization","Bearer "+g.getToken());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();


                String input = data.toString();
                Log.d("PUT DataPoint", input);

                OutputStream os=null;

                os = conn.getOutputStream();
                os.write(input.getBytes());
                os.flush();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                    System.out.println("Error:"+conn.getResponseCode());
                    return false;
                }
                else{
                    System.out.println("ECG - success - "+SessionID+ " - " +ecgReadNr);
                }
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

    }

    public void fillPreviousSessions() {

        try {
            dataSessions = new Structures();
            if (new getHeartRatePoints().execute().get()) {
                dataSessions.fillDataWithHR(responseHR.toString());

                System.out.println("hr received:" + responseHR.toString());

                if (new getAccPoints().execute().get()) {

                    dataSessions.fillDataWithACC(responseACC.toString());

                    System.out.println("acc received:" + responseACC.toString());

                    if (new getEcgPoints().execute().get()) {
                        dataSessions.fillDataWithECG(responseECG.toString());

                        System.out.println("ecg received:" + responseECG.toString());

                        fillListWithSessions();
                    }
                }
            }
        } catch (InterruptedException e) {

        } catch (ExecutionException e) {

        }
    }

    public void fillListWithSessions() {

        allData = dataSessions.getAllDataBySession();
        int[] listImgs = new int[allData.size()];
        String [] prgmNameList = new String[allData.size()];
        String [] timeList = new String[allData.size()];
        String [] dateList = new String[allData.size()];


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        //get all sessions
        int[] allSessionKeys = new int [allData.size()];
        int x = 0;
        for (int sessionKey : allData.keySet()) {
            System.out.println("key:"+sessionKey);
            allSessionKeys[x] = sessionKey;
            x++;
        }
        Arrays.sort(allSessionKeys);


        int i = allSessionKeys.length-1;
        int entryList = 0;
        for (; i>=0;i--) {

            int sessionKey = allSessionKeys[i];
            prgmNameList[entryList]=""+sessionKey;



            for (String readDP: allData.get(sessionKey).keySet())
            {
                System.out.println("Session:" + sessionKey+ " - readDP:" + readDP);
                int max = 1;
                for (int part_number: allData.get(sessionKey).get(readDP).keySet()) {
                    if (part_number> max) {
                        max = part_number;
                    }
                }
                JSONObject minRead = allData.get(sessionKey).get(readDP).get(1);
                JSONObject maxRead = allData.get(sessionKey).get(readDP).get(max);

                try {
                    String physical_activity = minRead.getString("temporal_relationship_to_physical_activity");
                    switch (physical_activity) {
                        case "at rest": listImgs[entryList] = R.drawable.icons_rest; break;
                        case "active": listImgs[entryList] = R.drawable.icons_active; break;
                        case "before exercise": listImgs[entryList] = R.drawable.icons_before_exercise; break;
                        case "after exercise": listImgs[entryList] = R.drawable.icons_after_exercise; break;
                        case "during exercise": listImgs[entryList] = R.drawable.icons_exercise; break;
                    }
                } catch (JSONException e) {

                }
                try {
                    System.out.println("TEST" + minRead.getJSONObject("effective_time_frame").getString("date_time"));
                    System.out.println("TEST"+maxRead.getJSONObject("effective_time_frame").getString("date_time"));

                    Date dateInit = sdf.parse(minRead.getJSONObject("effective_time_frame").getString("date_time"));
                    Date dateEnd = sdf.parse(maxRead.getJSONObject("effective_time_frame").getString("date_time"));
                    int seconds = (int) (getDateDiff(dateInit,dateEnd,TimeUnit.MILLISECONDS) / 1000) % 60 ;
                    int minutes = (int) ((getDateDiff(dateInit,dateEnd,TimeUnit.MILLISECONDS) / (1000*60)) % 60);
                    int hours   = (int) ((getDateDiff(dateInit,dateEnd,TimeUnit.MILLISECONDS) / (1000*60*60)) % 24);

                    timeList[entryList] = "Time: "+hours+ ":" + minutes + ":" + seconds;
                    dateList[entryList] = df.format(dateInit);

                } catch (JSONException e) {

                } catch (ParseException e) {

                }
                break;
            }
            entryList++;

        }


        g.setAllDataBySession(allData);
        lv=(ListView) findViewById(R.id.listSession);
        lv.setAdapter(new CustomAdapter(this, prgmNameList,listImgs, timeList, dateList ));
        lv.setVisibility(View.VISIBLE);
    }


    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    private class getHeartRatePoints extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            String domain = getResources().getString(R.string.server_ip);

            responseHR = new StringBuffer();
            try {
                String url = "http://" + domain + ":8083/v1.0.M1/dataPoints?schema_namespace=omh&schema_name=heart-rate&schema_version=1.0";

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setDoOutput(false);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + g.getToken());
                con.setRequestProperty("Content-Type", "application/json");

                System.out.println(con.getHeaderFields().toString());
                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;


                while ((inputLine = in.readLine()) != null) {
                    responseHR.append(inputLine);
                }
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;

        }
    }

    private class getAccPoints extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            String domain = getResources().getString(R.string.server_ip);

            responseACC = new StringBuffer();
            try {
                String url = "http://" + domain + ":8083/v1.0.M1/dataPoints?schema_namespace=omh&schema_name=accelerometer&schema_version=1.0";

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setDoOutput(false);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + g.getToken());
                con.setRequestProperty("Content-Type", "application/json");

                System.out.println(con.getHeaderFields().toString());
                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;


                while ((inputLine = in.readLine()) != null) {
                    responseACC.append(inputLine);
                }
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;

        }
    }

    private class getEcgPoints extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {

            String domain = getResources().getString(R.string.server_ip);

            responseECG = new StringBuffer();
            try {
                String url = "http://" + domain + ":8083/v1.0.M1/dataPoints?schema_namespace=omh&schema_name=ecg&schema_version=1.0";

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                //add request header
                con.setDoOutput(false);
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + g.getToken());
                con.setRequestProperty("Content-Type", "application/json");

                System.out.println(con.getHeaderFields().toString());
                int responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;


                while ((inputLine = in.readLine()) != null) {
                    responseECG.append(inputLine);
                }
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;

        }
    }

}