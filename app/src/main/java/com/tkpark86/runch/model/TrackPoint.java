package com.tkpark86.runch.model;

public class TrackPoint {
    public double latitude;
    public double longitude;
    public float elevation;
    public String distance;

    public TrackPoint() {
        latitude = 0.0d;
        longitude = 0.0d;
        elevation = 0.0f;
        distance = "0km";
    }
}
