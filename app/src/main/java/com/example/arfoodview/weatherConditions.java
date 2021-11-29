package com.example.arfoodview;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.common.api.Status;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.compat.AutocompleteFilter;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.ui.PlaceAutocomplete;
import com.google.android.libraries.places.compat.ui.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.List;
import java.util.Locale;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class weatherConditions extends AppCompatActivity implements PlaceSelectionListener {

    final String APP_ID = "5f674a3c25d6d2047656ec8bbd9c0196";
    final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";

    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 101;
    String Location_Provider = LocationManager.GPS_PROVIDER;
    TextView NameofCity, weatherState, Temperature;
    ImageView mweatherIcon;

    LocationManager mLocationManager;
    LocationListener mLocationListener;


    TextView cityField, detailsField, currentTemperatureField, weatherIcon, sub;
    LocationManager locationManager;
    Double lat, longt;
    String country, subLocality;
    Button nextPage, locationFab;
    Typeface weatherFont;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    Context context;
    private ProgressDialog dialog;
    SwipeRefreshLayout refreshLayout;


    //fab actions
    FloatingActionButton settings, call, cancel,  btnLang, diningFab, gasstationFab, hotelFab, fetchWeather, arBtn;
    LinearLayout settingLayout, callLayout, layoutCancel, layoutlocationFab;
    boolean isFABOpen = false;
    View fabBGLayout;

    //bottom sheet
    int decide = 0;

    static final Integer LOCATION = 1;
    static final Integer CALL = 2;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_weather_conditions);

        weatherState = findViewById(R.id.weatherCondition);
        Temperature = findViewById(R.id.temperature);
        mweatherIcon = findViewById(R.id.weatherIcon);
        NameofCity = findViewById(R.id.cityName);

        weatherFont = Typeface.createFromAsset(weatherConditions.this.getAssets(), "fonts/weathericons-regular-webfont.ttf");
        sharedpreferences = getSharedPreferences("locationPref", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        cityField =  findViewById(R.id.city_field);
        detailsField = findViewById(R.id.details_field);
        currentTemperatureField = findViewById(R.id.current_temperature_field);
        weatherIcon = findViewById(R.id.weather_icon);
        sub = findViewById(R.id.sub_field);
        weatherIcon.setTypeface(weatherFont);
        context = weatherConditions.this;
        btnLang = findViewById(R.id.languageButton);
        refreshLayout = findViewById(R.id.refresh);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                decide = 0;
                refreshLayout.setRefreshing(false);
            }
        });


        //fabs
        settings = findViewById(R.id.fabSettings);
        call = findViewById(R.id.fabCall);
        cancel = findViewById(R.id.fabCancel);
        locationFab = findViewById(R.id.fabLocation);
        settingLayout = findViewById(R.id.layoutfabSettings);
        callLayout = findViewById(R.id.layoutfabCall);
        layoutCancel = findViewById(R.id.layoutCancel);
        fabBGLayout = findViewById(R.id.fabBGLayout);
        layoutlocationFab = findViewById(R.id.locationLayout);
        diningFab = findViewById(R.id.dining);
        gasstationFab = findViewById(R.id.gasStation);
        hotelFab = findViewById(R.id.hotel);
        arBtn = findViewById(R.id.ar);

        diningFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(weatherConditions.this, individualType.class).putExtra("type", "restaurant"));
            }
        });


        gasstationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(weatherConditions.this, individualType.class).putExtra("type", "gas+station"));
            }
        });

        hotelFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(weatherConditions.this, individualType.class).putExtra("type", "hotel"));
            }
        });

        btnLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(weatherConditions.this, languageOptions.class));
                setLocale(sharedpreferences.getString("language", "en"));
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showFABMenu();
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFABMenu();
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // to make a phone call
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:112"));

                if (ActivityCompat.checkSelfPermission(weatherConditions.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);


            }
        });

        locationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutocompleteFilter.Builder filterBuilder = new AutocompleteFilter.Builder();
                // filterBuilder.setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS);

                Intent intent = null;
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(filterBuilder.build())
                            .build(weatherConditions.this);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            }
        });


        nextPage = findViewById(R.id.next);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(weatherConditions.this, toVisit.class);
                startActivity(i);
            }
        });

        arBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(weatherConditions.this, LoginActivity.class));
                //setLocale(sharedpreferences.getString("language", "en"));
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(weatherConditions.this,"Location get Successfully", Toast.LENGTH_SHORT).show();
        getWeatherForCurrentLocation();
    }

    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String Latitude = String.valueOf(location.getLatitude());
                String Longitude = String.valueOf(location.getLongitude());
                editor.putString("latitude",Latitude);
                editor.putString("longitude",Longitude);
                editor.commit();
                RequestParams params= new RequestParams();
                params.put("lat", Latitude);
                params.put("lon",Longitude);
                params.put("appid", APP_ID);
                letsdoSomeNetworking(params);
            }


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("A");

            }

            @Override
            public void onProviderEnabled(String provider) {
                System.out.println("B");

            }

            @Override
            public void onProviderDisabled(String provider) {
                //not able to get loc
                System.out.println("C");

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_Provider, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(weatherConditions.this,"Location get Successfully", Toast.LENGTH_SHORT).show();
                switch (requestCode) {
                    //Location
                    case 1:

                        break;
                    //Call
                    case 2:
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel: 112"));
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            startActivity(callIntent);
                        }
                        break;

                }
            }
            else
            {
                //user denied permission
            }
        }

    }
    private void letsdoSomeNetworking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(weatherConditions.this, "Data Get Success", Toast.LENGTH_SHORT).show();
                weatherData weatherD = weatherData.fromJson(response);
                updateUI(weatherD);
                //super.onSuccess(statusCode, headers, response);
            }


            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    private void updateUI(weatherData weather){
        Temperature.setText(weather.getmTemperature());
        NameofCity.setText(weather.getMcity());
        weatherState.setText(weather.getMweatherType());
        int resourceID = getResources().getIdentifier(weather.getMicon(),"drawable", getPackageName());
        mweatherIcon.setImageResource(resourceID);
    }


    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(weatherConditions.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(weatherConditions.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(weatherConditions.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(weatherConditions.this, new String[]{permission}, requestCode);
            }
        } else {

        }
    }

    private void showFABMenu(){
        isFABOpen=true;
        callLayout.setVisibility(View.VISIBLE);
        layoutlocationFab.setVisibility(View.VISIBLE);
        fabBGLayout.setVisibility(View.VISIBLE);

        settings.animate().rotationBy(180);
        settingLayout.setVisibility(View.GONE);
        layoutCancel.setVisibility(View.VISIBLE);

        callLayout.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        layoutlocationFab.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabBGLayout.setVisibility(View.GONE);
        settings.animate().rotationBy(-180);

        layoutCancel.setVisibility(View.GONE);
        settingLayout.setVisibility(View.VISIBLE);

        callLayout.animate().translationY(0);
        layoutlocationFab.animate().translationY(0);

        callLayout.setVisibility(View.GONE);
        layoutlocationFab.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(isFABOpen){
            closeFABMenu();
        }else{
            super.onBackPressed();
        }
    }

    public void onStart() {
        super.onStart();
        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION);
        askForPermission(Manifest.permission.CALL_PHONE,CALL);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.d("Place selected", place.getName().toString());
        LatLng latLng = place.getLatLng();
        lat = latLng.latitude;
        longt = latLng.longitude;
        decide = 1;

    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, weatherConditions.class);
        startActivity(refresh);
        finish();
    }


    @Override
    public void onError(Status status) {}
}