package com.pushhunter.huawei;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.clustering.ClusterItem;

class SampleClusterItem implements ClusterItem {

    private final LatLng location;

    SampleClusterItem(LatLng location) {
        this.location = location;
    }

    @Override
    public double getLatitude() {
        return location.latitude;
    }

    @Override
    public double getLongitude() {
        return location.longitude;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
