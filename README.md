# Huawei Map Clustering
A high performance marker clustering library for Huawei Map

<img src="Screenshots/100k.gif" width="320" height="685">

## Motivation
Why not use [Huawei built in .clusterable(true)](appmarket://details?id=" + "de.dvb.dvbmobil")? Because it's very slow for >100 markers, which causes ANRs. But this library can easily handle thousands of markers (the video above demonstrates the sample application with 100 000 markers running on Huawei P40 Lite).


## Integration
1. Implement `ClusterItem` to represent a marker on the map. The cluster item returns the position of the marker and an optional title or snippet:

```java

class SampleClusterItem implements ClusterItem {

    private final LatLng location;

    SampleClusterItem(@NonNull LatLng location) {
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

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
```

2. Create an instance of ClusterManager and set it as a camera idle listener using `GoogleMap.setOnCameraIdleListener(...)`:

```java
ClusterManager<SampleClusterItem> clusterManager = new ClusterManager<>(context, googleMap);
googleMap.setOnCameraIdleListener(clusterManager);
```

3. To add a callback that's invoked when a cluster or a cluster item is clicked, use `ClusterManager.setCallbacks(...)`:

```java
clusterManager.setCallbacks(new ClusterManager.Callbacks<SampleClusterItem>() {
            @Override
            public boolean onClusterClick(@NonNull Cluster<SampleClusterItem> cluster) {
                Log.d(TAG, "onClusterClick");
                return false;
            }

            @Override
            public boolean onClusterItemClick(@NonNull SampleClusterItem clusterItem) {
                Log.d(TAG, "onClusterItemClick");
                return false;
            }
        });
```

4. To customize the icons create an instance of `IconGenerator` and set it using `ClusterManager.setIconGenerator(...)`. You can also use the default implementation `DefaultIconGenerator` and customize the style of icons using `DefaultIconGenerator.setIconStyle(...)`.

5. Populate ClusterManager with items using `ClusterManager.addItems(...)`:

```java
List<SampleClusterItem> clusterItems = generateSampleClusterItems();
clusterManager.addItems(clusterItems);
```

## Show your support
This project is completely open source, feel free to make a pull request or report an issue.
<br/>
Give a ⭐️ if this project helped you!

