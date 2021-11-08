package com.tkpark86.runch;

import android.app.Application;

import com.tkpark86.runch.model.MapData;

/**
 * Created by tkpark on 2016-01-01.
 */
public class GoodrunsApplication extends Application {
    public MapData getMapData() {
        return mapData;
    }

    public void setMapData(MapData mapData) {
        this.mapData = mapData;
    }

    private MapData mapData;



}
