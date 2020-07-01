package com.pushhunter.huawei;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.huawei.clustering.Cluster;
import com.huawei.clustering.ClusterManager;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * map activity entrance class
 */

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final LatLngBounds DEUTSCHLAND = new LatLngBounds(
            new LatLng(47.77083, 6.57361), new LatLng(53.35917, 12.10833));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState == null) {
            setupMapFragment();
        }
    }

    @Override
    public void onMapReady(final HuaweiMap huaweiMap) {
        huaweiMap.setOnMapLoadedCallback(new HuaweiMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                huaweiMap.animateCamera(CameraUpdateFactory.newLatLngBounds(DEUTSCHLAND, 0));
            }
        });

        ClusterManager<SampleClusterItem> clusterManager = new ClusterManager<>(this, huaweiMap);
        clusterManager.setCallbacks(new ClusterManager.Callbacks<SampleClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<SampleClusterItem> cluster) {
                Log.d(TAG, "onClusterClick");
                return false;
            }

            @Override
            public boolean onClusterItemClick(SampleClusterItem clusterItem) {
                Log.d(TAG, "onClusterItemClick");
                return false;
            }
        });
        huaweiMap.setOnCameraIdleListener(clusterManager);

        List<SampleClusterItem> clusterItems = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            clusterItems.add(new SampleClusterItem(
                    RandomLocationGenerator.generate(DEUTSCHLAND)));
        }
        clusterManager.setItems(clusterItems);
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
    }
}