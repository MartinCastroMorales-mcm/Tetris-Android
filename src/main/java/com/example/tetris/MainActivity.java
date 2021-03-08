 package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

 public class MainActivity extends AppCompatActivity {

    TetrisEngine tetrisEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        DisplayMetrics metrics = new DisplayMetrics();
        //Display display = getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        int realWidth = metrics.widthPixels;
        Point size = new Point();
        //display.getSize(size);
        tetrisEngine = new TetrisEngine(this, realWidth, realHeight);
        setContentView(tetrisEngine);
    }

     @Override
     protected void onResume() {
         super.onResume();
         System.out.println("resume");
         getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
         tetrisEngine.resume();
     }

     @Override
     public void onWindowFocusChanged(boolean hasFocus) {
         super.onWindowFocusChanged(hasFocus);
        // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
     }

     @Override
     protected void onPause() {
         super.onPause();
         tetrisEngine.pause();

     }
 }