package com.example.plantpoints;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MapListener {

    MapView mapView;
    IMapController controller;
    MyLocationNewOverlay myLocation;
    MapEventsReceiver receiver;

    TextView debugText;
    SeekBar debugSeekbar;

    Button addPlantButton, confirmPlantButton, cancelPlantButton;
    LinearLayout addPlantWindow;
    EditText nameEdit, descriptionEdit;
    SeekBar rangeBar;
    TextView rangeText;

    boolean addingPlant;

    List<Polygon> circles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());

        setContentView(R.layout.activity_main);

        debugText = findViewById(R.id.debug_text);
        debugSeekbar = findViewById(R.id.debug_seekbar);

        addPlantButton = findViewById(R.id.add_plant_button);
        addPlantWindow = findViewById(R.id.add_plant_window);

        confirmPlantButton = findViewById(R.id.confirm_plant_button);
        cancelPlantButton = findViewById(R.id.cancel_plant_button);
        nameEdit = findViewById(R.id.name_plant_box);
        descriptionEdit = findViewById(R.id.description_plant_box);
        rangeText = findViewById(R.id.range_text);
        rangeBar = findViewById(R.id.range_bar);

        mapView = findViewById(R.id.main_map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getMapCenter();
        mapView.setMultiTouchControls(true);
        mapView.getLocalVisibleRect(new Rect());
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setScrollableAreaLimitDouble(new BoundingBox(85, 180, -85, -180));
        mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().getMaxLatitude(),
                MapView.getTileSystem().getMinLatitude(), 0);
        mapView.setMaxZoomLevel(20.0);
        mapView.setMinZoomLevel(4.0);

        myLocation = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        controller = mapView.getController();
        controller.zoomTo(17.0);

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

        mapView.getOverlayManager().add(new Overlay() {
            @Override
            public void draw(Canvas pCanvas, MapView pMapView, boolean pShadow) {
                super.draw(pCanvas, pMapView, pShadow);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e, MapView mapView) {

                /*for (int i = 0; i < circles.size(); i++) {
                    mapView.getOverlays().remove(circles.get(i));
                }
                circles.clear();

                controller.animateTo(mapView.getMapCenter());
                String s = mapView.getMapCenter().getLongitude() + " x " + mapView.getMapCenter().getLatitude();
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();



                Polygon circle = new Polygon(mapView);
                double radius = 2;
                double longi = mapView.getMapCenter().getLongitude();
                double latit = mapView.getMapCenter().getLatitude();
                ArrayList<GeoPoint> circlePoints = new ArrayList<>();
                for (float i = 0; i < 6.28; i+=0.1) {
                    double x = latit + radius * sin(i);
                    double y = longi + radius*cos(i);
                    circlePoints.add(new GeoPoint(x, y));
                }
                circle.setPoints(circlePoints);
                circle.setFillColor(Color.argb(50, 27, 192, 63));
                circle.setStrokeColor(Color.argb(50, 79, 219, 110));
                circles.add(circle);
                mapView.getOverlays().add(circle);


                //Toast.makeText(getApplicationContext(), e.getX() + " x " + e.getY(), Toast.LENGTH_SHORT).show();
*/
                //return super.onSingleTapUp(e, mapView);
                return false;
            }

            @Override
            public boolean onTouchEvent(MotionEvent e, MapView mapView) {
                if (addingPlant) {
                    String s = mapView.getMapCenter().getLongitude() + " x " + mapView.getMapCenter().getLatitude();
                    //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

                    DrawMiddleCircle();

                    //debugText.setText(s + " " + /*lerp(1, 0.09, sin(abs(latit) / 85 * 1.62)) + " x " + */(double)round(globeSmooth(abs(latit) / 85) * 1000) / 1000);

                    //Toast.makeText(getApplicationContext(), e.getX() + " x " + e.getY(), Toast.LENGTH_SHORT).show();

                }

                return super.onTouchEvent(e, mapView);
            }
        });


        addPlantButton.setOnClickListener(view -> {
            addPlantWindow.setVisibility(View.VISIBLE);
            addingPlant = true;
            rangeBar.setProgress(5);
            DrawMiddleCircle();
        });

        cancelPlantButton.setOnClickListener(view -> {
            addPlantWindow.setVisibility(View.GONE);
            addingPlant = false;
            ClearCircles();
        });

        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rangeText.setText(i + " km");
                DrawMiddleCircle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void DrawMiddleCircle() {
        ClearCircles();

        Polygon circle = new Polygon(mapView);
        //double radius = 1;
        double radius = rangeBar.getProgress();
        double longi = mapView.getMapCenter().getLongitude();
        double latit = mapView.getMapCenter().getLatitude();
        ArrayList<GeoPoint> circlePoints = new ArrayList<>();
        for (float i = 0; i < 6.28; i += 0.1) {
            double x = latit + radius * sin(i) / 88 * lerp(1, 0.09, globeSmooth(abs(latit) / 85));
            double y = longi + radius * cos(i) / 88;

            circlePoints.add(new GeoPoint(x, y));
        }
        circle.setPoints(circlePoints);
        circle.setFillColor(Color.argb(50, 27, 192, 63));
        circle.setStrokeColor(Color.argb(50, 79, 219, 110));
        circles.add(circle);
        mapView.getOverlays().add(circle);

        debugText.setText(getDistanceFromLatLonInKm(myLocation.getMyLocation().getLatitude(), myLocation.getMyLocation().getLongitude(), latit, longi) + " km");
    }

    private void ClearCircles() {
        for (int i = 0; i < circles.size(); i++) {
            mapView.getOverlays().remove(circles.get(i));
        }
        circles.clear();
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }

    double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    double lerp(double v0, double v1, double t) {
        return (1 - t) * v0 + t * v1;
    }

    double globeSmooth(double x) {
        return 1 - Math.cos((x * Math.PI) / 2);
    }
}