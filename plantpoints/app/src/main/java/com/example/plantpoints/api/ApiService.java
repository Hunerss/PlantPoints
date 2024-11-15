package com.example.plantpoints.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import com.example.plantpoints.models.Point;

import java.util.List;

public interface ApiService {
    @POST("addPoint.php")
    Call<Void> addPoint(@Body Point point);

    @GET("getPoints.php")
    Call<List<Point>> getPoints();
}
