package com.huawei.clustering;

import androidx.annotation.NonNull;

import com.huawei.hms.maps.model.Marker;

/**
 * Allows for modifying the marker that is being rendered.
 *
 * @param <T> type of the cluster item
 */
public interface RenderPostProcessor<T extends ClusterItem> {
    /**
     * Allows for modifying the marker that is being rendered.
     *
     * @param marker the marker
     * @param cluster the cluster
     */
    void postProcess(@NonNull Marker marker, @NonNull Cluster<T> cluster);
}