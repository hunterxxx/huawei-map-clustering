package com.huawei.clustering;

import androidx.annotation.NonNull;

import com.huawei.hms.maps.model.BitmapDescriptor;

/**
 * Generates icons for clusters and cluster items. Note that its implementations
 * should cache generated icons for subsequent use.
 */
public interface IconGenerator<T extends ClusterItem> {
    /**c
     * Returns an icon for the given cluster.
     *
     * @param cluster the cluster to return an icon for
     * @return the icon for the given cluster
     */
    @NonNull
    BitmapDescriptor getClusterIcon(@NonNull Cluster<T> cluster);

    /**
     * Returns an icon for the given cluster item.
     *
     * @param clusterItem the cluster item to return an icon for
     * @return the icon for the given cluster item
     */
    @NonNull
    BitmapDescriptor getMarkerIcon(@NonNull T clusterItem);
}
