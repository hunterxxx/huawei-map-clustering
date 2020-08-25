# Huawei Map Clustering
A high performance marker clustering library for Huawei Map.</br>
Clustering folder is the library.</br>
App is the example, check it out for easy implementation.

|<img src="Screenshots/100k.gif" width="320" height="585">|<img src="https://miro.medium.com/max/262/1*0DUmGcxkFOpp6XDdY2Dalw.png" width="320" height="585">|<img src="https://miro.medium.com/max/291/1*bNHT0sDdBxaho2rpXJQi-A.png" width="320" height="400">|
|:---:|:---:|:---:|
| 100K Clustered Markers | Library needed | Demo App |

Explained in article:
https://medium.com/@heydjbaby/100k-clustered-markers-with-huawei-map-ffcba4168727

## Motivation
Why not use [Huawei built in .clusterable(true)](https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/hms-map-drawonthemap#h2-1586915875534)? Because it's very slow for >100 markers, which causes ANRs. But this library can easily handle thousands of markers (the video above demonstrates the sample application with 100 000 markers running on Huawei P40 Lite).


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

2. Create an instance of ClusterManager and set it as a camera idle listener using `HuaweiMap.setOnCameraIdleListener(...)`:

```java
ClusterManager<SampleClusterItem> clusterManager = new ClusterManager<>(context, huaweiMap);
huaweiMap.setOnCameraIdleListener(clusterManager);
```
3. Populate ClusterManager with items using `ClusterManager.addItems(...)`:

```java
List<SampleClusterItem> clusterItems = generateSampleClusterItems();
clusterManager.addItems(clusterItems);
```

## Show your support
This project is completely open source, feel free to make a pull request or report an issue.
<br/>
Give a ⭐️ if this project helped you!

