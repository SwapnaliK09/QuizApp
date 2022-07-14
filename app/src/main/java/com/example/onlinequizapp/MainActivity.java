package com.example.onlinequizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView quizTimer;
    private RelativeLayout option1Layout, option2Layout, option3Layout, option4Layout;
    private TextView option1Tv, option2Tv, option3Tv, option4Tv;
    private ImageView option1Icon, option2Icon, option3Icon, option4Icon;
    private TextView totalQuestionTv;
    private TextView questionTv;
    private TextView currentQuestionTv;
    private final List<QuestionList> questionLists = new ArrayList<>();

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://onlinequizapp-8969c-default-rtdb.firebaseio.com/");
    private CountDownTimer countDownTimer;
    private int currentQuestionPosition = 0;
    private int selectedOption = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quizTimer = findViewById(R.id.quizTimer);
        option1Layout = findViewById(R.id.option1Layout);
        option2Layout = findViewById(R.id.option2Layout);
        option3Layout = findViewById(R.id.option3Layout);
        option4Layout = findViewById(R.id.option4Layout);

        option1Tv = findViewById(R.id.option1Tv);
        option2Tv = findViewById(R.id.option2Tv);
        option3Tv = findViewById(R.id.option3Tv);
        option4Tv = findViewById(R.id.option4Tv);

        option1Icon = findViewById(R.id.option1);
        option2Icon = findViewById(R.id.option2);
        option3Icon = findViewById(R.id.option3);
        option4Icon = findViewById(R.id.option4);

        questionTv = findViewById(R.id.questionTv);
        totalQuestionTv = findViewById(R.id.totalQuestions);
        currentQuestionTv = findViewById(R.id.currentQuestion);

        final AppCompatButton nextBtn = findViewById(R.id.nextQuestionBtn);

        InstructionsDialog instructionsDialog = new InstructionsDialog(MainActivity.this);
        instructionsDialog.setCancelable(false);
        instructionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        instructionsDialog.show();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              final int getQuizTime = Integer.parseInt(snapshot.child("timer").getValue(String.class));
                System.out.println("<<<<<<<<<<<"+getQuizTime);
                for (DataSnapshot question : snapshot.child("questions").getChildren()) {

                    String getQuestion = question.child("question").getValue(String.class);
                    String getOption1 = question.child("option1").getValue(String.class);
                    String getOption2 = question.child("option2").getValue(String.class);
                    String getOption3 = question.child("option3").getValue(String.class);
                    String getOption4 = question.child("option4").getValue(String.class);
                    int getAnswer = Integer.parseInt(question.child("answer").getValue(String.class));

                    QuestionList questionList = new QuestionList(getQuestion, getOption1, getOption2, getOption3, getOption4, getAnswer);
                    questionLists.add(questionList);
                    totalQuestionTv.setText("/" + questionLists.size());
                    startQuizTimer(getQuizTime);
                    selectQuestion(currentQuestionPosition);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "failed to get data from firebase.", Toast.LENGTH_SHORT).show();

            }
        });

        option1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = 1;
                selectOptions(option1Layout,option1Icon);

            }
        });

        option2Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption =2;
                selectOptions(option2Layout,option2Icon);
            }
        });
        option3Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = 3;
                selectOptions(option3Layout,option3Icon);

            }
        });
        option4Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption= 4;
                selectOptions(option4Layout,option4Icon);

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedOption != 0){
                    questionLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOption);
                    selectedOption = 0;
                    currentQuestionPosition++;

                    if (currentQuestionPosition < questionLists.size()){
                        selectQuestion(currentQuestionPosition);
                    }
                    else {
                        countDownTimer.cancel();
                        finishQuiz();
                    }

                }
                else {
                    Toast.makeText(MainActivity.this, "Please select an option.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void finishQuiz() {
        Intent intent = new Intent(MainActivity.this, QuizResult.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("questions", (Serializable) questionLists);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void startQuizTimer(int maxTimeInSeconds) {
        countDownTimer = new CountDownTimer(maxTimeInSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUnitFinished) {
                long getHour = TimeUnit.MILLISECONDS.toHours(millisUnitFinished);
                long getMinute = TimeUnit.MILLISECONDS.toMinutes(millisUnitFinished);
                long getSecond = TimeUnit.MILLISECONDS.toSeconds(millisUnitFinished);

                String generateTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", getHour,
                        getMinute - TimeUnit.HOURS.toMinutes(getHour),
                        getSecond - TimeUnit.MINUTES.toSeconds(getMinute));
                quizTimer.setText(generateTime);
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }
        };
        countDownTimer.start();
    }

    private void selectQuestion(int questionListPosition){
        resetOptions();
        questionTv.setText(questionLists.get(questionListPosition).getQuestion());
        option1Tv.setText(questionLists.get(questionListPosition).getOption1());
        option2Tv.setText(questionLists.get(questionListPosition).getOption2());
        option3Tv.setText(questionLists.get(questionListPosition).getOption3());
        option4Tv.setText(questionLists.get(questionListPosition).getOption4());

        currentQuestionTv.setText("Question"+(questionListPosition+1));
    }

    private void resetOptions(){
        option1Layout.setBackgroundResource(R.drawable.round_back_white);
        option2Layout.setBackgroundResource(R.drawable.round_back_white);
        option3Layout.setBackgroundResource(R.drawable.round_back_white);
        option4Layout.setBackgroundResource(R.drawable.round_back_white);

        option1Icon.setImageResource(R.drawable.rounde_back_white_50_100);
        option2Icon.setImageResource(R.drawable.rounde_back_white_50_100);
        option3Icon.setImageResource(R.drawable.rounde_back_white_50_100);
        option4Icon.setImageResource(R.drawable.rounde_back_white_50_100);
    }

    private void  selectOptions(RelativeLayout selectedOptionLayout, ImageView selectedOptionIcon){
        resetOptions();
        selectedOptionIcon.setImageResource(R.drawable.ic_check);
        selectedOptionLayout.setBackgroundResource(R.drawable.round_background_selected_option);

    }
}