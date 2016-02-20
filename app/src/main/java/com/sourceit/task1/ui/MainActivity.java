package com.sourceit.task1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sourceit.task1.R;
import com.sourceit.task1.utils.L;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    public static final String FILE_NAME = "json_countries";
    public static final String WRITE = "write";
    public static final String READ = "READ";

    private Intent intent;
    private Gson gson;

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

        gson = new Gson();
        intent = new Intent(this, Main2Activity.class);

        list = (RecyclerView) findViewById(R.id.recycler_list);
        currentObjects = new ArrayList<>();

        list.setLayoutManager(layoutManager);

        levelsObjects();
    }

    private boolean fileExistance(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    private class MyAsyncTask extends AsyncTask<String, Void, Void> {
        boolean created;

        @Override
        protected Void doInBackground(String... params) {

            if (params[0].equals(WRITE)) {
                L.d("write");
                try {
                    String jsonRegions = gson.toJson(localObjectsType);
                    FileOutputStream fos = openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                    try {
                        fos.write(jsonRegions.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (params[0].equals(READ)) {
                created = true;
                L.d("read");
                try {
                    FileInputStream fis = openFileInput(FILE_NAME);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    StringBuilder text = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            text.append(line);
                        }
                        fis.close();
                        Type collectionType = new TypeToken<List<ObjectType>>() {
                        }.getType();
                        localObjectsType = gson.fromJson(text.toString(), collectionType);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (created) {
                setAdapter();
            }
        }
    }

    private void levelsObjects() {
        if (currentList == null) {
            currentList = regions;

            if (!fileExistance(FILE_NAME)) {
                Retrofit.getCountries(new Callback<List<ObjectType>>() {
                    @Override
                    public void success(List<ObjectType> countries, Response response) {
                        localObjectsType = countries;
                        setAdapter();
                        new MyAsyncTask().execute(WRITE);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                L.d("file exist");
                new MyAsyncTask().execute(READ);
            }
        } else if (currentList.equals(regions)) {
            L.d("mygetregions");
            currentList = subregions;
            setList();

        } else if (currentList.equals(subregions)) {
            L.d("mygetsubregions");
            currentList = countries;
            setList();

        } else if (currentList.equals(countries)) {
            L.d("mygetcountry");
            for (ObjectType localObject : localObjectsType) {
                if (localObject.getName().toLowerCase().equals(selectedItem)) {
                    intent.putExtra(info, localObject);
                    startActivity(intent);
                }
            }
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

    private void setList() {

        ArrayList<ObjectType> tempCountries = new ArrayList<>();

        for (Iterator<ObjectType> it = localObjectsType.iterator(); it.hasNext(); ) {
            L.d("iterator from local objects to temp");
            tempCountries.add(it.next());
        }
        L.d("temp countries size" + tempCountries.size());
        localObjectsType.clear();
        L.d("temp countries size" + tempCountries.size());

        if (currentList.equals(subregions)) {
            for (Iterator<ObjectType> it = tempCountries.iterator(); it.hasNext(); ) {
                ObjectType tempCountry = it.next();
                L.d("tempCountry region: " + tempCountry.getRegion());
                if (tempCountry.getRegion().toLowerCase().equals(selectedItem)) {
                    L.d("local objects add");
                    localObjectsType.add(tempCountry);
                }
            }
        } else if (currentList.equals(countries)) {
            for (Iterator<ObjectType> it = tempCountries.iterator(); it.hasNext(); ) {
                ObjectType tempCountry = it.next();
                if (tempCountry.getSubregion().toLowerCase().equals(selectedItem)) {
                    localObjectsType.add(tempCountry);
                }
            }
        }

        currentObjects.clear();
        getObjects();
        list.getAdapter().notifyDataSetChanged();
    }

    private void setAdapter() {
        getObjects();

        list.setAdapter(new MyRecyclerAdapter(currentObjects, new OnItemClickWatcher<String>() {
            @Override
            public void onItemClick(View v, int position, String item) {
                L.d("click " + item);
                selectedItem = item.toLowerCase();
                levelsObjects();
            }
        }));
    }
}
