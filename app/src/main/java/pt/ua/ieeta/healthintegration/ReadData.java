package pt.ua.ieeta.healthintegration;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import Bio.Library.namespace.BioLib;

public class ReadData extends AppCompatActivity {

    Globals g = Globals.getInstance();
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SettingOptions settingsOpts;
    private Chronometer chronometer;
    private BioLib lib;

    private int SessionID;
    private int hrReadNr;
    private int accReadNr;
    private int ecgReadNr;
    private boolean chronoStarted = false;
    private int countToHandle = 0;
    private TextView textHR;
    private TextView textX;
    private TextView textY;
    private TextView textZ;
    private TextView textACC;
    private TextView status;


    private LinearLayout lytHR;
    private LinearLayout lytACC;
    private LinearLayout lytXYZ;



    private GraphicalView chartView;
    private boolean flag;
    private int count=0;
    private byte[] allbytes = new byte[2500];
    XYSeries series = new XYSeries("ECG");



    private final Handler dataHandler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case BioLib.MESSAGE_DATA_UPDATED:
                    if (!chronoStarted && (settingsOpts.isRecordHeartRate() || settingsOpts.isRecordAccelerometer())) {
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        chronoStarted = true;
                        SessionID = getCurrentSession();
                    }


                    countToHandle++;
                    if (countToHandle >100) {

                        countToHandle = 0;
                        BioLib.Output out = (BioLib.Output) msg.obj;
                        //System.out.println("BATTERY:" + out.battery);

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
                        SessionID = getCurrentSession();
                    }

                    handleWithDataECG((byte[][]) msg.obj);
                    break;
                case BioLib.STATE_CONNECTED:
                    hrReadNr = 0;
                    ecgReadNr = 0;
                    accReadNr = 0;
                    status.setText("Connected");
                    break;
                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    Intent returnIntent1 = new Intent();
                    setResult(100, returnIntent1);
                    finish();
                    break;
                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    Intent returnIntent2 = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent2);
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_data);
        setupActionBar();
        reloadSettingOptions();
        pref = getSharedPreferences("info", MODE_PRIVATE);
        chronometer = (Chronometer) findViewById(R.id.chronometer);

        textX = (TextView) findViewById(R.id.value_X);
        textY = (TextView) findViewById(R.id.value_Y);
        textZ = (TextView) findViewById(R.id.value_Z);
        textACC = (TextView) findViewById(R.id.value_ACC);
        textHR = (TextView) findViewById(R.id.value_HR);
        status = (TextView) findViewById(R.id.lbl_status);
        lytHR = (LinearLayout) findViewById(R.id.lytHR);
        lytACC = (LinearLayout) findViewById(R.id.lytACC);
        lytXYZ = (LinearLayout) findViewById(R.id.lytXYZ);

        connectToDevice(settingsOpts.getDeviceAddress());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.stop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectToDevice();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                disconnectToDevice();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        disconnectToDevice();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
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

    private void reloadSettingOptions() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ReadData.this);
        String device_address = settings.getString("sync_frequency", "1LO1100006");
        Boolean toUse = settings.getBoolean("device_connected", false);
        Boolean toUseHeartRate = settings.getBoolean("save_heart_rate", false);
        Boolean toUseECG= settings.getBoolean("save_ecg", false);
        Boolean toUseACC = settings.getBoolean("save_acc", false);
        String physicalActivity = settings.getString("physical_activity", "at rest");
        settingsOpts = new SettingOptions(device_address, toUse, toUseHeartRate, toUseECG, toUseACC,physicalActivity);
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

    public void handleWithDataHR(int heart_rate) {

        if(settingsOpts.isRecordHeartRate()) {

            lytHR.setVisibility(View.VISIBLE);
            String pulse = "" + heart_rate;
            textHR.setText(pulse);

            new heartRateSendtoDBTask().execute("" + heart_rate);
        }
    }

    public void handleWithDataACC(byte accX, byte accY, byte accZ) {

        if (settingsOpts.isRecordAccelerometer()) {
            lytACC.setVisibility(View.VISIBLE);
            lytXYZ.setVisibility(View.VISIBLE);

            String x_value = "" + accX;
            String y_value = "" + accY;
            String z_value = "" + accZ;

            textX.setText(x_value);
            textY.setText(y_value);
            textZ.setText(z_value);

            Float accelationSquareRoot = (accX * accX + accY * accY + accZ * accZ) /
                    (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            double acceleration = Math.sqrt(accelationSquareRoot);
            double res = acceleration-3.456522103440785;
            if (res < 0.0) {
                res = 0.0;
                textACC.setText("" + res);
            } else {
                textACC.setText("" + res);
            }
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


            updateECGChart(ecg_data);
            new ecgSendtoDBTask().execute(Arrays.toString(data));
        }
    }

    private void updateECGChart(byte[] ecg) {

        LinearLayout chartLyt = (LinearLayout) findViewById(R.id.chart);
        chartLyt.setVisibility(View.VISIBLE);

        if(!flag)
        {
            if (chartView != null)
                chartView.clearFocus();
        }
        int x;

        int y=count*500;

        if (count>3) {
            y = 2000;
            for(x=0;x<2000;x++)
                allbytes[x] = allbytes[x+500];
        }

        series.clear();
        for(x=0;x<500;x++)
        {
            allbytes[y]=ecg[x];
            y++;
        }
        count++;


        for (x = 0; x < 2500; x++) {
            series.add(x, allbytes[x] & 0xFF);
        }

        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setLineWidth(0.2f);
        renderer.setColor(Color.RED);
        renderer.setDisplayBoundingPoints(true);
        renderer.setPointStrokeWidth(0.5f);
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(series);

        XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
        mRenderer.addSeriesRenderer(renderer);

        mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins

        mRenderer.setPanEnabled(false, false);
        mRenderer.setYAxisMax(250);
        mRenderer.setYAxisMin(0);
        mRenderer.setShowGrid(true); // we show the grid

        chartView= ChartFactory.getLineChartView(getApplicationContext(), dataset, mRenderer);
        chartLyt.removeAllViews();
        chartLyt.addView(chartView, 0);

        flag=false;
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

}
