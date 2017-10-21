package pt.ua.ieeta.healthintegration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

public class Info_Session extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link customViewPager} that will host the section contents.
     */

    private customViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info__session);


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        mViewPager = (customViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(false);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        setupActionBar();

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        Globals g = Globals.getInstance();
        private int SessionID = g.getCurrentSessionSelected();
        private HashMap< Integer, HashMap< String, HashMap< Integer, JSONObject>>> allData = g.getAllDataBySession();
        private HashMap< String, HashMap< Integer, JSONObject>> dataSessionType = allData.get(SessionID);

        /*
        *   Heart Rate Stuff
        * */
        private TextView textCountHR;
        private TextView textSumHR;
        private TextView textMinHR;
        private TextView textMaxHR;
        private TextView textAvgHR;
        private TextView textVarHR;
        private LinearLayout chartLytHR;
        private LinearLayout layoutHR;
        private GraphicalView chartViewHR;
        private XYSeries seriesHR = new XYSeries("Heart Rate");
        private String [] xValuesHR = null;
        private int [] yValuesHR = null;
        private int maxHR = 0;
        private int minHR = 255;
        private int avgHR;
        private double varHR = 0.0;


        /*
        *   ECG Stuff
        * */

        private TextView textCountECG;
        private TextView textSumECG;
        private TextView textMinECG;
        private TextView textMaxECG;
        private TextView textAvgECG;
        private TextView textVarECG;
        private LinearLayout chartLytECG;
        private LinearLayout layoutECG;
        private GraphicalView chartViewECG;
        private XYSeries seriesECG = new XYSeries("ECG");
        private String [] xValuesECG = null;
        private int [] yValuesECG = null;
        private int maxECG = 0;
        private int minECG = 255;
        private int avgECG;
        private double varECG = 0.0;

        /*
        * ACC Stuff
        * */

        private LinearLayout chartLytACC;
        private LinearLayout layoutACC;
        private GraphicalView chartViewACC;
        private XYSeries seriesACCx = new XYSeries("X-Values");
        private XYSeries seriesACCy = new XYSeries("Y-Values");
        private XYSeries seriesACCz = new XYSeries("Z-Values");
        private XYSeries seriesACC_acceleration = new XYSeries("Acceleration");

        private String [] xValuesACC = null;
        private int [] yValuesACC_X = null;
        private int [] yValuesACC_Y = null;
        private int [] yValuesACC_Z = null;
        private double [] yValuesACC_acceleration = null;
        private double maxACC = -1000;
        private double minACC = 1000;
        private double avgACC = 0;
        private double varACC = 0.0;
        private TextView textMinACC;
        private TextView textMaxACC;
        private TextView textAvgACC;
        private TextView textVarACC;




        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_info__session, container, false);

            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                textCountHR = (TextView) rootView.findViewById(R.id.value_countHR);
                textMinHR = (TextView) rootView.findViewById(R.id.value_minHR);
                textMaxHR = (TextView) rootView.findViewById(R.id.value_maxHR);
                textAvgHR = (TextView) rootView.findViewById(R.id.value_avgHR);
                textVarHR = (TextView) rootView.findViewById(R.id.value_varHR);
                textSumHR = (TextView) rootView.findViewById(R.id.value_sumHR);
                fillChartHeartRate(rootView);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                textCountECG = (TextView) rootView.findViewById(R.id.value_countECG);
                textMinECG = (TextView) rootView.findViewById(R.id.value_minECG);
                textMaxECG = (TextView) rootView.findViewById(R.id.value_maxECG);
                textAvgECG = (TextView) rootView.findViewById(R.id.value_avgECG);
                textVarECG = (TextView) rootView.findViewById(R.id.value_varECG);
                textSumECG = (TextView) rootView.findViewById(R.id.value_sumECG);
                fillChartECG(rootView);
            } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                textMinACC = (TextView) rootView.findViewById(R.id.value_minACC);
                textMaxACC = (TextView) rootView.findViewById(R.id.value_maxACC);
                textAvgACC = (TextView) rootView.findViewById(R.id.value_avgACC);
                textVarACC = (TextView) rootView.findViewById(R.id.value_varACC);

                fillChartACC(rootView);
            }
            return rootView;
        }

        public void fillChartHeartRate(View v) {

            if (dataSessionType.containsKey("heart-rate")) {
                yValuesHR = new int [dataSessionType.get("heart-rate").size()];
                xValuesHR = new String [dataSessionType.get("heart-rate").size()];

                checkOutPointsHR(dataSessionType.get("heart-rate"));

                System.out.println("Points to Fill" + Arrays.toString(yValuesHR));
                chartLytHR = (LinearLayout) v.findViewById(R.id.chartHR);
                layoutHR = (LinearLayout) v.findViewById(R.id.linearlayoutHR);
                layoutHR.setVisibility(View.VISIBLE);
                seriesHR.clear();
                // fill series with data

                for (int x = 0; x < yValuesHR.length; x++) {
                    seriesHR.add(x, yValuesHR[x]);
                }

                XYSeriesRenderer renderer = new XYSeriesRenderer();
                renderer.setLineWidth(2f);
                renderer.setColor(Color.RED);
                renderer.setDisplayBoundingPoints(true);
                renderer.setPointStyle(PointStyle.CIRCLE);
                renderer.setPointStrokeWidth(6f);
                XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                dataset.addSeries(seriesHR);

                XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
                mRenderer.setZoomButtonsVisible(true);
                mRenderer.addSeriesRenderer(renderer);
                mRenderer.setXLabels(0);
                mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
                mRenderer.setInScroll(true);
                mRenderer.setPanEnabled(false, false);
                mRenderer.setYAxisMax(230);
                mRenderer.setYAxisMin(30);
                mRenderer.setPanEnabled(true, true);
                mRenderer.setZoomEnabled(true, false);
                mRenderer.setShowGrid(true); // we show the grid
                mRenderer.setLabelsTextSize(25);
                mRenderer.setAxisTitleTextSize(40);


                if(xValuesHR.length >= 5){
                    Double D = xValuesHR.length/1.3;
                    int _4th = Integer.valueOf(D.intValue());
                    mRenderer.addXTextLabel(0, xValuesHR[0]);
                    mRenderer.addXTextLabel(xValuesHR.length/4, xValuesHR[xValuesHR.length/4]);
                    mRenderer.addXTextLabel(xValuesHR.length/2, xValuesHR[xValuesHR.length/2]);
                    mRenderer.addXTextLabel(_4th, xValuesHR[_4th]);
                    mRenderer.addXTextLabel(xValuesHR.length-1, xValuesHR[xValuesHR.length-1]);
                } else {
                    for (int i = 0; i < xValuesHR.length; i++) {
                        mRenderer.addXTextLabel(i, xValuesHR[i]);
                        mRenderer.setXLabelsPadding(10);
                    }
                }
                chartViewHR= ChartFactory.getLineChartView(v.getContext(), dataset, mRenderer);
                chartLytHR.removeAllViews();
                chartLytHR.addView(chartViewHR, 0);
            }
        }
        public int [] checkOutPointsHR(HashMap< Integer, JSONObject> dataSessionNumber){

            try {
                int[] allPointKeys = new int [dataSessionNumber.size()];
                int x = 0;
                for (int pointKey : dataSessionNumber.keySet()) {
                    allPointKeys[x] = pointKey;
                    x++;
                }
                Arrays.sort(allPointKeys);

                int total = 0;
                for( int i = 0; i < dataSessionNumber.size(); i++ ) {
                    JSONObject dataPoint = dataSessionNumber.get(allPointKeys[i]);
                    int val = dataPoint.getJSONObject("heart_rate").getInt("value");
                    yValuesHR[i] = val;
                    total = total + val;
                    xValuesHR[i] = dataPoint.getJSONObject("effective_time_frame").getString("date_time").split("T")[1].split("Z")[0];
                    if (val > maxHR) { maxHR = val; }
                    if (val < minHR) { minHR = val; }
                }
                avgHR = total / dataSessionNumber.size();

                double sumDiffsSquared = 0.0;

                for (int i = 0; i < dataSessionNumber.size(); i++ )
                {
                    JSONObject dataPoint = dataSessionNumber.get(allPointKeys[i]);
                    int val = dataPoint.getJSONObject("heart_rate").getInt("value");
                    double diff = val - avgHR;
                    diff *= diff;
                    sumDiffsSquared += diff;
                }
                varHR = sumDiffsSquared  / (dataSessionNumber.size()-1);

                textCountHR.setText("" + dataSessionType.get("heart-rate").size());
                textSumHR.setText("" + total);
                textMinHR.setText("" + minHR);
                textMaxHR.setText("" + maxHR);
                textAvgHR.setText("" + avgHR);
                textVarHR.setText("" + new BigDecimal(varHR).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void fillChartECG(View v) {

            if (dataSessionType.containsKey("ecg")) {
                int size = dataSessionType.get("ecg").size() * 500;
                yValuesECG = new int [size];
                xValuesECG = new String [size];

                checkOutPointsECG(dataSessionType.get("ecg"));

                System.out.println("Points to Fill" + Arrays.toString(yValuesECG));
                chartLytECG = (LinearLayout) v.findViewById(R.id.chartECG);
                layoutECG = (LinearLayout) v.findViewById(R.id.linearlayoutECG);
                layoutECG.setVisibility(View.VISIBLE);
                seriesECG.clear();
                // fill series with data

                for (int x = 0; x < yValuesECG.length; x++) {
                    seriesECG.add(x, yValuesECG[x]);
                }

                XYSeriesRenderer renderer = new XYSeriesRenderer();
                renderer.setLineWidth(0.5f);
                renderer.setColor(Color.RED);
                renderer.setDisplayBoundingPoints(true);

                renderer.setPointStrokeWidth(0.2f);
                XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                dataset.addSeries(seriesECG);

                XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
                mRenderer.setZoomButtonsVisible(true);
                mRenderer.addSeriesRenderer(renderer);
                mRenderer.setXLabels(0);
                mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
                mRenderer.setInScroll(true);
                mRenderer.setPanEnabled(false, false);
                mRenderer.setYAxisMax(230);
                mRenderer.setYAxisMin(30);
                mRenderer.setPanEnabled(true, true);
                mRenderer.setZoomEnabled(true, false);
                mRenderer.setShowGrid(true); // we show the grid
                mRenderer.setLabelsTextSize(25);
                mRenderer.setAxisTitleTextSize(40);



                if(xValuesECG.length >= 5){
                    Double D = xValuesECG.length/1.3;
                    int _4th = Integer.valueOf(D.intValue());
                    mRenderer.addXTextLabel(0, xValuesECG[0]);
                    mRenderer.addXTextLabel(xValuesECG.length/4, xValuesECG[xValuesECG.length/4]);
                    mRenderer.addXTextLabel(xValuesECG.length/2, xValuesECG[xValuesECG.length/2]);
                    mRenderer.addXTextLabel(_4th, xValuesECG[_4th]);
                    mRenderer.addXTextLabel(xValuesECG.length-1, xValuesECG[xValuesECG.length-1]);
                } else {
                    for (int i = 0; i < xValuesECG.length; i++) {
                        mRenderer.addXTextLabel(i, xValuesECG[i]);
                        mRenderer.setXLabelsPadding(10);
                    }
                }
                chartViewECG= ChartFactory.getLineChartView(v.getContext(), dataset, mRenderer);
                chartLytECG.removeAllViews();
                chartLytECG.addView(chartViewECG, 0);
            }
        }
        public int [] checkOutPointsECG(HashMap< Integer, JSONObject> dataSessionNumber){

            try {
                int size = dataSessionNumber.size() * 500;
                int[] allPointKeys = new int [dataSessionNumber.size()];
                int x = 0;
                for (int pointKey : dataSessionNumber.keySet()) {
                    allPointKeys[x] = pointKey;
                    x++;
                }
                Arrays.sort(allPointKeys);

                int total = 0;
                for( int i = 0; i < dataSessionNumber.size(); i++ ) {
                    JSONObject dataPoint = dataSessionNumber.get(allPointKeys[i]);
                    JSONArray currentValues = dataPoint.getJSONObject("ecg").getJSONArray("values");
                    for (int j = 0; j< currentValues.length(); j++ ) {
                        int val = currentValues.getInt(j);
                        int current_index = i*500 + j;
                        yValuesECG[current_index] = val;
                        total = total + val;
                        xValuesECG[current_index] = dataPoint.getJSONObject("effective_time_frame").getString("date_time").split("T")[1].split("Z")[0];
                        if (val > maxECG) { maxECG = val; }
                        if (val < minECG) { minECG = val; }
                    }

                }
                avgECG = total / size;

                double sumDiffsSquared = 0.0;

                for (int i = 0; i < dataSessionNumber.size(); i++ )
                {

                    JSONObject dataPoint = dataSessionNumber.get(allPointKeys[i]);
                    JSONArray currentValues = dataPoint.getJSONObject("ecg").getJSONArray("values");
                    for (int j = 0; j< currentValues.length(); j++ ) {
                        int val = currentValues.getInt(j);
                        double diff = val - avgECG;
                        diff *= diff;
                        sumDiffsSquared += diff;
                    }
                }
                varECG = sumDiffsSquared  / (size-1);

                textCountECG.setText("" + size);
                textSumECG.setText("" + total);
                textMinECG.setText("" + minECG);
                textMaxECG.setText("" + maxECG);
                textAvgECG.setText("" + avgECG);
                textVarECG.setText("" + new BigDecimal(varECG).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void fillChartACC(View v) {

            if (dataSessionType.containsKey("accelerometer")) {
                int size = dataSessionType.get("accelerometer").size();
                yValuesACC_X = new int [size];
                yValuesACC_Y = new int [size];
                yValuesACC_Z = new int [size];
                yValuesACC_acceleration = new double [size];

                xValuesACC = new String [size];

                checkOutPointsACC(dataSessionType.get("accelerometer"));

                System.out.println("Points to Fill ACC X" + Arrays.toString(yValuesACC_X));
                System.out.println("Points to Fill ACC Y" + Arrays.toString(yValuesACC_Y));
                System.out.println("Points to Fill ACC Z" + Arrays.toString(yValuesACC_Z));

                chartLytACC = (LinearLayout) v.findViewById(R.id.chartACC);
                layoutACC = (LinearLayout) v.findViewById(R.id.linearlayoutACC);
                layoutACC.setVisibility(View.VISIBLE);
                seriesACCx.clear();
                seriesACCy.clear();
                seriesACCz.clear();
                seriesACC_acceleration.clear();

                // fill series with data

                for (int x = 0; x < yValuesACC_X.length; x++) {
                    seriesACCx.add(x, yValuesACC_X[x]);
                    seriesACCy.add(x, yValuesACC_Y[x]);
                    seriesACCz.add(x, yValuesACC_Z[x]);
                    seriesACC_acceleration.add(x, yValuesACC_acceleration[x]);

                }

                XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                dataset.addSeries(seriesACCx);
                dataset.addSeries(seriesACCy);
                dataset.addSeries(seriesACCz);
                dataset.addSeries(seriesACC_acceleration);


                XYSeriesRenderer rendererX = new XYSeriesRenderer();
                rendererX.setLineWidth(1f);
                rendererX.setColor(Color.RED);
                rendererX.setDisplayBoundingPoints(true);
                rendererX.setPointStyle(PointStyle.CIRCLE);
                rendererX.setPointStrokeWidth(4f);

                XYSeriesRenderer rendererY = new XYSeriesRenderer();
                rendererY.setLineWidth(1f);
                rendererY.setColor(Color.BLUE);
                rendererY.setDisplayBoundingPoints(true);
                rendererY.setPointStyle(PointStyle.CIRCLE);
                rendererY.setPointStrokeWidth(4f);

                XYSeriesRenderer rendererZ = new XYSeriesRenderer();
                rendererZ.setLineWidth(1f);
                rendererZ.setColor(Color.GREEN);
                rendererZ.setDisplayBoundingPoints(true);
                rendererZ.setPointStyle(PointStyle.CIRCLE);
                rendererZ.setPointStrokeWidth(4f);

                XYSeriesRenderer rendererAcceleration= new XYSeriesRenderer();
                rendererAcceleration.setLineWidth(2f);
                rendererAcceleration.setColor(Color.BLACK);
                rendererAcceleration.setDisplayBoundingPoints(true);
                rendererAcceleration.setPointStyle(PointStyle.CIRCLE);
                rendererAcceleration.setPointStrokeWidth(6f);

                XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
                mRenderer.setZoomButtonsVisible(true);
                mRenderer.addSeriesRenderer(rendererX);
                mRenderer.addSeriesRenderer(rendererY);
                mRenderer.addSeriesRenderer(rendererZ);
                mRenderer.addSeriesRenderer(rendererAcceleration);

                mRenderer.setXLabels(0);
                mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00)); // transparent margins
                mRenderer.setInScroll(true);
                mRenderer.setPanEnabled(false, false);
                mRenderer.setYAxisMax(25);
                mRenderer.setYAxisMin(-25);
                mRenderer.setPanEnabled(true, true);
                mRenderer.setZoomEnabled(true, true);
                mRenderer.setShowGrid(true); // we show the grid
                mRenderer.setLabelsTextSize(25);
                mRenderer.setAxisTitleTextSize(40);


                if(xValuesACC.length >= 5){
                    Double D = xValuesACC.length/1.3;
                    int _4th = Integer.valueOf(D.intValue());
                    mRenderer.addXTextLabel(0, xValuesACC[0]);
                    mRenderer.addXTextLabel(xValuesACC.length/4, xValuesACC[xValuesACC.length/4]);
                    mRenderer.addXTextLabel(xValuesACC.length/2, xValuesACC[xValuesACC.length/2]);
                    mRenderer.addXTextLabel(_4th, xValuesACC[_4th]);
                    mRenderer.addXTextLabel(xValuesACC.length-1, xValuesACC[xValuesACC.length-1]);
                } else {
                    for (int i = 0; i < xValuesACC.length; i++) {
                        mRenderer.addXTextLabel(i, xValuesACC[i]);
                        mRenderer.setXLabelsPadding(10);
                    }
                }
                chartViewACC= ChartFactory.getLineChartView(v.getContext(), dataset, mRenderer);
                chartLytACC.removeAllViews();
                chartLytACC.addView(chartViewACC, 0);
            }
        }
        public int [] checkOutPointsACC(HashMap< Integer, JSONObject> dataSessionNumber){

            try {
                int[] allPointKeys = new int [dataSessionNumber.size()];
                int x = 0;
                for (int pointKey : dataSessionNumber.keySet()) {
                    allPointKeys[x] = pointKey;
                    x++;
                }
                Arrays.sort(allPointKeys);

                double total = 0;
                for( int i = 0; i < dataSessionNumber.size(); i++ ) {
                    JSONObject dataPoint = dataSessionNumber.get(allPointKeys[i]);
                    JSONObject values = dataPoint.getJSONObject("accelerometer").getJSONObject("values");
                    int valX = values.getInt("x");
                    int valY = values.getInt("y");
                    int valZ = values.getInt("z");

                    yValuesACC_X[i] = valX;
                    yValuesACC_Y[i] = valY;
                    yValuesACC_Z[i] = valZ;
                    Float accelationSquareRoot = (valX * valX + valY * valY + valZ * valZ) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
                    double acceleration = Math.sqrt(accelationSquareRoot);
                    yValuesACC_acceleration[i] =acceleration-3.456522103440785;
                    if (yValuesACC_acceleration[i] < 0.0) { yValuesACC_acceleration[i] = 0.0;}

                    double val = yValuesACC_acceleration[i];

                    xValuesACC[i] = dataPoint.getJSONObject("effective_time_frame").getString("date_time").split("T")[1].split("Z")[0];

                    total = total + yValuesACC_acceleration[i];

                    if (val > maxACC) { maxACC = val; }
                    if (val < minACC) { minACC = val; }
                }

                avgACC = total / dataSessionNumber.size();

                double sumDiffsSquared = 0.0;

                for (int i = 0; i < yValuesACC_acceleration.length; i++ )
                {
                    double val = yValuesACC_acceleration[i];
                    double diff = val - avgACC;
                    diff *= diff;
                    sumDiffsSquared += diff;
                }
                varACC = sumDiffsSquared  / (yValuesACC_acceleration.length-1);

                textMinACC.setText("" + new BigDecimal(minACC).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                textMaxACC.setText("" + new BigDecimal(maxACC).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                textAvgACC.setText("" + new BigDecimal(avgACC).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                textVarACC.setText("" + new BigDecimal(varACC).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Heart Rate";
                case 1:
                    return "ECG";
                case 2:
                    return "Accelerometer";
            }
            return null;
        }
    }
}
