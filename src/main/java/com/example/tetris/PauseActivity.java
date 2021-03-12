package com.example.tetris;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PauseActivity extends AppCompatActivity {
    Intent resultIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause);
        int score = getIntent().getIntExtra("score", 0);
        TextView textView = findViewById(R.id.pause_score);
        textView.setText("Score: " + score);
    }

    public void onClickResume(View view) {
        resultIntent = getIntent();
        resultIntent.putExtra("key", "Resume");
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public void onClickQuit(View view) {
        resultIntent = getIntent();
        resultIntent.putExtra("key", "Quit");
        setResult(RESULT_OK, resultIntent);
        finish();
    }


}