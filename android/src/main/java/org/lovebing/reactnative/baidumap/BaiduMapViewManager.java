package org.lovebing.reactnative.baidumap;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapViewLayoutParams;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lovebing on 12/20/2015.
 */
public class BaiduMapViewManager extends ViewGroupManager<TextureMapView> {

    private static final String REACT_CLASS = "RCTBaiduMapView";

    private ThemedReactContext mReactContext;

    private ReadableArray childrenPoints;
  //  private HashMap<String, Marker> mMarkerMap = new HashMap<>();
   // private HashMap<String, List<Marker>> mMarkersMap = new HashMap<>();
    private TextView mMarkerText;
    private ClusterManager<MyItem> mClusterManager;

    public String getName() {
        return REACT_CLASS;
    }


    public void initSDK(Context context) {
        SDKInitializer.initialize(context);
    }

    public TextureMapView createViewInstance(ThemedReactContext context) {
        mReactContext = context;
        TextureMapView mapView = new TextureMapView(context);
        mClusterManager = new ClusterManager<MyItem>(mapView, mapView.getMap(),mReactContext);
        setListeners(mapView);
        return mapView;
    }
    @Override
    public void addView(TextureMapView parent, View child, int index) {
        if (childrenPoints != null) {
            Point point = new Point();
            ReadableArray item = childrenPoints.getArray(index);
            if (item != null) {
                point.set(item.getInt(0), item.getInt(1));
                MapViewLayoutParams mapViewLayoutParams = new MapViewLayoutParams
                    .Builder()
                    .layoutMode(MapViewLayoutParams.ELayoutMode.absoluteMode)
                    .point(point)
                    .build();
                parent.addView(child, mapViewLayoutParams);
            }
        }
    }

    @ReactProp(name = "zoomControlsVisible")
    public void setZoomControlsVisible(TextureMapView mapView, boolean zoomControlsVisible) {
        mapView.showZoomControls(zoomControlsVisible);
    }

    @ReactProp(name = "trafficEnabled")
    public void setTrafficEnabled(TextureMapView mapView, boolean trafficEnabled) {
        mapView.getMap().setTrafficEnabled(trafficEnabled);
    }

    @ReactProp(name = "baiduHeatMapEnabled")
    public void setBaiduHeatMapEnabled(TextureMapView mapView, boolean baiduHeatMapEnabled) {
        mapView.getMap().setBaiduHeatMapEnabled(baiduHeatMapEnabled);
    }

    @ReactProp(name = "mapType")
    public void setMapType(TextureMapView mapView, int mapType) {
        mapView.getMap().setMapType(mapType);
    }

    @ReactProp(name = "zoom")
    public void setZoom(TextureMapView mapView, float zoom) {
        MapStatus mapStatus = new MapStatus.Builder().zoom(zoom).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mapView.getMap().setMapStatus(mapStatusUpdate);
    }

    @ReactProp(name = "center")
    public void setCenter(TextureMapView mapView, ReadableMap position) {
       //  Log.i("setCenter","setCenter");
       // if (position != null) {
       //     double latitude = position.getDouble("latitude");
       //     double longitude = position.getDouble("longitude");
       //     LatLng point = new LatLng(latitude, longitude);
       //     MapStatus mapStatus = new MapStatus.Builder()
       //         .target(point)
       //         .build();
       //     MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
       //     mapView.getMap().setMapStatus(mapStatusUpdate);
       // }
    }

    @ReactProp(name = "marker")
    public void setMarker(TextureMapView mapView, ReadableMap option) {
//        if (option != null) {
//            String key = "marker_" + mapView.getId();
//            Marker marker = mMarkerMap.get(key);
//            if (marker != null) {
//                MarkerUtil.updateMaker(marker, option);
//            } else {
//                marker = MarkerUtil.addMarker(mapView, option);
//                mMarkerMap.put(key, marker);
//            }
//        }
    }

    @ReactProp(name = "markers")
    public void setMarkers(TextureMapView mapView, ReadableArray options) {
        if (options.size() <= 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(36.6386720382, 117.1135711670));
            mapView.getMap().setMapStatus(MapStatusUpdateFactory
                .newLatLngBounds(builder.build()));
        } else {
            List<MyItem> items = new ArrayList<MyItem>();
            mClusterManager.clearItems();
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < options.size(); i++) {
                ReadableMap option = options.getMap(i);
                builder.include(MarkerUtil.getLatLngFromOption(option));
                items.add(new MyItem(MarkerUtil.getLatLngFromOption(option)));
            }
            mClusterManager.addItems(items);
            mapView.getMap().setMapStatus(MapStatusUpdateFactory
                .newLatLngBounds(builder.build()));
        }
        Log.i("setMakers", "setMakers");
    }
    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;


        public MyItem(LatLng latLng) {
            mPosition = latLng;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            return BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_gcoding);
        }
    }


    @ReactProp(name = "childrenPoints")
    public void setChildrenPoints(TextureMapView mapView, ReadableArray childrenPoints) {
        this.childrenPoints = childrenPoints;
    }

    /**
     * @param mapView
     */
    private void setListeners(final TextureMapView mapView) {
        BaiduMap map = mapView.getMap();

        if (mMarkerText == null) {
            mMarkerText = new TextView(mapView.getContext());
            mMarkerText.setBackgroundResource(R.drawable.popup);
            mMarkerText.setPadding(32, 32, 32, 32);
        }
        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.e("onMapLoaded","onMapLoaded");
                sendEvent(mapView, "onMapLoaded", null);
            }
        });

        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapClick", writableMap);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putString("name", mapPoi.getName());
                writableMap.putString("uid", mapPoi.getUid());
                writableMap.putDouble("latitude", mapPoi.getPosition().latitude);
                writableMap.putDouble("longitude", mapPoi.getPosition().longitude);
                sendEvent(mapView, "onMapPoiClick", writableMap);
                return true;
            }
        });
        map.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            @Override
            public void onMapDoubleClick(LatLng latLng) {
                WritableMap writableMap = Arguments.createMap();
                writableMap.putDouble("latitude", latLng.latitude);
                writableMap.putDouble("longitude", latLng.longitude);
                sendEvent(mapView, "onMapDoubleClick", writableMap);
            }
        });

        map.setOnMapStatusChangeListener(mClusterManager);

        map.setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                Log.e("mClusterManager", "onClusterClick");
                mapView.getMap().setMapStatus(MapStatusUpdateFactory.zoomIn());
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {
                Log.e("mClusterManager", "onClusterItemClick");
                WritableMap writableMap = Arguments.createMap();
                WritableMap position = Arguments.createMap();
                position.putDouble("latitude", item.getPosition().latitude);
                position.putDouble("longitude", item.getPosition().longitude);
                writableMap.putMap("position", position);
                //  writableMap.putString("title", cluster.getTitle());
                sendEvent(mapView, "onMarkerClick", writableMap);
                return false;
            }
        });

    }

    /**
     * @param eventName
     * @param params
     */
    private void sendEvent(TextureMapView mapView, String eventName, @Nullable WritableMap params) {
        WritableMap event = Arguments.createMap();
        event.putMap("params", params);
        event.putString("type", eventName);
        mReactContext
            .getJSModule(RCTEventEmitter.class)
            .receiveEvent(mapView.getId(),
                "topChange",
                event);
    }
}
