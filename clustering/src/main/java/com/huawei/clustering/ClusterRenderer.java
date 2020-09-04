package com.huawei.clustering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ClusterRenderer<T extends ClusterItem> implements HuaweiMap.OnMarkerClickListener {

    private static final int BACKGROUND_MARKER_Z_INDEX = 0;

    private static final int FOREGROUND_MARKER_Z_INDEX = 1;

    private final HuaweiMap mHuaweiMap;

    private final List<Cluster<T>> mClusters = new ArrayList<>();

    private final Map<Cluster<T>, MarkerState> mMarkers = new HashMap<>();

    private IconGenerator<T> mIconGenerator;

    private RenderPostProcessor<T> mRenderPostProcessor;

    private ClusterManager.Callbacks<T> mCallbacks;

    ClusterRenderer(@NonNull Context context, @NonNull HuaweiMap huaweiMap) {
        mHuaweiMap = huaweiMap;
        mHuaweiMap.setOnMarkerClickListener(this);
        mIconGenerator = new DefaultIconGenerator<>(context);
        mRenderPostProcessor = new DefaultRenderPostProcessor<>();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object markerTag = marker.getTag();
        if (markerTag instanceof Cluster) {
            //noinspection unchecked
            Cluster<T> cluster = (Cluster<T>) marker.getTag();
            //noinspection ConstantConditions
            List<T> clusterItems = cluster.getItems();

            if (mCallbacks != null) {
                if (clusterItems.size() > 1) {
                    return mCallbacks.onClusterClick(cluster);
                } else {
                    return mCallbacks.onClusterItemClick(clusterItems.get(0));
                }
            }
        }

        return false;
    }

    void setCallbacks(@Nullable ClusterManager.Callbacks<T> listener) {
        mCallbacks = listener;
    }

    void setIconGenerator(@NonNull IconGenerator<T> iconGenerator) {
        mIconGenerator = iconGenerator;
    }

    void setRenderPostProcessor(@NonNull RenderPostProcessor<T> renderPostProcessor) {
        mRenderPostProcessor = renderPostProcessor;
    }

    void render(@NonNull List<Cluster<T>> clusters) {
        List<Cluster<T>> clustersToAdd = new ArrayList<>();
        List<Cluster<T>> clustersToRemove = new ArrayList<>();

        for (Cluster<T> cluster : clusters) {
            if (!mMarkers.containsKey(cluster)) {
                clustersToAdd.add(cluster);
            }
        }

        for (Cluster<T> cluster : mMarkers.keySet()) {
            if (!clusters.contains(cluster)) {
                clustersToRemove.add(cluster);
            }
        }

        mClusters.addAll(clustersToAdd);
        mClusters.removeAll(clustersToRemove);

        // Remove the old clusters.
        for (Cluster<T> clusterToRemove : clustersToRemove) {
            Marker markerToRemove = mMarkers.get(clusterToRemove).getMarker();
            markerToRemove.setZIndex(BACKGROUND_MARKER_Z_INDEX);

            Cluster<T> parentCluster = findParentCluster(mClusters, clusterToRemove.getLatitude(),
                    clusterToRemove.getLongitude());
            if (parentCluster != null) {
                animateMarkerToLocation(markerToRemove, new LatLng(parentCluster.getLatitude(),
                        parentCluster.getLongitude()), true);
            } else {
                markerToRemove.remove();
            }

            mMarkers.remove(clusterToRemove);
        }

        // Add the new clusters.
        for (Cluster<T> clusterToAdd : clustersToAdd) {
            Marker markerToAdd;

            BitmapDescriptor markerIcon = getMarkerIcon(clusterToAdd);
            String markerTitle = getMarkerTitle(clusterToAdd);
            String markerSnippet = getMarkerSnippet(clusterToAdd);

            Cluster parentCluster = findParentCluster(clustersToRemove, clusterToAdd.getLatitude(),
                    clusterToAdd.getLongitude());
            if (parentCluster != null) {
                markerToAdd = mHuaweiMap.addMarker(new MarkerOptions()
                        .position(new LatLng(parentCluster.getLatitude(), parentCluster.getLongitude()))
                        .icon(markerIcon)
                        .title(markerTitle)
                        .snippet(markerSnippet)
                        .zIndex(FOREGROUND_MARKER_Z_INDEX));
                animateMarkerToLocation(markerToAdd,
                        new LatLng(clusterToAdd.getLatitude(), clusterToAdd.getLongitude()), false);
            } else {
                markerToAdd = mHuaweiMap.addMarker(new MarkerOptions()
                        .position(new LatLng(clusterToAdd.getLatitude(), clusterToAdd.getLongitude()))
                        .icon(markerIcon)
                        .title(markerTitle)
                        .snippet(markerSnippet)
                        .alpha(0.0F)
                        .zIndex(FOREGROUND_MARKER_Z_INDEX));
                animateMarkerAppearance(markerToAdd);
            }
            markerToAdd.setTag(clusterToAdd);

            mMarkers.put(clusterToAdd, new MarkerState(markerToAdd));
        }
    }

    @NonNull
    private BitmapDescriptor getMarkerIcon(@NonNull Cluster<T> cluster) {
        BitmapDescriptor clusterIcon;

        List<T> clusterItems = cluster.getItems();
        if (clusterItems.size() > 1) {
            clusterIcon = mIconGenerator.getClusterIcon(cluster);
        } else {
            clusterIcon = mIconGenerator.getMarkerIcon(clusterItems.get(0));
        }

        return Preconditions.checkNotNull(clusterIcon);
    }

    @Nullable
    private String getMarkerTitle(@NonNull Cluster<T> cluster) {
        List<T> clusterItems = cluster.getItems();
        if (clusterItems.size() > 1) {
            return null;
        } else {
            return clusterItems.get(0).getTitle();
        }
    }

    @Nullable
    private String getMarkerSnippet(@NonNull Cluster<T> cluster) {
        List<T> clusterItems = cluster.getItems();
        if (clusterItems.size() > 1) {
            return null;
        } else {
            return clusterItems.get(0).getSnippet();
        }
    }

    @Nullable
    private Cluster<T> findParentCluster(@NonNull List<Cluster<T>> clusters,
                                         double latitude, double longitude) {
        for (Cluster<T> cluster : clusters) {
            if (cluster.contains(latitude, longitude)) {
                return cluster;
            }
        }

        return null;
    }

    private void animateMarkerToLocation(@NonNull final Marker marker, @NonNull LatLng targetLocation,
                                         final boolean removeAfter) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofObject(marker, "position",
                new LatLngTypeEvaluator(), targetLocation);
        objectAnimator.setInterpolator(new FastOutSlowInInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (removeAfter) {
                    marker.remove();
                }
            }
        });
        objectAnimator.start();
    }

    private void animateMarkerAppearance(@NonNull Marker marker) {
        ObjectAnimator.ofFloat(marker, "alpha", 1.0F).start();
    }

    private static class LatLngTypeEvaluator implements TypeEvaluator<LatLng> {

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            double latitude = (endValue.latitude - startValue.latitude) * fraction + startValue.latitude;
            double longitude = (endValue.longitude - startValue.longitude) * fraction + startValue.longitude;
            return new LatLng(latitude, longitude);
        }
    }
}
