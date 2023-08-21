package com.example.tetris;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {  //TODO: how to close mainActivity when the quit is pressed. How to

    TetrisEngine tetrisEngine;
    SQLiteHelper sqlHandler;
    public final int REQUEST_PAUSE_CODE = 01;
    public final int REQUEST_SAVE_SCORE_CODE = 02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUI();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        sqlHandler = new SQLiteHelper(this, "SCORE_DATABASE", null, 0);
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        DisplayMetrics metrics = new DisplayMetrics();
        //Display display = getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int realHeight = metrics.heightPixels;
        int realWidth = metrics.widthPixels;
        //display.getSize(size);
        tetrisEngine = new TetrisEngine(this, realWidth, realHeight, this);
        setContentView(tetrisEngine);



    }
    public void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("resume");
        //FIXME how to automaticaly hide it after swiping up.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        tetrisEngine.resume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tetrisEngine.pause();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SAVE_SCORE_CODE:
                try {
                    int score = data.getIntExtra("score", 0);
                    String name = data.getStringExtra("playerName");
                    sqlHandler.saveScore(name, score, sqlHandler.getWritableDatabase());
                    this.finish();
                } catch (NullPointerException e) {

                    finish();
                }
                break;
            case REQUEST_PAUSE_CODE:
                //From Pause
                switch (data.getStringExtra("key")) {
                    case "Resume":
                        //Do nothing and return to the game.
                        break;
                    case "Quit":
                        //Quit Game and return to the main menu, save score?
                        finish();

                }
        }
    }
}
