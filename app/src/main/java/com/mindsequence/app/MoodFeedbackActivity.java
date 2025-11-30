package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 注意：您需要在 AndroidManifest.xml 中注册此 Activity
public class MoodFeedbackActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable returnRunnable;
    private int countdown = 3;
    private TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_feedback);

        // 隐藏系统 ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupHeader();
        setupContent();
        timerText = findViewById(R.id.timer_text);
        startAutoReturnTimer();
    }

    private void setupHeader() {
        // 设置 Header Message
        TextView headerMessage = findViewById(R.id.header).findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Your emotional state matters");
        }
    }

    private void setupContent() {

        // --- 1. 从 Intent 中获取数据 ---
        String mood = getIntent().getStringExtra(MainActivity.EXTRA_MOOD_NAME);
        String emoji = getIntent().getStringExtra(MainActivity.EXTRA_MOOD_EMOJI);

        // 提供默认值以防万一
        if (mood == null) mood = "Content";
        if (emoji == null) emoji = "🙂";

        // 根据情绪提供不同的反馈信息
        String message;
        if (mood.equals("Upset")) {
            message = "It's okay to feel upset. We have some tools that can help. Take a deep breath.";
        } else if (mood.equals("Happy") || mood.equals("Great")) {
            message = "That's wonderful! We've recorded your positive emotional state. Keep it up.";
        } else {
            message = "Your emotional state has been recorded. Remember to check in with yourself throughout the day.";
        }

        // --- 2. 绑定 UI ---
        ((TextView) findViewById(R.id.mood_emoji)).setText(emoji);
        ((TextView) findViewById(R.id.mood_title)).setText(mood);
        ((TextView) findViewById(R.id.message_box)).setText(message);

        // --- 3. 按钮点击逻辑 (保持不变) ---
        Button btnReturn = findViewById(R.id.btn_return_now);
        Button btnContinue = findViewById(R.id.btn_continue);

        btnReturn.setOnClickListener(v -> navigateToMainPage());
        btnContinue.setOnClickListener(v -> {
            stopAutoReturnTimer();
            Toast.makeText(this, "Continuing Session (e.g., to Breathing Exercise)...", Toast.LENGTH_SHORT).show();
        });
    }

    private void startAutoReturnTimer() {
        countdown = 3; // 重置倒计时
        returnRunnable = new Runnable() {
            @Override
            public void run() {
                if (countdown > 0) {
                    // 更新 UI 上的倒计时
                    timerText.setText(String.format("Returning to main page in %ds...", countdown));
                    countdown--;
                    handler.postDelayed(this, 1000); // 1秒后重复执行
                } else {
                    navigateToMainPage();
                }
            }
        };
        handler.post(returnRunnable);
    }

    private void stopAutoReturnTimer() {
        if (returnRunnable != null) {
            handler.removeCallbacks(returnRunnable);
        }
    }

    private void navigateToMainPage() {
        stopAutoReturnTimer();
        // 实际应用中：
         Intent intent = new Intent(this, MainActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(intent);
         finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 确保在页面不可见时停止计时器，防止内存泄漏
        stopAutoReturnTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoReturnTimer();
    }
}