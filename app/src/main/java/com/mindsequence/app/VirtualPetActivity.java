package com.mindsequence.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import java.util.Random;

public class VirtualPetActivity extends AppCompatActivity {

    private ImageView petImage;
    private FrameLayout effectContainer;
    private TextView petSpeech;
    private View speechBubble;
    private AppCompatButton btnInteract;
    private AnimatorSet currentAnimation;
    private Random random = new Random();
    private Handler speechHandler = new Handler();

    // 宠物对话内容
    private final String[] petDialogs = {
            "Hello! I'm Misty, always here to listen.",
            "Thanks for spending time with me!",
            "You're doing great today!",
            "I love when you visit me!",
            "Take a deep breath, everything will be okay.",
            "Your presence makes me happy!",
            "Let's enjoy this peaceful moment together."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_pet);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupHeader();
        setupAnimations();
    }

    private void initViews() {
        petImage = findViewById(R.id.pet_image);
        effectContainer = findViewById(R.id.effect_container);
        petSpeech = findViewById(R.id.pet_speech);
        speechBubble = findViewById(R.id.speech_bubble);
        btnInteract = findViewById(R.id.btn_interact);

        AppCompatButton btnBack = findViewById(R.id.btn_back_main);

        // 设置互动按钮点击事件
        btnInteract.setOnClickListener(v -> {
            // 在宠物中心位置创建气泡
            createClickBubble(petImage.getWidth() / 2f, petImage.getHeight() / 2f);
            startPetInteraction();
        });

        // 设置返回按钮
        btnBack.setOnClickListener(v -> onBackPressed());

        // 设置宠物图片触摸事件（支持点击位置相关的气泡）
        petImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 在点击位置创建气泡
                    createClickBubble(event.getX(), event.getY());
                    startPetInteraction();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupHeader() {
        View headerView = findViewById(R.id.header);
        TextView headerMessage = headerView.findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("Meet your virtual companion");
        }

        ImageView backButton = headerView.findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    /**
     * 在指定位置创建点击气泡效果
     */
    private void createClickBubble(float x, float y) {
        // 创建气泡View
        View bubble = new View(this);
        bubble.setBackground(ContextCompat.getDrawable(this, R.drawable.bubble_effect));

        // 设置气泡大小（更小的尺寸）
        int bubbleSize = dpToPx(20); // 20dp
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bubbleSize, bubbleSize);

        // 设置气泡位置（基于点击坐标）
        params.leftMargin = (int) (x - 3 * bubbleSize);
        params.topMargin = (int) (y - bubbleSize);

        bubble.setLayoutParams(params);
        bubble.setAlpha(0f);

        // 添加到效果容器
        effectContainer.addView(bubble);

        // 气泡动画
        AnimatorSet bubbleAnim = new AnimatorSet();

        // 缩放动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bubble, "scaleX", 0.5f, 1.5f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(bubble, "scaleY", 0.5f, 1.5f);

        // 透明度动画
        ObjectAnimator alphaIn = ObjectAnimator.ofFloat(bubble, "alpha", 0f, 0.6f);
        alphaIn.setDuration(200);

        ObjectAnimator alphaOut = ObjectAnimator.ofFloat(bubble, "alpha", 0.6f, 0f);
        alphaOut.setDuration(300);

        // 组合动画
        bubbleAnim.play(scaleX).with(scaleY).with(alphaIn);
        bubbleAnim.play(alphaOut).after(200);
        bubbleAnim.setDuration(500);

        bubbleAnim.start();

        // 动画结束后移除气泡
        bubbleAnim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                effectContainer.removeView(bubble);
            }
        });
    }

    private void setupAnimations() {
        // 初始的轻微浮动动画
        startIdleAnimation();

        // 随机更换对话
        startRandomSpeechChanges();
    }

    /**
     * 宠物空闲状态动画 - 轻微浮动
     */
    private void startIdleAnimation() {
        ObjectAnimator floatAnim = ObjectAnimator.ofFloat(petImage, "translationY", 0f, -10f, 0f);
        floatAnim.setDuration(3000);
        floatAnim.setRepeatCount(ValueAnimator.INFINITE);
        floatAnim.setRepeatMode(ValueAnimator.REVERSE);
        floatAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnim.start();
    }

    /**
     * 开始宠物互动动画
     */
    private void startPetInteraction() {
        // 停止当前动画
        if (currentAnimation != null && currentAnimation.isRunning()) {
            currentAnimation.cancel();
        }

        // 创建新的动画组合
        currentAnimation = new AnimatorSet();

        // 1. 跳跃动画
        ObjectAnimator jumpUp = ObjectAnimator.ofFloat(petImage, "translationY", 0f, -30f);
        jumpUp.setDuration(200);
        jumpUp.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator jumpDown = ObjectAnimator.ofFloat(petImage, "translationY", -30f, 0f);
        jumpDown.setDuration(300);
        jumpDown.setInterpolator(new BounceInterpolator());

        // 2. 缩放动画（开心效果）
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(petImage, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(petImage, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(500);
        scaleY.setDuration(500);

        // 3. 旋转动画（轻微摇摆）
        ObjectAnimator rotate = ObjectAnimator.ofFloat(petImage, "rotation", -5f, 5f, 0f);
        rotate.setDuration(400);

        // 组合动画
        currentAnimation.play(jumpUp).before(jumpDown);
        currentAnimation.play(scaleX).with(scaleY).with(rotate).after(jumpUp);

        currentAnimation.start();

        // 更新对话
        updatePetSpeech();
    }

    /**
     * 更新宠物对话
     */
    private void updatePetSpeech() {
        String newSpeech = petDialogs[random.nextInt(petDialogs.length)];
        petSpeech.setText(newSpeech);

        // 对话气泡动画
        speechBubble.setScaleX(0.8f);
        speechBubble.setScaleY(0.8f);
        speechBubble.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();
    }

    /**
     * 随机更换对话
     */
    private void startRandomSpeechChanges() {
        Runnable speechChanger = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    // 25%的几率自动更换对话
                    if (random.nextInt(4) == 0) {
                        updatePetSpeech();
                    }
                    speechHandler.postDelayed(this, 10000); // 每10秒检查一次
                }
            }
        };
        speechHandler.postDelayed(speechChanger, 10000);
    }

    /**
     * dp转px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentAnimation != null) {
            currentAnimation.cancel();
        }
        speechHandler.removeCallbacksAndMessages(null);
    }
}