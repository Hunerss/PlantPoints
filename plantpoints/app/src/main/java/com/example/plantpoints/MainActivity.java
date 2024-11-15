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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plantpoints.api.ApiService;
import com.example.plantpoints.models.Point;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity implements MapListener {

    MapView mapView;
    IMapController controller;
    MyLocationNewOverlay myLocation;

    Button addPlantButton, confirmPlantButton, cancelPlantButton;
    LinearLayout addPlantWindow;
    EditText nameEdit, descriptionEdit;
    SeekBar rangeBar;
    TextView rangeText;
    ImageView locationButton;

    boolean addingPlant;

    List<Polygon> circles = new ArrayList<>();

    Polygon middleCircle;

    private static final int LOCATION_FINE_PERMISSION_CODE = 100;
    private static final int LOCATION_COARSE_PERMISSION_CODE = 101;
    private static final int BACKGROUND_LOCATION_PERMISSION_CODE = 102;
    private static final int INTERNET_PERMISSION_CODE = 103;

    private ArrayList<Point> pointsList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider provider = Configuration.getInstance();
        provider.setUserAgentValue(BuildConfig.LIBRARY_PACKAGE_NAME);
        provider.setOsmdroidBasePath(getStorage());
        provider.setOsmdroidTileCache(getStorage());

        setContentView(R.layout.activity_main);

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://plantpoints.great-site.net/") // Adres bazowy API
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);

        fetchPoints();

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_FINE_PERMISSION_CODE);
        //checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION, BACKGROUND_LOCATION_PERMISSION_CODE);
//        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATION_COARSE_PERMISSION_CODE);
        checkPermission(Manifest.permission.INTERNET, INTERNET_PERMISSION_CODE);

        addPlantButton = findViewById(R.id.add_plant_button);
        addPlantWindow = findViewById(R.id.add_plant_window);

        confirmPlantButton = findViewById(R.id.confirm_plant_button);
        cancelPlantButton = findViewById(R.id.cancel_plant_button);
        locationButton = findViewById(R.id.locate_button);
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
            // Pobieranie danych z pól tekstowych
            final String name = nameEdit.getText().toString();
//            final String name = "name";
            final String description = descriptionEdit.getText().toString();
//            final String description = "description";
            final int range = rangeBar.getProgress();
            final double xValue = mapView.getMapCenter().getLatitude(), yValue = mapView.getMapCenter().getLongitude();

            Log.d("add point",  name + " " + description + " " + range + "\n" + xValue + " x " + yValue);

            /*try {
//                range = Integer.parseInt(rangeInput.getText().toString());
                range = 15;
//                xValue = Double.parseDouble(xValueInput.getText().toString());
                xValue = 0.1234;
//                yValue = Double.parseDouble(yValueInput.getText().toString());
                yValue = -0.4321;
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                return;
            }*/

            if (name.isEmpty() || description.isEmpty() || range <= 0) {
                Toast.makeText(MainActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            // Utworzenie obiektu Point
            Point point = new Point(name, description, range, xValue, yValue);

            // Wysłanie żądania Retrofit
            apiService.addPoint(point).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Point added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to add point", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    //Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Retrofit error", "Error: " + t.getMessage());
                }
            });
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

        locationButton.setOnClickListener(view -> {
            GoToMyLocation();
            controller.zoomTo(17.0);
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
        if (middleCircle == null) return;

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

    private void fetchPoints() {
        //Call<List<Point>> call = apiService.getPoints();
        Call<String> call = apiService.getPoints();

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //pointsList.clear();
                    //pointsList.addAll(response.body()); // Dodanie punktów do ArrayList
                    Log.d("response", response.body());
                    //Toast.makeText(MainActivity.this, "Pobrano punkty: " + pointsList.size(), Toast.LENGTH_SHORT).show();

                    drawCircles();
                } else {
                    Toast.makeText(MainActivity.this, "Nie znaleziono punktów", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
//                Toast.makeText(MainActivity.this, "Błąd: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Retrofit", "Błąd: " + t.getMessage());
                //throw new RuntimeException("Błąd: " + t.getMessage());
            }
        });


//        call.enqueue(new Callback<List<Point>>() {
//            @Override
//            public void onResponse(Call<List<Point>> call, Response<List<Point>> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    pointsList.clear();
//                    pointsList.addAll(response.body()); // Dodanie punktów do ArrayList
//                    Toast.makeText(MainActivity.this, "Pobrano punkty: " + pointsList.size(), Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(MainActivity.this, "Nie znaleziono punktów", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Point>> call, Throwable t) {
////                Toast.makeText(MainActivity.this, "Błąd: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                Log.e("Retrofit", "Błąd: " + t.getMessage());
//                //throw new RuntimeException("Błąd: " + t.getMessage());
//            }
//        });
    }

    void drawCircles(){

        for (int i = 0; i < circles.size(); i++) {
            mapView.getOverlays().remove(circles.get(i));
        }
        circles.clear();

        for (int i = 0; i < pointsList.size(); i++) {

            circles.add(new Polygon(mapView));

            mapView.getOverlays().add(circles.get(i));

            double radius = pointsList.get(i).getRange();
            double longi = pointsList.get(i).getY_value();
            double latit = pointsList.get(i).getX_value();
            ArrayList<GeoPoint> circlePoints = new ArrayList<>();
            for (float j = 0; j < 6.28; j += 0.1) {
                double x = latit + radius * sin(j) / 88 * lerp(1, 0.09, globeSmooth(abs(latit) / 85));
                double y = longi + radius * cos(j) / 88;

                circlePoints.add(new GeoPoint(x, y));
            }
            middleCircle.setPoints(circlePoints);
            middleCircle.setFillColor(Color.argb(50, 91, 235, 139));
            middleCircle.setStrokeColor(Color.argb(50, 36, 158, 77));
        }
    }
}