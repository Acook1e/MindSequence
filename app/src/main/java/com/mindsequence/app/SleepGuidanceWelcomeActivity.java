package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SleepGuidanceWelcomeActivity extends AppCompatActivity {

    private Button btnStartSleepGuidance;
    private Button btnBackToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_guidance_welcome);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupHeader();
        setupViews();
    }

    /**
     * 设置通用 Header，包括标题和返回按钮
     */
    private void setupHeader() {
        // 1. 设置头部标题
        View headerView = findViewById(R.id.header);
        TextView headerMessage = headerView.findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Improve your sleep quality");
        }

        // 2. 设置返回按钮
        ImageButton backButton = headerView.findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    /**
     * 初始化布局中的视图元素并设置点击监听器
     */
    private void setupViews() {
        btnStartSleepGuidance = findViewById(R.id.btn_start_sleep_guidance);
        btnBackToMain = findViewById(R.id.btn_back_to_main);

        // 1. "Start Sleep Guidance" 按钮的点击事件
        btnStartSleepGuidance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSleepGuidance();
            }
        });

        // 2. "Back to Main Menu" 按钮的点击事件
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMainPage();
            }
        });
    }

    /**
     * 处理开始睡眠引导的逻辑
     * 这里可以跳转到睡眠引导选择页面
     */
    private void startSleepGuidance() {
        Intent intent = new Intent(this, SleepGuideSelectionActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转回主页 (MainActivity)
     */
    private void navigateToMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}