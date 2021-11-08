package com.tkpark86.runch.model;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RaceInfo {
    public String type;
    public String legend;
    public int lineColor;
    public float lineWidth;
    public String totalDistance;
    public List<TrackPoint> list;
    public Polyline polyline;

    public RaceInfo() {
        type = "";
        legend = "";
        lineColor = 0;
        lineWidth = 0.0f;
        totalDistance = "0km";
        list = new ArrayList<>();
    }
}
