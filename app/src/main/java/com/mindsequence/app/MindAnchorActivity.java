package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// 注意：您需要在 AndroidManifest.xml 中注册此 Activity
public class MindAnchorActivity extends AppCompatActivity {

    private Button btnStartBreathing;
    private Button btnBackToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局文件，对应 activity_mind_anchor.xml
        setContentView(R.layout.activity_mind_anchor);

        // 隐藏系统 ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupHeader();
        setupViews();
    }

    /**
     * 设置通用 Header，包括标题和返回按钮。
     */
    private void setupHeader() {
        // 1. 设置头部标题
        View headerView = findViewById(R.id.header);
        TextView headerMessage = headerView.findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Take a moment to breathe"); // 设置页面标题
        }

        // 2. 设置返回按钮
        ImageButton backButton = headerView.findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE); // 显示返回按钮
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed(); // 点击返回按钮，执行返回操作
                }
            });
        }
    }

    /**
     * 初始化布局中的视图元素并设置点击监听器。
     */
    private void setupViews() {
        btnStartBreathing = findViewById(R.id.btn_start_breathing);
        btnBackToMain = findViewById(R.id.btn_back_to_main);

        // 1. “Start Deep Breathing Exercise” 按钮的点击事件
        btnStartBreathing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBreathingExercise();
            }
        });

        // 2. “Back to Main” 按钮的点击事件
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 直接返回到主页 (MainActivity)
                navigateToMainPage();
            }
        });
    }

    /**
     * 处理开始深呼吸练习的逻辑。
     * 实际应用中，这里应该跳转到一个新的 BreathingActivity 页面。
     */
    private void startBreathingExercise() {
        Intent intent = new Intent(this, BreathingExerciseActivity.class);
        startActivity(intent);
    }

    /**
     * 跳转回主页 (MainActivity)。
     */
    private void navigateToMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        // 清除当前栈顶上的所有 Activity，确保返回到主页后，按返回键会退出应用
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}