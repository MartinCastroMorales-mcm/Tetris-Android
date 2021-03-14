package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void onClickStartGame(View view) {
        Intent intentStartGame = new Intent(this, MainActivity.class);
        startActivity(intentStartGame);
    }

    public void onClickSeeHighScores(View view) {
        Intent intentSeeHighScores = new Intent(this, ScoreTableActivity.class);
        startActivity(intentSeeHighScores);

    }
}