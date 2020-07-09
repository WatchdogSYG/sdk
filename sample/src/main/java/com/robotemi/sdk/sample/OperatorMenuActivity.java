package com.robotemi.sdk.sample;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class OperatorMenuActivity extends AppCompatActivity {
    Activity a;

    public OperatorMenuActivity() {

    }

    public void getMainReference(Activity a) {
        this.a = a;
        System.out.println(a.toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operatormenu);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
