package com.example.plantpoints;

import static org.osmdroid.tileprovider.util.StorageUtils.getStorage;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements MapListener {

    MapView mapView;
    IMapController controller;
    MyLocationNewOverlay myLocation;

    Button addPlantButton, confirmPlantButton, cancelPlantButton;
    LinearLayout addPlantWindow;
    EditText nameEdit, descriptionEdit;
    SeekBar rangeBar;
    TextView rangeText;

    boolean addingPlant;

    List<Polygon> circles = new ArrayList<>();

    Polygon middleCircle;

    private static final int LOCATION_FINE_PERMISSION_CODE = 100;
    private static final int LOCATION_COARSE_PERMISSION_CODE = 101;
    private static final int BACKGROUND_LOCATION_PERMISSION_CODE = 102;
    private static final int INTERNET_PERMISSION_CODE = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());

        setContentView(R.layout.activity_main);


        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_FINE_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, BACKGROUND_LOCATION_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_COARSE_PERMISSION_CODE);
        checkPermission(Manifest.permission.INTERNET, INTERNET_PERMISSION_CODE);

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

        GoToMyLocation();

        controller.setZoom(6.0);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);

        mapView.getOverlays().add(myLocation);
        mapView.addMapListener(this);

        mapView.getOverlayManager().add(new Overlay() {
            @Override
            public void draw(Canvas pCanvas, MapView pMapView, boolean pShadow) {
                super.draw(pCanvas, pMapView, pShadow);
                if (addingPlant)
                    DrawMiddleCircle();
            }
        });


        addPlantButton.setOnClickListener(view -> {
            addPlantWindow.setVisibility(View.VISIBLE);
            addingPlant = true;
            nameEdit.setText("");
            descriptionEdit.setText("");
            rangeBar.setProgress(5);
            middleCircle = new Polygon(mapView);
            middleCircle.setOnClickListener((polygon, mapView, eventPos) -> {
                middleCircle.closeInfoWindow();
                return false;
            });

            mapView.getOverlays().add(middleCircle);
            DrawMiddleCircle();
            RefreshMap();
        });

        confirmPlantButton.setOnClickListener(v -> {
//            final String name = nameInput.getText().toString();
//            final String description = descriptionInput.getText().toString();
//            final int range;
//            final double xValue, yValue;
//            try {
//                range = Integer.parseInt(rangeInput.getText().toString());
//                xValue = Double.parseDouble(xValueInput.getText().toString());
//                yValue = Double.parseDouble(yValueInput.getText().toString());
//            } catch (NumberFormatException e) {
//                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show());
//                return;
//            }
//
//            if (name.isEmpty() || description.isEmpty() || range <= 0) {
//                runOnUiThread(() -> Toast.makeText(MainActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show());
//                return;
//            }

            // Utworzenie JSON-a
            JSONObject data = new JSONObject();
            try {
                data.put("name", "name");
//                data.put("name", name);
                data.put("description", "description");
//                data.put("description", description);
                data.put("range", 15);
//                data.put("range", range);
                data.put("x_value", 0.2134);
//                data.put("x_value", xValue);
                data.put("y_value", -0.4324);
//                data.put("y_value", yValue);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error creating JSON data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Wysłanie żądania POST w oddzielnym wątku
            new Thread(() -> {
                try {
                    URL url = new URL("http://plantpoints.great-site.net/addPoint.php"); // URL do API
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(10000); // 10 sekund timeout
                    conn.setReadTimeout(10000);    // 10 sekund timeout
                    conn.setDoOutput(true);

                    // Wysłanie danych
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(data.toString().getBytes("UTF-8"));
                    }

                    // Sprawdzenie kodu odpowiedzi
                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Odczytanie odpowiedzi
                        Scanner scanner = new Scanner(conn.getInputStream());
                        final StringBuilder response = new StringBuilder();
                        while (scanner.hasNext()) {
                            response.append(scanner.nextLine());
                        }
                        scanner.close();

                        // Wyświetlenie odpowiedzi
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Server error: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error sending data", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        cancelPlantButton.setOnClickListener(view -> {
            addPlantWindow.setVisibility(View.GONE);
            addingPlant = false;
            mapView.getOverlays().remove(middleCircle);
            RefreshMap();
        });

        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                rangeText.setText(i + " km");
                DrawMiddleCircle();
                RefreshMap();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void GoToMyLocation() {
        myLocation.enableMyLocation();
        myLocation.enableFollowLocation();
        myLocation.setDrawAccuracyEnabled(true);
        myLocation.runOnFirstFix(() -> runOnUiThread(() ->{
            controller.setCenter(myLocation.getMyLocation());
            controller.animateTo(myLocation.getMyLocation());
        }));
    }

    private void DrawMiddleCircle() {
        double radius = rangeBar.getProgress();
        double longi = mapView.getMapCenter().getLongitude();
        double latit = mapView.getMapCenter().getLatitude();
        ArrayList<GeoPoint> circlePoints = new ArrayList<>();
        for (float i = 0; i < 6.28; i += 0.1) {
            double x = latit + radius * sin(i) / 88 * lerp(1, 0.09, globeSmooth(abs(latit) / 85));
            double y = longi + radius * cos(i) / 88;

            circlePoints.add(new GeoPoint(x, y));
        }
        middleCircle.setPoints(circlePoints);
        middleCircle.setFillColor(Color.argb(50, 27, 192, 63));
        middleCircle.setStrokeColor(Color.argb(50, 79, 219, 110));
    }

    void RefreshMap(){
        GeoPoint center = (GeoPoint)mapView.getMapCenter();
        mapView.getController().setCenter(center);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        else
            Log.d("permission", "Permission already granted");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_FINE_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Log.d("permission", "Location Fine Permission Granted");
                GoToMyLocation();
            }
            else {
                Log.d("permission", "Location Fine Permission Denied");
            }
        }
        else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.d("permission", "Background Location Permission Granted");
            else
                Log.d("permission", "Background Location Permission Denied");
        }
        else if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.d("permission", "Internet Permission Granted");
            else
                Log.d("permission", "Internet Permission Denied");
        }
        else if (requestCode == LOCATION_COARSE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.d("permission", "Coarse Location Permission Granted");
            else
                Log.d("permission", "Coarse Location Permission Denied");
        }
    }
}