package com.example.journal;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Matrix;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;

import data_classes.Journal;

public class JournalActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button backButton;
    private Button editButton;
    private Button reloadImageButton;
    private Button pdfButton;
    private ImageView journalImageView;
    private TextView locationTextView;
    private GoogleMap gMap;
    private EditText dateEditText;
    private EditText headingEditText;
    private EditText contentEditText;
    private EditText moodEditText;
    private SupportMapFragment mapFragment;
    private SharedPreferences sharedPreferences;

    // PDF
    int pdfPageHeight = 1120;
    int pdfPagewidth = 792;
    private static final int PERMISSION_REQUEST_CODE = 200;
    // PDF

    public ArrayList<Journal> all_journals;
    private Journal currentJournal;
    public static int position = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        backButton = (Button) findViewById(R.id.btnBackFromJournal);
        editButton = (Button) findViewById(R.id.btnEditJournal);
        reloadImageButton = (Button) findViewById(R.id.btnEditJournalImage);
        pdfButton = (Button) findViewById(R.id.btnExportPDF);
        journalImageView = (ImageView) findViewById(R.id.ivJournalImage);
        locationTextView = (TextView) findViewById(R.id.tvLocationContext);
        dateEditText = (EditText) findViewById(R.id.etJournalDate);
        headingEditText = (EditText) findViewById(R.id.etJournalHeading);
        contentEditText = (EditText) findViewById(R.id.etJournalContent);
        moodEditText = (EditText) findViewById(R.id.etMood);

        all_journals = load_list();

        if (position != -2) {
            currentJournal = all_journals.get(position);
            setContent(currentJournal);
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        if (!checkPermission()) {
            requestPermission();
        }



        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backMainPage();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit_journal();
            }
        });

        reloadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
            }
        });

        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generatePDF();
            }
        });
    }

    public ArrayList<Journal> load_list() {
        sharedPreferences = getSharedPreferences(MainActivity.MyPrefs, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(MainActivity.UserKey, null);
        Type type = new TypeToken<ArrayList<Journal>>() {}.getType();
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

        Toast.makeText(this, "Journal Has Updated !", Toast.LENGTH_SHORT).show();

    }


    private void backMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void setContent(Journal journal) {
        dateEditText.setText(journal.getDate());
        contentEditText.setText(journal.getMainContent());
        headingEditText.setText(journal.getHeading());
        moodEditText.setText(journal.getMood());

        byte[] imageArray = journal.getImageArray();
        Bitmap image = BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
        journalImageView.setImageBitmap(image);

        Double latitude = journal.getLatitude();
        Double longitude = journal.getLongitude();
        System.out.println(latitude + " ---- " + longitude);
        String location_string = "Latitude = " + latitude + "  Longitude = " + longitude;
        locationTextView.setText(location_string);

    }

    public void edit_journal() {

        currentJournal.setHeading(headingEditText.getText().toString());
        currentJournal.setMainContent(contentEditText.getText().toString());
        currentJournal.setDate(dateEditText.getText().toString());
        currentJournal.setMood(moodEditText.getText().toString());

        Bitmap imageBitmap=((BitmapDrawable)journalImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageArray = stream.toByteArray();

        currentJournal.setImageArray(imageArray);

        all_journals.remove(position);
        all_journals.add(currentJournal);
        save_list(all_journals);

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
                journalImageView.setImageBitmap(selectedImageBitmap);

            }
        }
    });

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        if (currentJournal.getLatitude() != null && currentJournal.getLongitude() != null) {
            LatLng current_lat_lng = new LatLng(currentJournal.getLatitude(), currentJournal.getLongitude());
            gMap.addMarker(new MarkerOptions().position(current_lat_lng).title("Journal Location"));
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_lat_lng, 11));
        }
//        else {
//            mapFragment.getView().setVisibility(View.INVISIBLE);
//            int commit = this.getFragmentManager().beginTransaction().remove(mapFragment).commit();
//        }
    }



    private String getJournalInfo() {
        return "Location => Latitude = " + currentJournal.getLatitude() + " Longitude = " + currentJournal.getLongitude() + System.lineSeparator()
                + "Mood => " + currentJournal.getMood() + System.lineSeparator() + System.lineSeparator()
                + currentJournal.getMainContent();
    }


    private void generatePDF() {
        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        Paint paint = new Paint();
        Paint title = new Paint();
        Paint content = new Paint();

        // we are adding page info to our PDF file
        // in which we will be passing our pageWidth,
        // pageHeight and number of pages and after that
        // we are calling it to create our PDF.
        PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pdfPagewidth, pdfPageHeight, 1).create();

        // below line is used for setting
        // start page for our PDF file.
        PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

        // creating a variable for canvas
        // from our page of PDF.
        Canvas canvas = myPage.getCanvas();


        // below line is used for adding typeface for
        // our text which we will be adding in our PDF file.
        title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        content.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // below line is used for setting text size
        // which we will be displaying in our PDF file.
        title.setTextSize(15);
        content.setTextSize(13);

        // below line is sued for setting color
        // of our text inside our PDF file.
        title.setColor(ContextCompat.getColor(this, R.color.dark_orange));

        // below line is used to draw text in our PDF file.
        // the first parameter is our text, second parameter
        // is position from start, third parameter is position from top
        // and then we are passing our variable of paint which is title.


        canvas.drawText(currentJournal.getHeading(), 210, 80, title);
        canvas.drawText(currentJournal.getDate(), 600, 80, content);


        // below line is used to draw our image on our PDF file.
        // the first parameter of our drawbitmap method is
        // our bitmap
        // second parameter is position from left
        // third parameter is position from top and last
        // one is our variable for paint.

        if (currentJournal.getImageArray() != null) {
            Bitmap imageBitmap = ((BitmapDrawable)journalImageView.getDrawable()).getBitmap();
            Bitmap resized_bitmap = getResizedBitmap(imageBitmap, 300, 200);
            canvas.drawBitmap(resized_bitmap, 200, 150, paint);
        }


        // similarly we are creating another text and in this
        // we are aligning this text to center of our PDF file.
        title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        title.setColor(ContextCompat.getColor(this, R.color.black));
        title.setTextSize(13);

        // below line is used for setting
        // our text to center of PDF.
        title.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Location => Latitude = " + currentJournal.getLatitude() + " Longitude = " + currentJournal.getLongitude(), 50, 400, title);
        canvas.drawText("Mood => " + currentJournal.getMood(), 50, 420, title);
//        System.out.println("Mood => " + currentJournal.getMood());
//        canvas.drawText(currentJournal.getMainContent(), 50, 460, title);


        TextPaint mTextPaint = new TextPaint();
        StaticLayout mTextLayout = new StaticLayout( currentJournal.getMainContent() ,mTextPaint, canvas.getWidth() - 100, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.save();
        int textX = 50;
        int textY = 460;
        canvas.translate(textX, textY);

        mTextLayout.draw(canvas);
        canvas.restore();
        // after adding all attributes to our
        // PDF file we will be finishing our page.
        pdfDocument.finishPage(myPage);

        // below line is used to set the name of
        // our PDF file and its path.
        String pdf_name = currentJournal.getHeading().replaceAll("\\s+","_") + ".pdf";
        File file = new File(Environment.getExternalStorageDirectory(), pdf_name);

        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(file));

            // below line is to print toast message
            // on completion of PDF generation.
            Toast.makeText(JournalActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // below line is used
            // to handle error
            e.printStackTrace();
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close();
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


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
//        bm.recycle();
        return resizedBitmap;
    }



}