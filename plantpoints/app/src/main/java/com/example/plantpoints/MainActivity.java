package com.example.plantpoints;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MapListener {

    MapView mapView;
    IMapController controller;
    MyLocationNewOverlay myLocation;
    MapEventsReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.main_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getMapCenter();
        mapView.setMultiTouchControls(true);
        mapView.getLocalVisibleRect(new Rect());

        myLocation = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        controller = mapView.getController();

        myLocation.enableMyLocation();
        myLocation.enableFollowLocation();
        myLocation.setDrawAccuracyEnabled(true);
        myLocation.runOnFirstFix(() -> runOnUiThread(() ->{
            controller.setCenter(myLocation.getMyLocation());
            controller.animateTo(myLocation.getMyLocation());
        }));

        controller.setZoom(6.0);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapView.getOverlays().add(myLocation);
        mapView.addMapListener(this);

        receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Toast.makeText(MainActivity.this, "HAHA", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };



        mapView.getOverlayManager().add(new Overlay() {
            @Override
            public void draw(Canvas pCanvas, MapView pMapView, boolean pShadow) {
                super.draw(pCanvas, pMapView, pShadow);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e, MapView mapView) {
                /*controller.animateTo(mapView.getMapCenter());
                String s = mapView.getMapCenter().getLongitude() + " x " + mapView.getMapCenter().getLatitude();
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();



                Polygon circle = new Polygon(mapView);
                double radius = 2;
                double longi = mapView.getMapCenter().getLongitude();
                double latit = mapView.getMapCenter().getLatitude();
                ArrayList<GeoPoint> circlePoints = new ArrayList<>();
                for (float i = 0; i < 6.28; i+=0.1) {
                    circlePoints.add(new GeoPoint(latit + radius*sin(i), longi + radius*cos(i)));
                }
                circle.setPoints(circlePoints);
                circle.setFillColor(Color.argb(50, 27, 192, 63));
                circle.setStrokeColor(Color.argb(50, 79, 219, 110));
                mapView.getOverlays().add(circle);*/


                Toast.makeText(getApplicationContext(), e.getX() + " x " + e.getY(), Toast.LENGTH_SHORT).show();

                return super.onSingleTapUp(e, mapView);
            }
        });


    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }
}