package com.example.parseproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.parseproject.databinding.ActivityViewRequestBinding;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestActivity extends AppCompatActivity {
    ActivityViewRequestBinding requestBinding;
    ArrayList<String> requests = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    LocationListener locationListener;

    ArrayList<Double> requestLatitudes = new ArrayList<Double>();
    ArrayList<Double> requestLongitudes = new ArrayList<Double>();
    ArrayList<String> usernames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_request);
        setTitle("Nearby Request");

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);

        requests.clear();
        requests.add("Getting nearby requests...");
        requestBinding.listView.setAdapter(arrayAdapter);

        requestBinding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(ViewRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    if (requestLatitudes.size() > i && requestLongitudes.size() > i && usernames.size() >i && lastKnowLocation != null) {
                        Intent intent = new Intent(ViewRequestActivity.this, DriverLocationActivity.class);
                        intent.putExtra("requestLatitude",requestLatitudes.get(i));
                        intent.putExtra("requestLongitude",requestLongitudes.get(i));
                        intent.putExtra("driverLatitude",lastKnowLocation.getLatitude());
                        intent.putExtra("driverLongitude", lastKnowLocation.getLongitude());
                        intent.putExtra("username",usernames.get(i));

                        startActivity(intent);
                    }

                }


            }
        });


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateListView(location);
                //     Toast.makeText(RiderActivity.this, String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT).show();

                ParseUser.getCurrentUser().put("position", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));

                ParseUser.getCurrentUser().saveInBackground();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (lastKnowLocation != null) {
                    updateListView(lastKnowLocation);
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateListView(lastKnowLocation);
                }
            }
        }
    }

    public void updateListView(Location location) {

        if (location != null) {


            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            final ParseGeoPoint geoPointLoation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            query.whereNear("location", geoPointLoation);

            query.whereDoesNotExist("driverUsername");

            query.setLimit(10);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {

                        requests.clear();
                        requestLatitudes.clear();
                        requestLongitudes.clear();
                        usernames.clear();

                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {

                                ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");

                                if (requestLocation != null) {
                                    Double distanceInMiles = geoPointLoation.distanceInMilesTo(requestLocation);

                                    Double distanceOneDP = (double) Math.round(distanceInMiles * 10) / 10;

                                    requests.add(distanceOneDP.toString() + " miles");

                                    requestLatitudes.add(requestLocation.getLatitude());
                                    requestLongitudes.add(requestLocation.getLongitude());
                                    usernames.add(object.getString("username"));
                                }


                            }


                        }
                    } else {
                        requests.add("No active request nearby");
                    }

                    arrayAdapter.notifyDataSetChanged();
                }
            });
        }

    }
}
