package com.example.onlinequizapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class QuizResult extends AppCompatActivity {

    private  List<QuestionList> questionListList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        final TextView score = findViewById(R.id.scoreTV);
        final TextView totalScoreTV = findViewById(R.id.totalScoreTV);
        final TextView correctTV = findViewById(R.id.correctTV);
        final TextView incorrectTV = findViewById(R.id.incorrectTV);

        final AppCompatButton shareBtn = findViewById(R.id.shareBtn);
        final AppCompatButton reTakeBtn = findViewById(R.id.reTakeBtn);

        questionListList =(List<QuestionList>) getIntent().getSerializableExtra("questions");
        totalScoreTV.setText("/"+questionListList.size());
        score.setText(getCorrectAnswer()+" ");
        correctTV.setText(getCorrectAnswer()+"");
        incorrectTV.setText(String.valueOf(questionListList.size()-getCorrectAnswer()));

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,"My Score = "+score.getText());


                Intent shareIntent = Intent.createChooser(sendIntent,"Share Via");
                startActivity(shareIntent);

            }
        });

        reTakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            startActivity(new Intent(QuizResult.this,MainActivity.class));
            }
        });
    }

    private  int getCorrectAnswer(){
        int correctAnswer =0;
        for (int i = 0;i < questionListList.size();i++){
            int getUserSelectedOption = questionListList.get(i).getUserSelectedAnswer();
            int getQuestionAnswer = questionListList.get(i).getAnswer();
            if (getQuestionAnswer == getUserSelectedOption){
                correctAnswer++;
            }
        }
        return correctAnswer;
    }
}