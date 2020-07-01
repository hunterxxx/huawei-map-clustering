package com.pushhunter.huawei;

import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;

import java.util.Random;

final class RandomLocationGenerator {

    private static final Random RANDOM = new Random();


    static LatLng generate(LatLngBounds bounds) {
        double minLatitude = bounds.southwest.latitude;
        double maxLatitude = bounds.northeast.latitude;
        double minLongitude = bounds.southwest.longitude;
        double maxLongitude = bounds.northeast.longitude;
        return new LatLng(
                minLatitude + (maxLatitude - minLatitude) * RANDOM.nextDouble(),
                minLongitude + (maxLongitude - minLongitude) * RANDOM.nextDouble());
    }

    private RandomLocationGenerator() {
    }
}
