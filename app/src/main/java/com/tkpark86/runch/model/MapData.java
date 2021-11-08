package com.tkpark86.runch.model;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    public List<RaceInfo> raceInfoList;
    public List<WayPoint> wptList;

    public MapData() {
        raceInfoList = new ArrayList<>();
        wptList = new ArrayList<>();
    }
}
