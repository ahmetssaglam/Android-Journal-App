package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import data_classes.Journal;

public class UnlockActivity extends AppCompatActivity {

    private EditText getPassword;
    public Button unlockPassword;
    public Button backMainPage;

    public static String current_password;
    public boolean control;
    public static int pos;

//    private SharedPreferences sharedPreferences;
//    public static final String MyPrefs = "JournalPref";
//    public static final String UserKey = "journal_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        getPassword = (EditText) findViewById(R.id.etUnlockPassword);
        unlockPassword = (Button) findViewById(R.id.btnUnlock);
        backMainPage = (Button) findViewById(R.id.btnBackFromUnlock);

        backMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainPage();
            }
        });

        unlockPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                control = controlPassword();
                if (control) {
                    openJournalPage();
                }
                else {
                    toastMsg();
                }
            }
        });

    }


    private void openMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void openJournalPage() {
        JournalActivity.position = pos;
        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
    }

    private boolean controlPassword() {
        String editTextPassword = getPassword.getText().toString();

        return editTextPassword.equals(current_password);

    }

    public void toastMsg() {
        Toast toast = Toast.makeText(this, "Wrong Password", Toast.LENGTH_LONG);
        toast.show();
    }

}