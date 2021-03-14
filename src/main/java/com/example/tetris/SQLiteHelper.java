package com.example.tetris;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class SQLiteHelper extends SQLiteOpenHelper {


    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "ScoreRecords.db";

    public SQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("SQLHelper Create");
        db.execSQL("CREATE TABLE SCORE_DATABASE" +
                "(_id INTEGER PRIMARY KEY," +
                "NAME TEXT," +
                "SCORE INTEGER)");

        fillTheDbWithPlaceHolderInfo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS SCORE_DATABASE");
        onCreate(db);
    }
    public boolean saveScore(String name, int score, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("NAME", name);
        contentValues.put("SCORE", score);
        db.insert("SCORE_DATABASE", null, contentValues);
        System.out.println("testSQL");
        return true;
    }

    private void fillTheDbWithPlaceHolderInfo(SQLiteDatabase db) {
        saveScore("Mercury", 10, db);
        saveScore("Venus", 1400, db);
        saveScore("Mars", 2600, db);
        saveScore("Jupiter", 4200, db);
        saveScore("Sol_Invictus", 9999, db);
    }
}
