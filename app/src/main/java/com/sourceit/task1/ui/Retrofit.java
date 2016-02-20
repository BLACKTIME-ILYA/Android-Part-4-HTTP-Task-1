package com.sourceit.task1.ui;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;

/**
 * Created by User on 15.02.2016.
 */
public class Retrofit {

    private static final String ENDPOINT = "https://restcountries.eu/rest";
    private static ApiInterface apiInterface;

    static {
        initialize();
    }

    interface ApiInterface {
        @GET("/v1/all")
        void getCountries(Callback<List<ObjectType>> callback);
    }

    public static void initialize() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        apiInterface = restAdapter.create(ApiInterface.class);
    }

    public static void getCountries(Callback<List<ObjectType>> callback) {
        apiInterface.getCountries(callback);
    }
}
