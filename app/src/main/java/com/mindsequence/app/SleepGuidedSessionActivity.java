package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class SleepGuidedSessionActivity extends AppCompatActivity {

    // UI Components
    private TextView tvSessionTitle;
    private TextView tvSessionDescription;
    private TextView tvTimer;
    private TextView tvSessionDuration;
    private TextView tvProgressText;
    private TextView tvInstruction;
    private ProgressBar progressBar;
    private ImageButton btnPlayPause;
    private Button btnEndSession;

    // Animation Views
    private View circleOuter;
    private View circleMiddle;
    private View circleInner;
    private View circleCenterDot;

    // Animations
    private Animation breathingOuterAnim;
    private Animation breathingMiddleAnim;
    private Animation breathingInnerAnim;
    private Animation pulseDotAnim;

    // Session state
    private boolean isPlaying = false;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 0;
    private long totalTimeInMillis = 0;
    private int progress = 0;

    // Session data from intent
    private String sessionTitle;
    private String sessionDescription;
    private String sessionDuration;
    private String guideType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_guided_session);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get data from intent
        getIntentData();

        setupHeader();
        setupViews();
        initializeAnimations();
        initializeSession();
        setupBackPressedHandler();
    }

    /**
     * 初始化动画
     */
    private void initializeAnimations() {
        // 加载动画资源
        breathingOuterAnim = AnimationUtils.loadAnimation(this, R.anim.anim_breathing_outer);
        breathingMiddleAnim = AnimationUtils.loadAnimation(this, R.anim.anim_breathing_middle);
        breathingInnerAnim = AnimationUtils.loadAnimation(this, R.anim.anim_breathing_inner);
        pulseDotAnim = AnimationUtils.loadAnimation(this, R.anim.anim_pulse_dot);
    }

    /**
     * 开始所有动画
     */
    private void startAllAnimations() {
        circleOuter.startAnimation(breathingOuterAnim);
        circleMiddle.startAnimation(breathingMiddleAnim);
        circleInner.startAnimation(breathingInnerAnim);
        circleCenterDot.startAnimation(pulseDotAnim);

        // 更新指令文本
        tvInstruction.setText("Breathe deeply and relax...");
    }

    /**
     * 停止所有动画
     */
    private void stopAllAnimations() {
        circleOuter.clearAnimation();
        circleMiddle.clearAnimation();
        circleInner.clearAnimation();
        circleCenterDot.clearAnimation();

        // 重置指令文本
        tvInstruction.setText("Press play to begin");
    }

    /**
     * 设置返回按钮处理
     */
    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Show confirmation if session is in progress
                if (isPlaying) {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SleepGuidedSessionActivity.this);
                    builder.setTitle("Leave Session")
                            .setMessage("Your session is in progress. Are you sure you want to leave?")
                            .setPositiveButton("Leave", (dialog, which) -> {
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                stopAllAnimations();
                                finish();
                            })
                            .setNegativeButton("Stay", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                } else {
                    // 如果会话没有在进行，允许返回
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * 获取从选择页面传递的数据
     */
    private void getIntentData() {
        Intent intent = getIntent();
        sessionTitle = intent.getStringExtra(SleepGuideSelectionActivity.EXTRA_GUIDE_TITLE);
        sessionDescription = intent.getStringExtra(SleepGuideSelectionActivity.EXTRA_GUIDE_DESCRIPTION);
        sessionDuration = intent.getStringExtra(SleepGuideSelectionActivity.EXTRA_GUIDE_DURATION);
        guideType = intent.getStringExtra(SleepGuideSelectionActivity.EXTRA_GUIDE_TYPE);

        // Set default values if null
        if (sessionTitle == null) sessionTitle = "Sleep Session";
        if (sessionDescription == null) sessionDescription = "Relax and drift into peaceful sleep";
        if (sessionDuration == null) sessionDuration = "20 min";
        if (guideType == null) guideType = SleepGuideSelectionActivity.GUIDE_TYPE_DORM_NOISE;
    }

    /**
     * 根据引导类型获取对应的头部标题
     */
    private String getHeaderTitleByType(String guideType) {
        switch (guideType) {
            case SleepGuideSelectionActivity.GUIDE_TYPE_DORM_NOISE:
                return "Relax and drift into peaceful sleep";
            case SleepGuideSelectionActivity.GUIDE_TYPE_PRE_EXAM:
                return "Calm your mind for exam success";
            case SleepGuideSelectionActivity.GUIDE_TYPE_MIND_WANDERING:
                return "Find focus in wandering thoughts";
            case SleepGuideSelectionActivity.GUIDE_TYPE_QUICK_WIND_DOWN:
                return "Quickly unwind for better sleep";
            default:
                return "Relax and drift into peaceful sleep";
        }
    }

    /**
     * 设置头部信息
     */
    private void setupHeader() {
        View headerView = findViewById(R.id.header);
        TextView headerMessage = headerView.findViewById(R.id.header_message);
        if (headerMessage != null) {
            // 根据引导类型设置不同的头部标题
            String headerTitle = getHeaderTitleByType(guideType);
            headerMessage.setText(headerTitle);
        }

        ImageButton backButton = headerView.findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 使用新的返回处理逻辑
                    getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
    }

    /**
     * 初始化视图组件
     */
    private void setupViews() {
        tvSessionTitle = findViewById(R.id.tv_session_title);
        tvSessionDescription = findViewById(R.id.tv_session_description);
        tvTimer = findViewById(R.id.tv_timer);
        tvSessionDuration = findViewById(R.id.tv_session_duration);
        tvProgressText = findViewById(R.id.tv_progress_text);
        tvInstruction = findViewById(R.id.tv_instruction);
        progressBar = findViewById(R.id.progress_bar);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnEndSession = findViewById(R.id.btn_end_session);

        // 初始化动画视图
        circleOuter = findViewById(R.id.circle_outer);
        circleMiddle = findViewById(R.id.circle_middle);
        circleInner = findViewById(R.id.circle_inner);
        circleCenterDot = findViewById(R.id.circle_center_dot);

        // Set session data
        tvSessionTitle.setText(sessionTitle);
        tvSessionDescription.setText(sessionDescription);
        tvSessionDuration.setText(sessionDuration + " session");

        // Set up click listeners
        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        btnEndSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endSession();
            }
        });
    }

    /**
     * 初始化会话计时器
     */
    private void initializeSession() {
        // Parse duration from string like "20 min", "15 min", etc.
        int minutes = 20; // default
        try {
            String durationNumber = sessionDuration.replaceAll("[^0-9]", "");
            minutes = Integer.parseInt(durationNumber);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        totalTimeInMillis = minutes * 60 * 1000; // Convert to milliseconds
        timeLeftInMillis = totalTimeInMillis;

        updateTimerDisplay();
        updateProgress();
    }

    /**
     * 切换播放/暂停状态
     */
    private void togglePlayPause() {
        if (isPlaying) {
            pauseSession();
        } else {
            startSession();
        }
    }

    /**
     * 开始会话
     */
    private void startSession() {
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_hold);

        // 开始动画
        startAllAnimations();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerDisplay();
                updateProgress();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                isPlaying = false;
                updateTimerDisplay();
                updateProgress();
                btnPlayPause.setImageResource(R.drawable.ic_play);

                // 停止动画
                stopAllAnimations();

                // Session completed
                showCompletionMessage();
            }
        }.start();
    }

    /**
     * 暂停会话
     */
    private void pauseSession() {
        isPlaying = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);

        // 停止动画
        stopAllAnimations();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     * 更新计时器显示
     */
    private void updateTimerDisplay() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimer.setText(timeFormatted);
    }

    /**
     * 更新进度条和进度文本
     */
    private void updateProgress() {
        if (totalTimeInMillis > 0) {
            progress = (int) ((totalTimeInMillis - timeLeftInMillis) * 100 / totalTimeInMillis);
            progressBar.setProgress(progress);
            tvProgressText.setText(progress + "% complete");
        }
    }

    /**
     * 显示完成消息
     */
    private void showCompletionMessage() {
        // 根据不同的引导类型显示不同的完成消息
        String completionMessage = getCompletionMessageByType(guideType);
        android.widget.Toast.makeText(this, completionMessage, android.widget.Toast.LENGTH_LONG).show();
    }

    /**
     * 根据引导类型获取对应的完成消息
     */
    private String getCompletionMessageByType(String guideType) {
        switch (guideType) {
            case SleepGuideSelectionActivity.GUIDE_TYPE_DORM_NOISE:
                return "Great job! You've learned to embrace ambient sounds for better sleep.";
            case SleepGuideSelectionActivity.GUIDE_TYPE_PRE_EXAM:
                return "Well done! Your mind is now calm and ready for rest before exams.";
            case SleepGuideSelectionActivity.GUIDE_TYPE_MIND_WANDERING:
                return "Excellent! You've found stillness amidst wandering thoughts.";
            case SleepGuideSelectionActivity.GUIDE_TYPE_QUICK_WIND_DOWN:
                return "Perfect! You're now relaxed and ready for deep sleep.";
            default:
                return "Session completed! Great job!";
        }
    }

    /**
     * 结束会话
     */
    private void endSession() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // 停止动画
        stopAllAnimations();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        // 清理动画资源
        stopAllAnimations();
    }
}