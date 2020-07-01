package com.example.parseproject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.parseproject.databinding.ActivityRiderBinding;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    ActivityRiderBinding riderBinding;
    Boolean requestActive = false;
    Handler handler = new Handler();
    Boolean driverActive = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_rider);
        riderBinding = DataBindingUtil.setContentView(this,R.layout.activity_rider);
        riderBinding.callUber.setOnClickListener(this);
        riderBinding.logout.setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ParseQuery<ParseObject>  query = new ParseQuery<ParseObject>("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() >0){
                        requestActive = true;
                        riderBinding.callUber.setText("Cancel Uber");
                        checkForUpdate();
                    }
                }
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
       locationListener = new LocationListener() {
           @Override
           public void onLocationChanged(Location location) {

               updateMap(location);
          //     Toast.makeText(RiderActivity.this, String.valueOf(location.getLatitude()), Toast.LENGTH_SHORT).show();


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

       if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
           locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0 , 0, locationListener);

       }
       else{
           if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
           }else{
               locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
               Location lastKnowLocation =  locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

               if(lastKnowLocation !=null){
                   updateMap(lastKnowLocation);
               }
           }
       }
    }

    private void updateMap(Location location){

        if(driverActive !=false){
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
            mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
                    Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    updateMap(lastKnowLocation);
                }
            }
        }
    }

    public void callUber(){

        if(requestActive){

            ParseQuery<ParseObject>  query = new ParseQuery<ParseObject>("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() >0){

                            for(ParseObject object:objects){
                                object.deleteInBackground();
                            }

                            requestActive = false;
                            riderBinding.callUber.setText("Call Uber");
                        }
                    }
                }
            });

        }else{

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
                Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(lastKnowLocation!=null){
                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());

                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
                    request.put("location",parseGeoPoint);
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            if(e==null){
                                riderBinding.callUber.setText("Cancel Uber");
                                requestActive = true;

                                 checkForUpdate();
                            }
                        }
                    });

                }else{
                    Toast.makeText(this,"Could not find location. Please try again later.",Toast.LENGTH_LONG).show();
                }
            }

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.callUber:
                callUber();
                break;

            case R.id.logout:
                logout();
                break;
        }
    }

    private void logout(){
        ParseUser.logOut();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
    }


    public void checkForUpdate(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

        query.whereExists("driverUsername");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(e == null && objects.size() > 0){
                    driverActive = true;

                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username",objects.get(0).get("driverUsername"));

                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {

                            if(e == null && objects.size()>0){

                                ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("position");

                                if(Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                                    Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                                    if(lastKnowLocation !=null){

                                        ParseGeoPoint userLocation = new ParseGeoPoint(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());

                                        Double distanceInMiles = driverLocation.distanceInMilesTo(userLocation);

                                        Double distanceOneDP = (double) Math.round(distanceInMiles * 10) / 10;

                                       if(distanceInMiles < 0.01){

                                           riderBinding.infoTextView.setText("Your driver is here!");

                                           ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
                                           query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());

                                           query.findInBackground(new FindCallback<ParseObject>() {
                                               @Override
                                               public void done(List<ParseObject> objects, ParseException e) {
                                                 if(e == null){
                                                     for (ParseObject object:objects){
                                                         object.deleteInBackground();
                                                     }
                                                 }
                                               }
                                           });
                                           handler.postDelayed(new Runnable() {
                                               @Override
                                               public void run() {
                                                   riderBinding.infoTextView.setText("");
                                                  riderBinding.callUber.setVisibility(View.VISIBLE);
                                                  riderBinding.callUber.setText("Call An Uber");
                                                  requestActive = false;
                                                  driverActive = false;

                                               }
                                           },5000);

                                       }else{

                                           LatLng driverLocationLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                           LatLng requestLocationLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                                           ArrayList<Marker> markers =  new ArrayList<>();

                                                  mMap.clear();
                                           markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLng).title("Driver location")));
                                           markers.add(mMap.addMarker(new MarkerOptions().position(requestLocationLatLng).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));


                                           LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                           for(Marker marker:markers){
                                               builder.include(marker.getPosition());
                                           }

                                           LatLngBounds bounds =  builder.build();

                                           int padding = 60; //offset from edges of the map in pixels
                                           CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,padding);

                                           mMap.animateCamera(cu);
                                           riderBinding.callUber.setVisibility(View.INVISIBLE);
                                           riderBinding.infoTextView.setText("Your driver is"+distanceOneDP.toString() +" miles way");
                                           handler.postDelayed(new Runnable() {
                                               @Override
                                               public void run() {
                                                   checkForUpdate();
                                               }
                                           },2000);

                                       }

                                      // riderBinding.infoTextView.setText("Your driver is miles way");



                                    }

                                }

                            }

                        }
                    });

                }


            }
        });
    }
}
