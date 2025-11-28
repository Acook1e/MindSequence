package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SleepGuideSelectionActivity extends AppCompatActivity {

    // 引导类型常量
    public static final String GUIDE_TYPE_DORM_NOISE = "dorm_noise";
    public static final String GUIDE_TYPE_PRE_EXAM = "pre_exam";
    public static final String GUIDE_TYPE_MIND_WANDERING = "mind_wandering";
    public static final String GUIDE_TYPE_QUICK_WIND_DOWN = "quick_wind_down";

    public static final String EXTRA_GUIDE_TYPE = "guide_type";
    public static final String EXTRA_GUIDE_TITLE = "guide_title";
    public static final String EXTRA_GUIDE_DURATION = "guide_duration";
    public static final String EXTRA_GUIDE_DESCRIPTION = "guide_description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_guide_selection);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupHeader();
        setupViews();
    }

    /**
     * 设置通用 Header
     */
    private void setupHeader() {
        View headerView = findViewById(R.id.header);
        TextView headerMessage = headerView.findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Choose your sleep guide");
        }

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
     * 初始化视图和点击事件
     */
    private void setupViews() {
        // 宿舍噪音适应模式
        setupGuideCard(
                findViewById(R.id.card_dorm_noise),
                GUIDE_TYPE_DORM_NOISE,
                "Dorm Noise Adaptation Mode",
                "20 min"
        );

        // 考前放松引导
        setupGuideCard(
                findViewById(R.id.card_pre_exam),
                GUIDE_TYPE_PRE_EXAM,
                "Pre-Exam Night Relaxation Guide",
                "15 min"
        );

        // 思绪漫游冥想
        setupGuideCard(
                findViewById(R.id.card_mind_wandering),
                GUIDE_TYPE_MIND_WANDERING,
                "Mind Wandering Specific Meditation",
                "30 min"
        );

        // 快速放松
        setupGuideCard(
                findViewById(R.id.card_quick_wind_down),
                GUIDE_TYPE_QUICK_WIND_DOWN,
                "Quick 10-Minute Wind Down",
                "10 min"
        );
    }

    /**
     * 设置引导卡片点击事件
     */
    private void setupGuideCard(View cardView, final String guideType, final String guideTitle, final String guideDuration) {
        // 获取描述文本
        TextView descTextView = cardView.findViewById(getDescriptionId(guideType));
        final String guideDescription = descTextView != null ? descTextView.getText().toString() : "";

        // 整个卡片可点击
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSleepGuide(guideType, guideTitle, guideDuration, guideDescription);
            }
        });

        // 按钮也可点击
        Button startButton = cardView.findViewById(getButtonId(guideType));
        if (startButton != null) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startSleepGuide(guideType, guideTitle, guideDuration, guideDescription);
                }
            });
        }
    }

    private int getButtonId(String guideType) {
        switch (guideType) {
            case GUIDE_TYPE_DORM_NOISE:
                return R.id.btn_dorm_noise;
            case GUIDE_TYPE_PRE_EXAM:
                return R.id.btn_pre_exam;
            case GUIDE_TYPE_MIND_WANDERING:
                return R.id.btn_mind_wandering;
            case GUIDE_TYPE_QUICK_WIND_DOWN:
                return R.id.btn_quick_wind_down;
            default:
                return R.id.btn_dorm_noise;
        }
    }

    /**
     * 根据引导类型获取对应的描述文本ID
     */
    private int getDescriptionId(String guideType) {
        switch (guideType) {
            case GUIDE_TYPE_DORM_NOISE:
                return R.id.desc_dorm_noise;
            case GUIDE_TYPE_PRE_EXAM:
                return R.id.desc_pre_exam;
            case GUIDE_TYPE_MIND_WANDERING:
                return R.id.desc_mind_wandering;
            case GUIDE_TYPE_QUICK_WIND_DOWN:
                return R.id.desc_quick_wind_down;
            default:
                return R.id.desc_dorm_noise;
        }
    }

    /**
     * 开始睡眠引导会话
     */
    private void startSleepGuide(String guideType, String guideTitle, String guideDuration, String guideDescription) {
        Intent intent = new Intent(this, SleepGuidedSessionActivity.class);
        intent.putExtra(EXTRA_GUIDE_TYPE, guideType);
        intent.putExtra(EXTRA_GUIDE_TITLE, guideTitle);
        intent.putExtra(EXTRA_GUIDE_DURATION, guideDuration);
        intent.putExtra(EXTRA_GUIDE_DESCRIPTION, guideDescription);
        startActivity(intent);
    }
}