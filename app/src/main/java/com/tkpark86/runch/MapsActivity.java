package com.tkpark86.runch;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.tkpark86.runch.model.MapData;
import com.tkpark86.runch.model.RaceInfo;
import com.tkpark86.runch.model.TrackPoint;
import com.tkpark86.runch.model.WayPoint;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnChartValueSelectedListener {

    private GoogleMap mMap;
    private LineChart mChart;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setDrawGridBackground(false);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDescriptionColor(Color.WHITE);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);

        Legend legend = mChart.getLegend();
        legend.setTextColor(Color.WHITE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        GoodrunsApplication app = (GoodrunsApplication) getApplicationContext();
        MapData mapData = app.getMapData();

        drawRoute(mapData.raceInfoList);

        // set WatPoint
        drawWayPoint(mapData.wptList);
    }

    @Override
    public void onValueSelected(Entry entry, int dataSetIndex, Highlight h) {
        //Log.d(LOG_TAG, "Entry selected=" + e.toString());
        //Log.d(LOG_TAG, "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
        TrackPoint tp = (TrackPoint) entry.getData();
        LatLng position = new LatLng(tp.latitude, tp.longitude);

        if(mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Marker")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pegman)));
        } else {
            mMarker.setPosition(position);

            float zomm = mMap.getCameraPosition().zoom;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zomm));
        }
        mMarker.setSnippet("Distance:" + tp.distance);
        mMarker.showInfoWindow();
    }

    @Override
    public void onNothingSelected() {

    }

    public void drawLegend(final RaceInfo raceInfo) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.legend_layout);

        TextView tvLegend = new TextView(this);
        tvLegend.setText(raceInfo.legend);
        tvLegend.setTextColor(raceInfo.lineColor);

        float scale = getResources().getDisplayMetrics().density;
        int leftRightDp = (int) (14 * scale + 0.5f);
        int topBottomDp = (int) (4 * scale + 0.5f);

        tvLegend.setPadding(leftRightDp, topBottomDp, leftRightDp, topBottomDp);
        tvLegend.setBackgroundResource(R.drawable.button_background_dark);
        tvLegend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawChart(raceInfo);
            }
        });

        layout.addView(tvLegend);
    }

    private void drawChart(RaceInfo raceInfo) {
        List<String> xVals = new ArrayList<>();
        List<Entry> yVals = new ArrayList<>();
        for (int i = 0; i < raceInfo.list.size(); i++) {
            xVals.add(raceInfo.list.get(i).distance + "");

            Entry entry = new Entry(raceInfo.list.get(i).elevation, i);
            entry.setData(raceInfo.list.get(i));
            yVals.add(entry);
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "Elevation(m)");

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setHighLightColor(Color.WHITE);
        set1.setColor(Color.WHITE);
        set1.setLineWidth(1f);
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setValueTextSize(20f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.WHITE);
        set1.setDrawFilled(true);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart.clear();
        mChart.setDescription("Totla Distance : " + raceInfo.totalDistance);
        mChart.setData(data);
        mChart.invalidate(); // refresh
    }

    private void drawPolyline(LatLngBounds.Builder builder, RaceInfo raceInfo) {
        PolylineOptions options = new PolylineOptions();
        NumberFormat nf4Km = new DecimalFormat("#,##0.#km");
        NumberFormat nf4m = new DecimalFormat("#,##0m");
        LatLng tempPoint = null;
        double totlaDistance = 0.0d;
        for (TrackPoint tp : raceInfo.list) {
            LatLng mapPoint = new LatLng(tp.latitude, tp.longitude);
            builder.include(mapPoint);
            options.add(mapPoint);

            if(tempPoint != null) {
                totlaDistance += SphericalUtil.computeDistanceBetween(tempPoint, mapPoint);
                if(totlaDistance > 1000.0d) {
                    tp.distance = nf4Km.format(totlaDistance/1000);
                } else {
                    tp.distance = nf4m.format(totlaDistance);
                }
            }
            tempPoint = mapPoint;
        }
        raceInfo.polyline = mMap.addPolyline(options
                .color(raceInfo.lineColor)
                .width(raceInfo.lineWidth));

        raceInfo.totalDistance = nf4Km.format(totlaDistance/1000);
    }

    private void drawPolygon(LatLngBounds.Builder builder, RaceInfo raceInfo) {
        PolygonOptions options = new PolygonOptions();
        for (TrackPoint tp : raceInfo.list) {
            LatLng mapPoint = new LatLng(tp.latitude, tp.longitude);
            builder.include(mapPoint);
            options.add(mapPoint);
        }

        Polygon polygon = mMap.addPolygon(options
                .strokeWidth(raceInfo.lineWidth)
                .strokeColor(raceInfo.lineColor)
                .fillColor(raceInfo.lineColor));

        int prevColor = polygon.getFillColor();
        polygon.setStrokeColor(Color.argb(100, Color.red(prevColor), Color.green(prevColor), Color.blue(prevColor)));
        polygon.setFillColor(Color.argb(100, Color.red(prevColor), Color.green(prevColor), Color.blue(prevColor)));
    }

    private void drawWayPoint(List<WayPoint> wptList) {
        for(WayPoint wpt : wptList) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(wpt.latitude, wpt.longitude))
                    .title(wpt.name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                    .infoWindowAnchor(0.5f, 0.5f));
        }
    }

    private void drawRoute(List<RaceInfo> raceInfoList) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(RaceInfo raceInfo : raceInfoList) {
            if(raceInfo.type.equals("polyline")) {
                drawPolyline(builder, raceInfo);

                // Chart
                drawChart(raceInfo);

                // set Legend
                drawLegend(raceInfo);
            } if(raceInfo.type.equals("polygon")) {
                drawPolygon(builder, raceInfo);
            }
        }

        // zoom
        LatLngBounds bounds = builder.build();
        int padding = 50; // offset from edges of the map in pixels
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        //
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (180 * scale + 0.5f);
        mMap.setPadding(0, 0, 0, dpAsPixels);
        //

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //mMap.moveCamera(cu);
                mMap.animateCamera(cu);
            }
        });

    }
}
