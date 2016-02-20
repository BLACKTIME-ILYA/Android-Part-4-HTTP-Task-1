package com.sourceit.task1.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.sourceit.task1.R;
import com.sourceit.task1.utils.L;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    Intent intent;

    private String regions;
    private String subregions;
    private String countries;
    public static String info;

    private RecyclerView list;
    private List<ObjectType> localObjectsType;
    private final LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());

    private ArrayList<String> currentObjects;
    private String currentList;
    private String selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regions = getString(R.string.regions_string);
        subregions = getString(R.string.subregions_string);
        countries = getString(R.string.сщгтекшуы_string);
        info = getString(R.string.info_string);

        intent = new Intent(this, Main2Activity.class);

        list = (RecyclerView) findViewById(R.id.recycler_list);
        currentObjects = new ArrayList<>();

        list.setLayoutManager(layoutManager);

        levelsWithRetrofit();
    }

    private void levelsWithRetrofit() {
        if (currentList == null) {
            currentList = regions;
            Retrofit.getCountries(new Callback<List<ObjectType>>() {
                @Override
                public void success(List<ObjectType> countries, Response response) {
                    localObjectsType = countries;
                    getObjects();

                    list.setAdapter(new MyRecyclerAdapter(currentObjects, new OnItemClickWatcher<String>() {
                        @Override
                        public void onItemClick(View v, int position, String item) {
                            L.d("click " + item);
                            selectedItem = item.toLowerCase();
                            levelsWithRetrofit();
                        }
                    }));
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (currentList.equals(regions)) {
            L.d("mygetregions");
            currentList = subregions;
            Retrofit.getRegion(selectedItem, new Callback<List<ObjectType>>() {
                @Override
                public void success(List<ObjectType> region, Response response) {
                    localObjectsType = region;
                    currentObjects.clear();
                    getObjects();
                    list.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (currentList.equals(subregions)) {
            L.d("mygetsubregions");
            currentList = countries;
            Retrofit.getSubregion(selectedItem, new Callback<List<ObjectType>>() {
                @Override
                public void success(List<ObjectType> objectTypes, Response response) {
                    localObjectsType = objectTypes;
                    currentObjects.clear();
                    getObjects();
                    list.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                }

            });

        } else if (currentList.equals(countries)) {
            L.d("mygetcountry");
            Retrofit.getCountry(selectedItem, new Callback<List<ObjectType>>() {
                @Override
                public void success(List<ObjectType> objectTypes, Response response) {
                    intent.putExtra(info, objectTypes.get(0));
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void getObjects() {

        for (ObjectType objectType : localObjectsType) {
            if (!currentObjects.isEmpty()) {
                if (currentList.equals(regions)) {
                    if (!currentObjects.contains(objectType.getRegion())) {
                        currentObjects.add(objectType.getRegion());
                    }

                } else if (currentList.equals(subregions)) {
                    if (!currentObjects.contains(objectType.getSubregion())) {
                        currentObjects.add(objectType.getSubregion());
                    }
                } else if (currentList.equals(countries)) {
                        currentObjects.add(objectType.getName());
                }
            } else {
                if (currentList.equals(regions)) {
                    currentObjects.add(objectType.getRegion());
                } else if (currentList.equals(subregions)) {
                    currentObjects.add(objectType.getSubregion());
                } else if (currentList.equals(countries)) {
                    currentObjects.add(objectType.getName());
                }
            }
        }
    }
}
