package com.example.journal;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


import data_classes.Journal;



public class MainActivity extends AppCompatActivity {

    public RecyclerView recyclerView;
    private TextView no_journals_TextView;
    public Button insertJournal;
    public Button setPassword;

    public ArrayList<Journal> journal_list;

    private SharedPreferences sharedPreferences;
    public static final String MyPrefs = "JournalPref";
    public static final String UserKey = "journal_list";

    private int PERMISSION_REQUEST_CODE = 200;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rvJournalList);
        no_journals_TextView = findViewById(R.id.tvNoJournalsText);
        insertJournal = (Button) findViewById(R.id.btnInsertJournal);
        setPassword = (Button) findViewById(R.id.btnSetPasswordFromMain);

        if (!checkPermission()) {
            requestPermission();
        }

        insertJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddJournalPage();
            }
        });

        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPasswordPage();
            }
        });



        if(!check_permission()) {
            request_permission();
            return;
        }

        journal_list = load_list();

        if(journal_list.size() == 0) {
            no_journals_TextView.setVisibility(View.VISIBLE);
        }
        else {
            // recycler view
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new JournalAdapter(journal_list, getApplicationContext()));
        }


    }



    boolean check_permission() {

        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    void request_permission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "READ PERMISSION IS REQUIRED !", Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
        }
    }


    public void save_list(ArrayList<Journal> journal_list) {

        sharedPreferences = this.getSharedPreferences(MyPrefs, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        Collections.sort(journal_list, Comparator.comparing(Journal::getDate));

        String json = gson.toJson(journal_list);
        editor.putString(UserKey, json);
        editor.apply();

        Toast.makeText(this, "Journal Has Added !", Toast.LENGTH_SHORT).show();

    }

    public ArrayList<Journal> load_list() {
        sharedPreferences = getSharedPreferences(MyPrefs, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(UserKey, null);
        Type type = new TypeToken<ArrayList<Journal>>() {}.getType();
        ArrayList<Journal> journals = gson.fromJson(json, type);

        return journals != null ? journals : new ArrayList<Journal>();
    }

    private void openAddJournalPage() {
        Intent intent = new Intent(this, AddJournalActivity.class);
        startActivity(intent);
    }

    private void openPasswordPage() {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
    }


    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denined.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }


}