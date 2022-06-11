package com.example.journal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import data_classes.Journal;

public class PasswordActivity extends AppCompatActivity {

    private EditText passwordEditText;
    public Button setPassword;
    public Button resetPassword;
    public Button backMainPage;

    public ArrayList<Journal> journal_list;

    private SharedPreferences sharedPreferences;
    public static final String MyPrefs = "JournalPref";
    public static final String UserKey = "journal_list";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        passwordEditText = (EditText) findViewById(R.id.etPassword);
        setPassword = (Button) findViewById(R.id.btnSetPassword);
        resetPassword = (Button) findViewById(R.id.btnResetPassword);
        backMainPage = (Button) findViewById(R.id.btnBackFromPassword);

        journal_list = load_list();


        backMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMainPage();
            }
        });

        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPasswords();
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPasswords();
            }
        });

    }



    private void openMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void save_list(ArrayList<Journal> journal_list) {

        sharedPreferences = this.getSharedPreferences(MyPrefs, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        journal_list.sort(Comparator.comparing(Journal::getDate));

        String json = gson.toJson(journal_list);
        editor.putString(UserKey, json);
        editor.apply();

        Toast.makeText(this, "Passwords Updated !", Toast.LENGTH_SHORT).show();

    }

    public ArrayList<Journal> load_list() {
        sharedPreferences = getSharedPreferences(MyPrefs, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(UserKey, null);
        Type type = new TypeToken<ArrayList<Journal>>() {}.getType();
        ArrayList<Journal> journals = gson.fromJson(json, type);

        return journals != null ? journals : new ArrayList<Journal>();
    }

    private void setPasswords() {
        String password = passwordEditText.getText().toString();
        for (Journal journal : journal_list) {
            journal.setPassword(password);
        }
        save_list(journal_list);
        journal_list = load_list();
    }

    private void resetPasswords() {
        for (Journal journal : journal_list) {
            journal.setPassword(null);
        }
        save_list(journal_list);
        journal_list = load_list();
    }


}