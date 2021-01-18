package com.huawei.clustering;


import com.huawei.hms.maps.model.Marker;

import androidx.annotation.NonNull;

/**
 * A default render post processor that does nothing.
 *
 * @param <T> type of the the cluster item
 */
public class DefaultRenderPostProcessor<T extends ClusterItem> implements RenderPostProcessor<T> {
    @Override
    public void postProcess(@NonNull Marker marker, @NonNull Cluster<T> cluster) {
        // do nothing by default
    }
}