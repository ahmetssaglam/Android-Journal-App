package com.example.journal;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import data_classes.Journal;

public class AddJournalActivity extends AppCompatActivity implements LocationListener {

    private Button addBackButton;
    private Button addButton;
    private Button loadImageButton;
    private ImageView addJournalImageView;
    private EditText addDateEditText;
    private EditText addHeadingEditText;
    private EditText addContentEditText;
    private EditText addMoodEditText;
    private SharedPreferences sharedPreferences;

    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    public Double latitude = null;
    public Double longitude = null;

    public static final int SELECT_PICTURE = 200;
    public byte[] imageArray;
    public ArrayList<Journal> all_journals;
    public static int position = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal);

        addBackButton = (Button) findViewById(R.id.btnBackFromAddJournal);
        addButton = (Button) findViewById(R.id.btnAddJournal);
        loadImageButton = (Button) findViewById(R.id.btnAddJournalImage);
        addJournalImageView = (ImageView) findViewById(R.id.ivAddJournalImage);
        addDateEditText = (EditText) findViewById(R.id.etAddJournalDate);
        addHeadingEditText = (EditText) findViewById(R.id.etAddJournalHeading);
        addContentEditText = (EditText) findViewById(R.id.etAddJournalContent);
        addMoodEditText = (EditText) findViewById(R.id.etAddMood);

        all_journals = load_list();
        addDateEditText.setText(get_current_date());
        getLocation();


        addBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backMainPage();
            }
        });

        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Journal addingJournal = create_journal();
                all_journals.add(addingJournal);
                save_list(all_journals);
            }
        });
    }


    public Journal create_journal() {
        Journal journal = new Journal();

        getLocation();
        while (latitude == null || longitude == null) {
            getLocation();
        }
        journal.setLatitude(this.latitude);
        journal.setLongitude(this.longitude);

        journal.setHeading(addHeadingEditText.getText().toString());
        journal.setMainContent(addContentEditText.getText().toString());
        journal.setDate(addDateEditText.getText().toString());
        journal.setMood(addMoodEditText.getText().toString());

        journal.setImageArray(imageArray);
        journal.setPassword(null);

        return journal;
    }

    public ArrayList<Journal> load_list() {
        sharedPreferences = getSharedPreferences(MainActivity.MyPrefs, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MainActivity.UserKey, null);
        Type type = new TypeToken<ArrayList<Journal>>() {
        }.getType();
        ArrayList<Journal> journals = gson.fromJson(json, type);

        return journals != null ? journals : new ArrayList<Journal>();
    }


    public void save_list(ArrayList<Journal> journal_list) {

        sharedPreferences = this.getSharedPreferences(MainActivity.MyPrefs, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        journal_list.sort(Comparator.comparing(Journal::getDate));

        String json = gson.toJson(journal_list);
        editor.putString(MainActivity.UserKey, json);
        editor.apply();

        Toast.makeText(this, "Journal Has Added !!", Toast.LENGTH_SHORT).show();

    }


    void imageChooser() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        getImageActivity.launch(i);
    }


    ActivityResultLauncher<Intent> getImageActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == AddJournalActivity.RESULT_OK) {
            Intent data = result.getData();
            // do your operation from here....
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                Bitmap selectedImageBitmap = null;
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                addJournalImageView.setImageBitmap(selectedImageBitmap);

                // to save image in java object
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                imageArray = stream.toByteArray();
//                        selectedImageBitmap.recycle();
            }
        }
    });


    private void backMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public static String get_current_date() {

        LocalDate current_date = java.time.LocalDate.now();

        @SuppressLint("SimpleDateFormat")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date_string = current_date.format(formatter);
        System.out.println("String Data ==> " + date_string);

        return date_string;
    }

    public static boolean isLocationEnabled(Context context) {
        //...............
        return true;
    }

    protected void getLocation() {
        if (isLocationEnabled(AddJournalActivity.this)) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
//            Location l = locationManager.getLastKnownLocation(bestProvider);
//            (new Handler()).postDelayed(this::bekle, 5000);

            Location location = locationManager.getLastKnownLocation(bestProvider);

            if (location != null) {
                Log.e("TAG", "GPS is on");
                this.latitude = location.getLatitude();
                this.longitude = location.getLongitude();
                Toast.makeText(AddJournalActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
            }
            else{
                //This is what you need:
                locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }
        else
        {
            Log.e("LOCATION", "GIVE PERMISSON FOR LOCATION");
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //Hey, a non null location! Sweet!

        //remove location callback:
        locationManager.removeUpdates(this);

        //open the map:
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Toast.makeText(AddJournalActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void bekle() {
        System.out.println("wait");
    }


}