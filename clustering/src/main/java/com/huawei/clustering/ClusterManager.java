package com.huawei.clustering;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.huawei.clustering.Preconditions.checkNotNull;

/**
 * Groups multiple items on a map into clusters based on the current zoom level.
 * Clustering occurs when the map becomes idle, so an instance of this class
 *
 * @param <T> the type of an item to be clustered
 */
public class ClusterManager<T extends ClusterItem> implements HuaweiMap.OnCameraIdleListener {

    private static final int QUAD_TREE_BUCKET_CAPACITY = 4;
    private static final int DEFAULT_MIN_CLUSTER_SIZE = 1;

    private final HuaweiMap mHuaweiMap;

    private final QuadTree<T> mQuadTree;

    private final ClusterRenderer<T> mRenderer;

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private final ReentrantReadWriteLock quadTreeLock = new ReentrantReadWriteLock();

    private AsyncTask mQuadTreeTask;

    private AsyncTask mClusterTask;

    private int mMinClusterSize = DEFAULT_MIN_CLUSTER_SIZE;

    /**
     * Defines signatures for methods that are called when a cluster or a cluster item is clicked.
     *
     * @param <T> the type of an item managed by {@link ClusterManager}.
     */
    public interface Callbacks<T extends ClusterItem> {
        /**
         * Called when a marker representing a cluster has been clicked.
         *
         * @param cluster the cluster that has been clicked
         * @return <code>true</code> if the listener has consumed the event (i.e., the default behavior should not occur);
         * <code>false</code> otherwise (i.e., the default behavior should occur). The default behavior is for the camera
         * to move to the marker and an info window to appear.
         */
        boolean onClusterClick(@NonNull Cluster<T> cluster);

        /**
         * Called when a marker representing a cluster item has been clicked.
         *
         * @param clusterItem the cluster item that has been clicked
         * @return <code>true</code> if the listener has consumed the event (i.e., the default behavior should not occur);
         * <code>false</code> otherwise (i.e., the default behavior should occur). The default behavior is for the camera
         * to move to the marker and an info window to appear.
         */
        boolean onClusterItemClick(@NonNull T clusterItem);
    }

    /**
     * Creates a new cluster manager using the default icon generator.
     * To customize marker icons, set a custom icon generator using
     * {@link ClusterManager#setIconGenerator(IconGenerator)}.
     *
     * @param huawei the map instance where markers will be rendered
     */
    public ClusterManager(@NonNull Context context, @NonNull HuaweiMap huawei) {
        checkNotNull(context);
        mHuaweiMap = checkNotNull(huawei);
        mRenderer = new ClusterRenderer<>(context, huawei);
        mQuadTree = new QuadTree<>(QUAD_TREE_BUCKET_CAPACITY);
    }

    /**
     * Sets a custom icon generator thus replacing the default one.
     *
     * @param iconGenerator the custom icon generator that's used for generating marker icons
     */
    public void setIconGenerator(@NonNull IconGenerator<T> iconGenerator) {
        checkNotNull(iconGenerator);
        mRenderer.setIconGenerator(iconGenerator);
    }

    /**
     * Sets a callback that's invoked when a cluster or a cluster item is clicked.
     *
     * @param callbacks the callback that's invoked when a cluster or an individual item is clicked.
     *                  To unset the callback, use <code>null</code>.
     */
    public void setCallbacks(@Nullable Callbacks<T> callbacks) {
        mRenderer.setCallbacks(callbacks);
    }

    /**
     * Add items to be clustered thus replacing the old ones.
     *
     * @param clusterItems the items to be clustered
     */
    public void addItems(@NonNull List<T> clusterItems) {
        checkNotNull(clusterItems);
        buildQuadTree(clusterItems);
    }

    /**
     * Add an item to be clustered thus replacing the old one.
     *
     * @param clusterItem the items to be clustered
     */
    public void addItem(@NonNull T clusterItem) {
        checkNotNull(clusterItem);
        mQuadTree.insert(clusterItem);
    }

    public void clearItems() {
        mQuadTree.clear();
    }

    /**
     * Sets the minimum size of a cluster. If the cluster size
     * is less than this value, display individual markers.
     */
    public void setMinClusterSize(int minClusterSize) {
        Preconditions.checkArgument(minClusterSize > 0);
        mMinClusterSize = minClusterSize;
    }

    @Override
    public void onCameraIdle() {
        cluster();
    }

    public void setRenderPostProcessor(@NonNull RenderPostProcessor<T> renderPostProcessor) {
        checkNotNull(renderPostProcessor);
        mRenderer.setRenderPostProcessor(renderPostProcessor);
    }

    private void buildQuadTree(@NonNull List<T> clusterItems) {
        if (mQuadTreeTask != null) {
            mQuadTreeTask.cancel(true);
        }

        mQuadTreeTask = new QuadTreeTask(clusterItems).executeOnExecutor(mExecutor);
    }

    public void cluster() {
        if (mClusterTask != null) {
            mClusterTask.cancel(true);
        }

        mClusterTask = new ClusterTask(mHuaweiMap.getProjection().getVisibleRegion().latLngBounds,
                mHuaweiMap.getCameraPosition().zoom).executeOnExecutor(mExecutor);
    }

    @NonNull
    private List<Cluster<T>> getClusters(@NonNull LatLngBounds latLngBounds, float zoomLevel) {
        List<Cluster<T>> clusters = new ArrayList<>();

        long tileCount = (long) (Math.pow(2, zoomLevel) * 2);

        double startLatitude = latLngBounds.northeast.latitude;
        double endLatitude = latLngBounds.southwest.latitude;

        double startLongitude = latLngBounds.southwest.longitude;
        double endLongitude = latLngBounds.northeast.longitude;

        double stepLatitude = 180.0 / tileCount;
        double stepLongitude = 360.0 / tileCount;

        if (startLongitude > endLongitude) { // Longitude +180°/-180° overlap.
            // [start longitude; 180]
            getClustersInsideBounds(clusters, startLatitude, endLatitude,
                    startLongitude, 180.0, stepLatitude, stepLongitude);
            // [-180; end longitude]
            getClustersInsideBounds(clusters, startLatitude, endLatitude,
                    -180.0, endLongitude, stepLatitude, stepLongitude);
        } else {
            getClustersInsideBounds(clusters, startLatitude, endLatitude,
                    startLongitude, endLongitude, stepLatitude, stepLongitude);
        }

        return clusters;
    }

    private void getClustersInsideBounds(@NonNull List<Cluster<T>> clusters,
                                         double startLatitude, double endLatitude,
                                         double startLongitude, double endLongitude,
                                         double stepLatitude, double stepLongitude) {
        long startX = (long) ((startLongitude + 180.0) / stepLongitude);
        long startY = (long) ((90.0 - startLatitude) / stepLatitude);

        long endX = (long) ((endLongitude + 180.0) / stepLongitude) + 1;
        long endY = (long) ((90.0 - endLatitude) / stepLatitude) + 1;

        for (long tileX = startX; tileX <= endX; tileX++) {
            for (long tileY = startY; tileY <= endY; tileY++) {
                double north = 90.0 - tileY * stepLatitude;
                double west = tileX * stepLongitude - 180.0;
                double south = north - stepLatitude;
                double east = west + stepLongitude;

                List<T> points = mQuadTree.queryRange(north, west, south, east);

                if (points.isEmpty()) {
                    continue;
                }

                if (points.size() >= mMinClusterSize) {
                    double totalLatitude = 0;
                    double totalLongitude = 0;

                    for (T point : points) {
                        totalLatitude += point.getLatitude();
                        totalLongitude += point.getLongitude();
                    }

                    double latitude = totalLatitude / points.size();
                    double longitude = totalLongitude / points.size();

                    clusters.add(new Cluster<>(latitude, longitude,
                            points, north, west, south, east));
                } else {
                    for (T point : points) {
                        clusters.add(new Cluster<>(point.getLatitude(), point.getLongitude(),
                                Collections.singletonList(point), north, west, south, east));
                    }
                }
            }
        }
    }

    private void quadTreeWriteLock(@NonNull Runnable runnable) {
        quadTreeLock.writeLock().lock();
        try {
            runnable.run();
        } finally {
            quadTreeLock.writeLock().unlock();
        }
    }

    private void quadTreeReadLock(@NonNull Runnable runnable) {
        quadTreeLock.readLock().lock();
        try {
            runnable.run();
        } finally {
            quadTreeLock.readLock().unlock();
        }
    }

    private class QuadTreeTask extends AsyncTask<Void, Void, Void> {

        private final List<T> mClusterItems;

        private QuadTreeTask(@NonNull List<T> clusterItems) {
            mClusterItems = clusterItems;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mQuadTree.clear();
            // Changed for loop to i
            for (int i = 0; i < mClusterItems.size(); i++) {
                mQuadTree.insert(mClusterItems.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            cluster();
            mQuadTreeTask = null;
        }
    }

    private class ClusterTask extends AsyncTask<Void, Void, List<Cluster<T>>> {

        private final LatLngBounds mLatLngBounds;
        private final float mZoomLevel;

        private ClusterTask(@NonNull LatLngBounds latLngBounds, float zoomLevel) {
            mLatLngBounds = latLngBounds;
            mZoomLevel = zoomLevel;
        }

        @Override
        protected List<Cluster<T>> doInBackground(Void... params) {
            return getClusters(mLatLngBounds, mZoomLevel);
        }

        @Override
        protected void onPostExecute(@NonNull List<Cluster<T>> clusters) {
            mRenderer.render(clusters);
            mClusterTask = null;
        }
    }
}
