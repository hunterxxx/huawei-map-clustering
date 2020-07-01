package com.huawei.clustering;

import androidx.annotation.NonNull;

import com.huawei.hms.maps.model.Marker;

public interface RenderPostProcessor<T extends ClusterItem> {

    boolean postProcess(@NonNull Marker marker, @NonNull Cluster<T> cluster);
}