package com.gochip.odiva;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;

public class InsecureDataStorageIIIActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insecure_data_storage_iiiactivity);
    }

    public void saveCredentials(View view) {
        EditText usr = (EditText) findViewById(R.id.edit_txt_user);
        EditText pwd = (EditText) findViewById(R.id.edit_txt_password);

        File ddir =  new File(getApplicationInfo().dataDir);

        try {
            File uinfo = File.createTempFile("uinfo", "tmp", ddir);
            uinfo.setReadable(true);
            uinfo.setWritable(true);
            FileWriter fw = new FileWriter(uinfo);
            fw.write(usr.getText().toString() + ":" + pwd.getText().toString() + "\n");
            fw.close();
            Toast.makeText(this, "You are login!", Toast.LENGTH_SHORT).show();
            // Now you can read the temporary file where ever the credentials are required.
        }
        catch (Exception e) {
            Toast.makeText(this, "File error occurred", Toast.LENGTH_SHORT).show();
            Log.d("Diva", "File error: " + e.getMessage());
        }
    }
}