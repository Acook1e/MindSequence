package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton; // 导入 ImageButton 以处理返回按钮
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // 常量：用于 Intent 传递情绪数据
    public static final String EXTRA_MOOD_EMOJI = "com.mindsequence.app.MOOD_EMOJI";
    public static final String EXTRA_MOOD_NAME = "com.mindsequence.app.MOOD_NAME";

    // 页面跳转映射表
    private Map<String, String> pageNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initData();
        setupHeader(); // 设置头部信息，并隐藏返回按钮
        setupViews();
    }

    // 新增/更新方法：设置头部信息
    private void setupHeader() {
        // 1. 设置头部信息
        TextView headerMessage = findViewById(R.id.header).findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("How are you feeling today?");
        }

        // 2. 在主页上隐藏返回按钮
        ImageButton backButton = findViewById(R.id.header).findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.GONE);
        }
    }

    private void initData() {
        pageNameMap.put("mood_feedback", "Mood Feedback");
        pageNameMap.put("anchor_page", "Mind Anchor Page");
        pageNameMap.put("focus_page", "Focus Pomodoro Page");
        pageNameMap.put("ai_page", "AI Companion Page");
        pageNameMap.put("pet_page", "Virtual Pet Page");
        pageNameMap.put("sleep_page", "Sleep Guide Page");
    }

    private void setupViews() {
        // 1. 设置心情选择器 (Mood Selector)
        setupMoodItem(findViewById(R.id.mood_upset), "😞", "Upset", "mood_feedback");
        setupMoodItem(findViewById(R.id.mood_neutral), "😐", "Neutral", "mood_feedback");
        setupMoodItem(findViewById(R.id.mood_content), "🙂", "Content", "mood_feedback");
        setupMoodItem(findViewById(R.id.mood_happy), "😊", "Happy", "mood_feedback");
        setupMoodItem(findViewById(R.id.mood_great), "✨", "Great", "mood_feedback");

        // 2. 焦虑急救包 (Mind Anchor)
        findViewById(R.id.btn_anchor).setOnClickListener(v -> navigateTo("anchor_page"));

        // 3. 功能区 (Features)
        setupFeatureItem(findViewById(R.id.feat_flow), R.drawable.ic_timer, "Mind Flow State", "Focus Pomodoro", "focus_page");
        setupFeatureItem(findViewById(R.id.feat_ai), R.drawable.ic_bot, "AI Partner", "Smart Dialogue", "ai_page");
        setupFeatureItem(findViewById(R.id.feat_pet), R.drawable.ic_paw, "Heart Companion", "Virtual Pet", "pet_page");
        setupFeatureItem(findViewById(R.id.feat_sleep), R.drawable.ic_bed, "Mind Sequence Sleep", "Sleep Guide", "sleep_page");
    }

    private void setupMoodItem(View view, String moodEmoji, String moodText, String targetId) {
        TextView tvEmoji = view.findViewById(R.id.mood_emoji);
        TextView tvText = view.findViewById(R.id.mood_text);
        tvEmoji.setText(moodEmoji);
        tvText.setText(moodText);

        view.setOnClickListener(v -> {
            if ("mood_feedback".equals(targetId)) {
                // 跳转到 MoodFeedbackActivity 并传递数据
                Intent intent = new Intent(this, MoodFeedbackActivity.class);
                intent.putExtra(EXTRA_MOOD_EMOJI, moodEmoji);
                intent.putExtra(EXTRA_MOOD_NAME, moodText);
                startActivity(intent);
            } else {
                navigateTo(targetId);
            }
        });
    }

    private void setupFeatureItem(View view, int iconResId, String title, String desc, String targetId) {
        ImageView ivIcon = view.findViewById(R.id.feat_icon);
        TextView tvTitle = view.findViewById(R.id.feat_title);
        TextView tvDesc = view.findViewById(R.id.feat_desc);

        ivIcon.setImageResource(iconResId);
        tvTitle.setText(title);
        tvDesc.setText(desc);

        view.setOnClickListener(v -> navigateTo(targetId));
    }

    private void navigateTo(String pageId) {
        // 根据 pageId 执行具体的跳转逻辑
        if ("anchor_page".equals(pageId)) {
            // *** 新增：跳转到 Mind Anchor Page ***
            Intent intent = new Intent(this, MindAnchorActivity.class);
            startActivity(intent);
        } else if ("focus_page".equals(pageId)) {
            Intent intent = new Intent(this, FocusPomodoroActivity.class);
            startActivity(intent);
        } else if ("ai_page".equals(pageId)) { // 新增：AI Partner 跳转逻辑
            // 跳转到 AI 聊天页面（AIChatActivity）
            Intent intent = new Intent(this, AIChatActivity.class);
            startActivity(intent);
        }  else if ("sleep_page".equals(pageId)) {
            Intent intent = new Intent(this, SleepGuidanceWelcomeActivity.class);
            startActivity(intent);
        } else {
            // 对于未实现的页面，显示 Toast 提示
            String pageTitle = pageNameMap.get(pageId);
            if (pageTitle == null) pageTitle = "Unknown Page";
            Toast.makeText(this, "Navigating to placeholder: " + pageTitle, Toast.LENGTH_SHORT).show();
        }
    }
}