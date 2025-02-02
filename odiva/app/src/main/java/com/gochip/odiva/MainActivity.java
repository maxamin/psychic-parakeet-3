package com.gochip.odiva;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToInsecureDataStorageI(View view) {
        Intent intent = new Intent(this, InsecureDataStorageIActivity.class);
        startActivity(intent);
    }

    public void goToInsecureDataStorageII(View view) {
        Intent intent = new Intent(this, InsecureDataStorageIIActivity.class);
        startActivity(intent);
    }

    public void goToInsecureDataStorageIII(View view) {
        Intent intent = new Intent(this, InsecureDataStorageIIIActivity.class);
        startActivity(intent);
    }

    public void goToInsecureDataStorageIV(View view) {
        Intent intent = new Intent(this, InsecureDataStorageIVActivity.class);
        startActivity(intent);
    }

    public void goToInputValidationIssuesI(View view) {
        Intent intent = new Intent(this, InputValidationIssuesIActivity.class);
        startActivity(intent);
    }

    public void goToInputValidationIssuesII(View view) {
        Intent intent = new Intent(this, AccessControlIssuesIActivity.class);
        startActivity(intent);
    }
}