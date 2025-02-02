package com.gochip.odiva;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class APICredsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apicreds);

        TextView apicview = (TextView) findViewById(R.id.apicTextView);
        // Connect to vendor cloud
        // Get API credentials and other confidential details of the user
        String apidetails = "API Key: 123secretapikey123\nAPI User name: diva\nAPI Password: p@ssword";

        // Display the details on the app
        apicview.setText(apidetails);
    }
}