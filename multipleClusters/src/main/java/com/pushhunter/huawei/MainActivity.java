package com.pushhunter.huawei;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.huawei.clustering.Cluster;
import com.huawei.clustering.ClusterManager;
import com.huawei.clustering.IconGenerator;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.SupportMapFragment;
import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
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

    private static final LatLngBounds NETHERLANDS = new LatLngBounds(
            new LatLng(50.77083, 3.57361), new LatLng(53.35917, 7.10833));

    private final SparseArray<BitmapDescriptor> mClusterIcons = new SparseArray<>();

    private static final int[] CLUSTER_ICON_BUCKETS = {10, 20, 50, 100, 500, 1000, 5000, 10000, 20000, 50000, 100000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(this);
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
                huaweiMap.animateCamera(CameraUpdateFactory.newLatLngBounds(NETHERLANDS, 0));
            }
        });

        //Cluster Manager 1 & 2
        final ClusterManager<MyItem> clusterManager = new ClusterManager<>(this, huaweiMap);
        final ClusterManager<MyItem> clusterManager2 = new ClusterManager<>(this, huaweiMap);

        clusterManagers(clusterManager);
        clusterManagers(clusterManager2);

        huaweiMap.setOnCameraIdleListener(new HuaweiMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                clusterManager.cluster();
                clusterManager2.cluster();
            }
        });

        //Cluster Items 1 by using addItems
        List<MyItem> clusterItems = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            clusterItems.add(new MyItem(
                    RandomLocationGenerator.generate(DEUTSCHLAND)));
        }
        clusterManager.addItems(clusterItems);

        //Cluster Items 2 by using addItem
        for (int i = 0; i < 100000; i++) {
            clusterManager2.addItem(new MyItem(
                    RandomLocationGenerator.generate(NETHERLANDS)));
        }

        clusterManager2.setIconGenerator(new IconGenerator<MyItem>() {
            @NonNull
            @Override
            public BitmapDescriptor getClusterIcon(@NonNull Cluster<MyItem> cluster) {
                int bucket = getClusterIconBucket(cluster);
                BitmapDescriptor descriptor = mClusterIcons.get(bucket);
                if (descriptor == null) {
                    CustomIconGenerator customIconGenerator = new CustomIconGenerator(getApplicationContext());
                    descriptor = customIconGenerator.createClusterIcon(bucket);
                    mClusterIcons.put(bucket, descriptor);
                }
                return descriptor;
            }

            @NonNull
            @Override
            public BitmapDescriptor getMarkerIcon(@NonNull MyItem clusterItem) {
                //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher);
                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                return icon;
            }
        });
    }

    private int getClusterIconBucket(@NonNull Cluster<MyItem> cluster) {
        int itemCount = cluster.getItems().size();
        if (itemCount <= CLUSTER_ICON_BUCKETS[0]) {
            return itemCount;
        }
        for (int i = 0; i < CLUSTER_ICON_BUCKETS.length - 1; i++) {
            if (itemCount < CLUSTER_ICON_BUCKETS[i + 1]) {
                return CLUSTER_ICON_BUCKETS[i];
            }
        }
        return CLUSTER_ICON_BUCKETS[CLUSTER_ICON_BUCKETS.length - 1];
    }

    private void clusterManagers(ClusterManager<MyItem> clusterManager) {
        clusterManager.setCallbacks(new ClusterManager.Callbacks<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                Log.d(TAG, "onClusterClick");
                return false;
            }

            @Override
            public boolean onClusterItemClick(MyItem clusterItem) {
                Log.d(TAG, "onClusterItemClick");
                return false;
            }
        });
    }

    //Done
    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
    }
}