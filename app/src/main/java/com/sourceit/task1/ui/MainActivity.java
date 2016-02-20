package com.sourceit.task1.ui;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.LinkedList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private String file_name;
    private String write;
    private String read;
    private static final int FIRST = 0;
    private static final int SECOND = 1;
    private static final int THIRD = 2;

    private Intent intent;
    private Gson gson;

    private String regions;
    private String subregions;
    private String countries;
    public static String info;

    private Spinner spinner_filter;
    private EditText filter;
    private RecyclerView list;
    private List<ObjectType> localObjectsType;
    private final LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());

    private LinkedList<String> tempCurrentObjects;
    private ArrayList<String> currentObjects;
    private String currentList;
    private String selectedItem;
    private String filterState;
    private String[] filterChooses;
    private String byCapital;
    private String byLanguage;
    private String byTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regions = getString(R.string.regions_string);
        subregions = getString(R.string.subregions_string);
        countries = getString(R.string.сщгтекшуы_string);
        info = getString(R.string.info_string);

        file_name = getString(R.string.json_countries_string);
        write = getString(R.string.write_string);
        read = getString(R.string.read_string);

        filterChooses = getResources().getStringArray(R.array.filters);
        filterState = filterChooses[FIRST];
        byTitle = filterChooses[FIRST];
        byCapital = filterChooses[THIRD];
        byLanguage = filterChooses[SECOND];

        gson = new Gson();
        intent = new Intent(this, Main2Activity.class);

        spinner_filter = (Spinner) findViewById(R.id.spinner_filter);
        filter = (EditText) findViewById(R.id.edit_filter);
        list = (RecyclerView) findViewById(R.id.recycler_list);
        currentObjects = new ArrayList<>();
        tempCurrentObjects = new LinkedList<>();

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

            if (params[0].equals(write)) {
                L.d("write");
                try {
                    String jsonRegions = gson.toJson(localObjectsType);
                    FileOutputStream fos = openFileOutput(file_name, Context.MODE_PRIVATE);
                    try {
                        fos.write(jsonRegions.getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (params[0].equals(read)) {
                created = true;
                L.d("read");
                try {
                    FileInputStream fis = openFileInput(file_name);
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

            if (!fileExistance(file_name)) {
                Retrofit.getCountries(new Callback<List<ObjectType>>() {
                    @Override
                    public void success(List<ObjectType> countries, Response response) {
                        localObjectsType = countries;
                        setAdapter();
                        new MyAsyncTask().execute(write);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Toast.makeText(getApplicationContext(), "something went wrong :(", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                L.d("file exist");
                new MyAsyncTask().execute(read);
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
        for (Iterator<String> it = currentObjects.iterator(); it.hasNext(); ) {
            tempCurrentObjects.add(it.next());
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
        tempCurrentObjects.clear();
        getObjects();
        list.getAdapter().notifyDataSetChanged();
    }

    private void setAdapter() {
        getObjects();

        list.setAdapter(new MyRecyclerAdapter(tempCurrentObjects, new OnItemClickWatcher<String>() {
            @Override
            public void onItemClick(View v, int position, String item) {
                L.d("click " + item);
                selectedItem = item.toLowerCase();
                levelsObjects();
            }
        }));

        filter.addTextChangedListener(new MyTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    L.d("try");
                    if (filterState.equals(byTitle)) {

                        for (Iterator<String> it = currentObjects.iterator(); it.hasNext(); ) {
                            String tempValue = it.next();
                            L.d("temp value " + tempValue);

                            if (tempValue.toLowerCase().contains(filter.getText().toString().toLowerCase())) {
                                if (!tempCurrentObjects.contains(tempValue)) {
                                    tempCurrentObjects.add(tempValue);
                                }
                            } else {
                                if (tempCurrentObjects.contains(tempValue)) {
                                    tempCurrentObjects.remove(tempValue);
                                }
                            }
                        }
                    } else if (filterState.equals(byLanguage)) {
                        if (currentList.equals(countries)) {
                            for (Iterator<ObjectType> it = localObjectsType.iterator(); it.hasNext(); ) {
                                ObjectType tempValue = it.next();
                                boolean containLang = false;
                                for (String language : tempValue.languages) {
                                    if (language.contains(filter.getText().toString().toLowerCase())) {
                                        containLang = true;
                                        break;
                                    }
                                }
                                if (containLang) {
                                    addRemove(true, tempValue);
                                } else {
                                    addRemove(false, tempValue);
                                }
                            }
                        }
                    } else if (filterState.equals(byCapital)) {
                        if (currentList.equals(countries)) {
                            for (Iterator<ObjectType> it = localObjectsType.iterator(); it.hasNext(); ) {
                                ObjectType tempValue = it.next();

                                if (tempValue.getCapital().toLowerCase().contains(filter.getText().toString().toLowerCase())) {
                                    L.d("temp value capital" + tempValue.getCapital());
                                    L.d("filter text: " + filter.getText().toString());
                                    addRemove(true, tempValue);
                                } else {
                                    addRemove(false, tempValue);
                                }
                            }
                        }
                    }
                    list.getAdapter().notifyDataSetChanged();
                } catch (NumberFormatException e) {
                }
            }
        });

        spinner_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                filterState = filterChooses[position];
                L.d("filter state: " + filterState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void addRemove(boolean choose, ObjectType value) {
        if (choose) {
            if (!tempCurrentObjects.contains(value.getName())) {
                tempCurrentObjects.add(value.getName());
            }
        } else {
            if (tempCurrentObjects.contains(value.getName())) {
                tempCurrentObjects.remove(value.getName());
            }
        }
    }
}
