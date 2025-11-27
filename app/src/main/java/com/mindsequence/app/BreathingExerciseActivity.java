package com.mindsequence.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BreathingExerciseActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable breathingRunnable;

    private TextView currentPhaseText;
    // 信息框中的控件
    private ImageView stepIcon;
    private TextView stepTitle;
    private TextView stepDescription;
    private TextView stepTimeRemaining;
    private TextView elapsedTimeLabel;

    private Button btnPauseResume;
    private View breathingVisualizer;

    private boolean isPaused = false;
    private int totalDurationSeconds = 180; // 3 minutes
    private int secondsElapsed = 0;

    // 呼吸周期 (4-4-6)
    private static final int INHALE_DURATION = 4;
    private static final int HOLD_DURATION = 4;
    private static final int EXHALE_DURATION = 6;
    private int currentPhaseRemaining = 0;
    private int currentCycleCounter = 0;

    private ObjectAnimator currentScaleXAnimator;
    private ObjectAnimator currentScaleYAnimator;
    private AnimatorSet holdAnimatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_exercise);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setupHeader();
        setupViews();
        startBreathingExercise();
    }

    private void setupHeader() {
        View headerContainer = findViewById(R.id.header);

        TextView headerMessage = headerContainer.findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Deep Breathing Exercise");
        }

        ImageButton backButton = headerContainer.findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupViews() {
        currentPhaseText = findViewById(R.id.current_phase_text);

        // 绑定新的信息框控件
        stepIcon = findViewById(R.id.step_icon);
        stepTitle = findViewById(R.id.step_title);
        stepDescription = findViewById(R.id.step_description);
        stepTimeRemaining = findViewById(R.id.step_time_remaining);
        elapsedTimeLabel = findViewById(R.id.elapsed_time_label);

        btnPauseResume = findViewById(R.id.btn_pause_resume);
        Button btnStopExercise = findViewById(R.id.btn_stop_exercise);
        breathingVisualizer = findViewById(R.id.breathing_visualizer);

        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnStopExercise.setOnClickListener(v -> stopExerciseAndFinish());

        // 初始信息框设置（Ready 状态）
        updateInfoBox("Ready", getPhaseDescription("Ready"), 0, 0);
    }

    // --- 呼吸/计时器核心逻辑 ---

    private void startBreathingExercise() {
        // 第一次运行时，设置初始状态
        currentPhaseText.setText("Inhale");
        currentPhaseRemaining = INHALE_DURATION;
        currentCycleCounter = 0;

        // 初始信息框设置
        updateInfoBox("Inhale", getPhaseDescription("Inhale"), INHALE_DURATION, 0);

        breathingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    handler.postDelayed(this, 100);
                    return;
                }

                // 1. 更新总计时器
                secondsElapsed++;

                // 2. 更新呼吸周期
                currentCycleCounter++;

                String currentPhase = currentPhaseText.getText().toString();
                int currentDuration = getCurrentPhaseDuration(currentPhase);

                // 统一更新所有信息
                updateInfoBox(currentPhase,
                        getPhaseDescription(currentPhase),
                        currentDuration,
                        currentCycleCounter);

                if (currentCycleCounter >= currentPhaseRemaining) {
                    moveToNextPhase();
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(breathingRunnable);
        animateBreathing(INHALE_DURATION, 1.3f, 1.0f, 1.3f);
    }

    private void moveToNextPhase() {
        cancelAllAnimators();
        String currentPhase = currentPhaseText.getText().toString();
        String nextPhase = "";
        int duration = 0;
        float startScale, targetScale = 1.0f;

        switch (currentPhase) {
            case "Inhale":
                nextPhase = "Hold";
                duration = HOLD_DURATION;
                startScale = 1.3f;
                animateHold(duration, startScale);
                break;
            case "Hold":
                nextPhase = "Exhale";
                duration = EXHALE_DURATION;
                startScale = 1.3f;
                targetScale = 0.8f;
                animateBreathing(duration, targetScale, startScale, targetScale);
                break;
            case "Exhale":
                nextPhase = "Inhale";
                duration = INHALE_DURATION;
                startScale = 0.8f;
                targetScale = 1.3f;
                animateBreathing(duration, targetScale, startScale, targetScale);
                break;
        }

        currentPhaseText.setText(nextPhase);
        currentPhaseRemaining = duration;
        currentCycleCounter = 0;

        // 阶段转换时立即更新信息框 (已持续时间为 0)
        updateInfoBox(nextPhase, getPhaseDescription(nextPhase), duration, 0);
    }

    // --- 更新：统一更新信息框的方法 (包含图标) ---
    private void updateInfoBox(String phase, String description, int duration, int elapsedInStep) {
        // 1. 设置图标
        int iconResId = getPhaseIcon(phase);
        if (iconResId != 0) {
            stepIcon.setImageResource(iconResId);
        }

        // 2. 步骤标题和说明
        String title = phase;
        if (duration > 0) {
            title += String.format(" (%ds)", duration);
        }
        stepTitle.setText(title);
        stepDescription.setText(description);

        // 3. 步骤剩余时间
        int stepTimeLeft = duration - elapsedInStep;
        stepTimeRemaining.setText(String.format("%ds", Math.max(0, stepTimeLeft)));

        // 4. 已持续总时间
        int totalMinutes = secondsElapsed / 60;
        int totalSeconds = secondsElapsed % 60;
        elapsedTimeLabel.setText(String.format("%d:%02d", totalMinutes, totalSeconds));

        // 5. 初始 Ready 状态下的特殊处理
        if ("Ready".equals(phase)) {
            stepIcon.setVisibility(View.INVISIBLE);
            stepTimeRemaining.setText("--");
            elapsedTimeLabel.setText("0:00");
        } else {
            stepIcon.setVisibility(View.VISIBLE);
        }
    }

    // --- 新增：获取阶段图标的辅助方法 ---
    private int getPhaseIcon(String phase) {
        switch (phase) {
            case "Inhale":
                return R.drawable.ic_inhale;
            case "Exhale":
                return R.drawable.ic_exhale;
            case "Hold":
                // 使用 ic_square 来表示静态保持
                return R.drawable.ic_square;
            case "Ready":
            default:
                return 0;
        }
    }

    // --- 辅助方法 (保持不变) ---
    private int getCurrentPhaseDuration(String phase) {
        switch (phase) {
            case "Inhale": return INHALE_DURATION;
            case "Hold": return HOLD_DURATION;
            case "Exhale": return EXHALE_DURATION;
            default: return 0;
        }
    }

    private String getPhaseDescription(String phase) {
        switch (phase) {
            case "Inhale": return "深吸一口气，让腹部充分扩张";
            case "Hold": return "屏住呼吸，感受空气充盈全身";
            case "Exhale": return "缓慢地通过嘴巴呼出，放松全身";
            case "Ready": return "点击开始按钮，进行三分钟的放松练习";
            default: return "跟随指引，调整您的呼吸节奏。";
        }
    }


    // --- 动画逻辑 (保持不变) ---
    private void cancelAllAnimators() {
        if (currentScaleXAnimator != null) {
            currentScaleXAnimator.cancel();
        }
        if (currentScaleYAnimator != null) {
            currentScaleYAnimator.cancel();
        }
        if (holdAnimatorSet != null) {
            holdAnimatorSet.cancel();
        }
    }

    private void animateBreathing(int durationSeconds, float targetScale, float startScale, float endScale) {
        if (breathingVisualizer == null) return;

        currentScaleXAnimator = ObjectAnimator.ofFloat(breathingVisualizer, "scaleX", startScale, endScale);
        currentScaleXAnimator.setDuration(durationSeconds * 1000L);
        currentScaleXAnimator.setInterpolator(new LinearInterpolator());

        currentScaleYAnimator = ObjectAnimator.ofFloat(breathingVisualizer, "scaleY", startScale, endScale);
        currentScaleYAnimator.setDuration(durationSeconds * 1000L);
        currentScaleYAnimator.setInterpolator(new LinearInterpolator());

        currentScaleXAnimator.start();
        currentScaleYAnimator.start();
    }

    private void animateHold(int durationSeconds, float currentScale) {
        if (breathingVisualizer == null) return;

        // 小范围的来回脉冲动画
        ObjectAnimator pulseInX = ObjectAnimator.ofFloat(breathingVisualizer, "scaleX", currentScale, currentScale + 0.02f, currentScale);
        ObjectAnimator pulseInY = ObjectAnimator.ofFloat(breathingVisualizer, "scaleY", currentScale, currentScale + 0.02f, currentScale);
        pulseInX.setDuration(1000);
        pulseInY.setDuration(1000);
        pulseInX.setRepeatCount(ValueAnimator.INFINITE);
        pulseInY.setRepeatCount(ValueAnimator.INFINITE);
        pulseInX.setRepeatMode(ValueAnimator.REVERSE);
        pulseInY.setRepeatMode(ValueAnimator.REVERSE);
        pulseInX.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseInY.setInterpolator(new AccelerateDecelerateInterpolator());

        holdAnimatorSet = new AnimatorSet();
        holdAnimatorSet.playTogether(pulseInX, pulseInY);
        holdAnimatorSet.start();

        handler.postDelayed(() -> {
            if (holdAnimatorSet != null) {
                holdAnimatorSet.cancel();
                breathingVisualizer.setScaleX(currentScale);
                breathingVisualizer.setScaleY(currentScale);
            }
        }, durationSeconds * 1000L);
    }

    // --- 控制逻辑 (保持不变) ---
    private void togglePauseResume() {
        isPaused = !isPaused;
        if (isPaused) {
            btnPauseResume.setText("Resume");
            btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play, 0, 0, 0);
            handler.removeCallbacks(breathingRunnable);
            cancelAllAnimators();
            Toast.makeText(this, "Exercise Paused", Toast.LENGTH_SHORT).show();
        } else {
            btnPauseResume.setText("Pause");
            btnPauseResume.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hold, 0, 0, 0);
            handler.post(breathingRunnable);

            // 恢复动画
            String currentPhase = currentPhaseText.getText().toString();
            int remainingDuration = currentPhaseRemaining - currentCycleCounter;
            float startScale = breathingVisualizer.getScaleX();

            switch (currentPhase) {
                case "Inhale":
                    animateBreathing(remainingDuration, 1.3f, startScale, 1.3f);
                    break;
                case "Hold":
                    animateHold(remainingDuration, startScale);
                    break;
                case "Exhale":
                    animateBreathing(remainingDuration, 0.8f, startScale, 0.8f);
                    break;
            }
            Toast.makeText(this, "Exercise Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopExerciseAndFinish() {
        handler.removeCallbacks(breathingRunnable);
        cancelAllAnimators();
        onExerciseFinished();
    }

    private void onExerciseFinished() {
        Toast.makeText(this, "Breathing Exercise Finished!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MindAnchorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(breathingRunnable);
        cancelAllAnimators();
    }
}