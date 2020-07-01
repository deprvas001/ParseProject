package com.example.parseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.parseproject.databinding.ActivityMainBinding;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
ActivityMainBinding mainBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        getSupportActionBar().hide();
        mainBinding.login.setOnClickListener(this);

        if(ParseUser.getCurrentUser() == null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){
                        Log.i("Info", "Anonymous Login Successful");
                    }else{
                        Log.i("Info", "Anonymous Login Failed");
                    }
                }
            });
        }else{
            if(ParseUser.getCurrentUser().get("riderOrDriver") !=null){
                Log.i("Info","Redirecting as " + ParseUser.getCurrentUser().get("riderOrDriver"));
                redirectActivity();
            }
        }
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public void getStarted(){
        Log.i("Switch Value", String .valueOf(mainBinding.switchButton.isChecked()));

        String userType= "rider";
        if(mainBinding.switchButton.isChecked()){
            userType = "driver";
        }

        ParseUser.getCurrentUser().put("riderOrDriver",userType);

        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                redirectActivity();
            }
        });
        Log.i("Info", "Redirecting as "+userType);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                getStarted();
                break;

        }
    }

    public void redirectActivity(){
        if(ParseUser.getCurrentUser().get("riderOrDriver").equals("rider")){
            Intent intent = new Intent(MainActivity.this,RiderActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(MainActivity.this,ViewRequestActivity.class);
            startActivity(intent);
        }
    }
}
