package com.example.tetris;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SaveScoreActivity extends AppCompatActivity {
    //Views
    EditText editText;
    TextView scoreView;
    TextView tetrosPlacedView;
    TextView deletedRowsView;

    String playerName;
    int score;
    int tetrosPlaced;
    int deletedRows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_score);

        score = getIntent().getIntExtra("score", 0);
        tetrosPlaced = getIntent().getIntExtra("tetrosPlaced", 0);
        deletedRows = getIntent().getIntExtra("deletedRows", 0);

        editText = findViewById(R.id.editText);
        scoreView = findViewById(R.id.score_view);
        tetrosPlacedView = findViewById(R.id.tetros_placed);
        deletedRowsView = findViewById(R.id.deleted_rows);

        scoreView.setText("Score: " + score);
        tetrosPlacedView.setText("TetrosPlaced: " + tetrosPlaced);
        deletedRowsView.setText("DeletedRows" + deletedRows);



    }

    public void onClickDone(View view) {
        Intent resultIntent = getIntent();
        playerName = editText.getText().toString();
        resultIntent.putExtra("playerName", playerName);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}