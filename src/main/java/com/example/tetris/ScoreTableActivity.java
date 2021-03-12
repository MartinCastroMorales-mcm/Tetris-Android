package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ScoreTableActivity extends AppCompatActivity {


    SQLiteHelper dbHelper;
    String[] arr = {"NAME", "SCORE"};
    int[] arr2 = {R.id.tvBody, R.id.tvPriority};
   // int[] arr3 = {R.id.tvBody};
//TODO: add sorting mechanisms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_table);
        dbHelper = new SQLiteHelper(this, "SCORE_DATABASE", null, 3);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SCORE_DATABASE", null);
        ListView listView = findViewById(R.id.score_table_list);
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.custom_list, cursor, arr, arr2);
       // SimpleCursorAdapter cursorAdapter2 = new SimpleCursorAdapter(this, R.layout.custom_list, cursor, arr, arr3);

        listView.setAdapter(cursorAdapter);
    }
}