package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class FocusPomodoroActivity extends AppCompatActivity {

    private static final long FOCUS_DURATION_MILLIS = 25 * 60 * 1000; // 25 minutes
    private long timeLeftInMillis = FOCUS_DURATION_MILLIS;
    private boolean isTimerRunning;
    private CountDownTimer countDownTimer;

    private TextView timerDisplay;
    private ProgressBar timerProgressBar;
    private TextView elapsedTime;
    private TextView remainingTime;
    private TextView progressPercent;
    private Button btnStartFocus;
    private ImageButton btnResetTimer;
    private Button btnEndFocus;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用新创建的布局
        setContentView(R.layout.activity_pomodoro_focus);        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupHeader();
        updateCountDownText();
    }

    private void initViews() {
        // 核心 UI 元素
        timerDisplay = findViewById(R.id.timer_display);
        timerProgressBar = findViewById(R.id.timer_progress_bar);

        // 统计数据显示
        elapsedTime = findViewById(R.id.text_elapsed_time);
        remainingTime = findViewById(R.id.text_remaining_time);
        progressPercent = findViewById(R.id.text_progress_percent);

        // 控制按钮
        btnStartFocus = findViewById(R.id.btn_start_focus);
        btnResetTimer = findViewById(R.id.btn_reset_timer);
        btnEndFocus = findViewById(R.id.btn_end_focus);

        // 设置 ProgressBar 的最大值（以秒为单位，25分钟）
        timerProgressBar.setMax((int) (FOCUS_DURATION_MILLIS / 1000));

        // 设置监听器
        btnStartFocus.setOnClickListener(v -> toggleTimer());
        btnResetTimer.setOnClickListener(v -> resetTimer());
        btnEndFocus.setOnClickListener(v -> endFocusSession());
    }

    private void setupHeader() {
        // 1. 设置头部信息
        TextView headerMessage = findViewById(R.id.header).findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Time to focus and achieve your goals");
        }

        // 2. 设置返回按钮
        btnBack = findViewById(R.id.header).findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setVisibility(View.VISIBLE); // 确保返回按钮可见
            btnBack.setOnClickListener(v -> finish()); // 点击返回按钮关闭当前 Activity
        }
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnStartFocus.setText("Start Focus");
                timerDisplay.setText("00:00");
            }
        }.start();

        isTimerRunning = true;
        btnStartFocus.setText("Pause Focus");
        btnStartFocus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hold, 0, 0, 0); // 假设存在 ic_pause
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        btnStartFocus.setText("Resume Focus");
        btnStartFocus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timeLeftInMillis = FOCUS_DURATION_MILLIS;
        isTimerRunning = false;
        updateCountDownText();
        btnStartFocus.setText("Start Focus");
        btnStartFocus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
        Toast.makeText(this, "Timer reset to 25:00", Toast.LENGTH_SHORT).show();
    }

    private void endFocusSession() {
        if (countDownTimer != null && isTimerRunning) {
            countDownTimer.cancel();
        }

        // 返回主页
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateCountDownText() {
        long totalSeconds = timeLeftInMillis / 1000;
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerDisplay.setText(timeFormatted);

        // 更新 ProgressBar
        int progress = (int) (FOCUS_DURATION_MILLIS / 1000 - totalSeconds);
        timerProgressBar.setProgress(progress);

        // 更新统计数据
        long elapsedSeconds = FOCUS_DURATION_MILLIS / 1000 - totalSeconds;
        String elapsedFormatted = String.format(Locale.getDefault(), "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
        elapsedTime.setText(elapsedFormatted);

        String remainingFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        remainingTime.setText(remainingFormatted);

        int progressPercentValue = (int) (((double) elapsedSeconds / (FOCUS_DURATION_MILLIS / 1000)) * 100);
        progressPercent.setText(String.format(Locale.getDefault(), "%d%%", progressPercentValue));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null && isTimerRunning) {
            countDownTimer.cancel();
        }
    }
}