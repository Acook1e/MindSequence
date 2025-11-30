package com.mindsequence.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIChatActivity extends AppCompatActivity {
    private static final String DEEPSEEK_API_KEY = "sk-428e89d1d9744768b7c94128f9019341";
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";

    private List<Message> mMessageList;
    private ChatAdapter mAdapter;
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private OkHttpClient mOkHttpClient;
    private Gson mGson;
    private Executor mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 初始化
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        mGson = new Gson();
        mMessageList = new ArrayList<>();
        mExecutor = Executors.newSingleThreadExecutor(); // 创建单线程执行器

        setupHeader(); // 设置头部
        bindViews();
        initRecyclerView();
        initInputView();
        addWelcomeMessage();
    }

    /**
     * 设置通用头部
     */
    private void setupHeader() {
        // 设置头部标题
        View headerView = findViewById(R.id.header);
        TextView headerMessage = headerView.findViewById(R.id.header_message);
        if (headerMessage != null) {
            headerMessage.setText("AI Empathetic Partner");
        }

        // 设置返回按钮 - 修改为返回主页
        ImageButton backButton = headerView.findViewById(R.id.btn_back);
        if (backButton != null) {
            backButton.setVisibility(View.VISIBLE);
            // 修改返回按钮逻辑：返回主页
            backButton.setOnClickListener(v -> returnToMainActivity());
        }
    }

    /**
     * 返回主页的方法
     */
    private void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * 重写返回键处理，使其也返回主页
     */
    @Override
    public void onBackPressed() {
        returnToMainActivity();
    }

    private void bindViews() {
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
    }

    private void initRecyclerView() {
        if (rvChat == null) return;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);

        mAdapter = new ChatAdapter(mMessageList);
        rvChat.setAdapter(mAdapter);
        rvChat.setHasFixedSize(true);
    }

    private void initInputView() {
        if (btnSend == null) {
            Toast.makeText(this, "发送按钮初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> sendMessage());

        // 软键盘回车发送
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // 文本变化监听
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                boolean hasText = !TextUtils.isEmpty(s.toString().trim());
                btnSend.setEnabled(hasText);
                btnSend.setAlpha(hasText ? 1.0f : 0.5f);
            }
        });
    }

    private void addWelcomeMessage() {
        String welcomeTime = getCurrentTimestamp();
        mMessageList.add(new Message(
                "Hi, I'm your empathetic AI partner. How are you feeling today? I'm here to listen without judgment.",
                Message.TYPE_AI,
                welcomeTime
        ));
        mAdapter.notifyItemInserted(mMessageList.size() - 1);
        scrollToBottom();
    }

    private void sendMessage() {
        String userContent = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
        if (userContent.isEmpty()) {
            Toast.makeText(this, "请输入消息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (DEEPSEEK_API_KEY.equals("YOUR_DEEPSEEK_API_KEY_HERE")) {
            Toast.makeText(this, "请配置API密钥", Toast.LENGTH_LONG).show();
            return;
        }

        // 添加用户消息
        String userTime = getCurrentTimestamp();
        mMessageList.add(new Message(userContent, Message.TYPE_USER, userTime));
        mAdapter.notifyItemInserted(mMessageList.size() - 1);
        etMessage.setText("");
        scrollToBottom();

        // 显示AI正在输入
        String loadingTime = getCurrentTimestamp();
        int loadingPos = mMessageList.size();
        mMessageList.add(new Message("AI is thinking...", Message.TYPE_AI, loadingTime));
        mAdapter.notifyItemInserted(loadingPos);
        scrollToBottom();

        // 调用API
        callDeepSeekAPI(userContent, loadingPos);
    }

    private void callDeepSeekAPI(String userContent, int loadingPos) {
        // 确保在后台线程执行
        new Thread(() -> {
            try {
                DeepSeekRequest requestBody = new DeepSeekRequest();
                requestBody.setModel("deepseek-chat");
                List<DeepSeekMessage> messages = new ArrayList<>();

                // 构建历史消息
                for (int i = 0; i < mMessageList.size() - 1; i++) {
                    Message msg = mMessageList.get(i);
                    String role = msg.getType() == Message.TYPE_USER ? "user" : "assistant";
                    messages.add(new DeepSeekMessage(role, msg.getContent()));
                }

                requestBody.setMessages(messages);
                requestBody.setTemperature(0.9);
                requestBody.setMaxTokens(2048);

                String jsonBody = mGson.toJson(requestBody);
                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        jsonBody
                );

                Request request = new Request.Builder()
                        .url(DEEPSEEK_API_URL)
                        .addHeader("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                // 在后台线程执行同步请求
                Response response = mOkHttpClient.newCall(request).execute();

                // 处理响应（仍在后台线程）
                String responseStr = null;
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        responseStr = response.body().string();
                        System.out.println("API Response: " + responseStr);

                        final DeepSeekResponse deepSeekResponse = mGson.fromJson(responseStr, DeepSeekResponse.class);
                        final String aiReply;

                        if (deepSeekResponse != null &&
                                deepSeekResponse.getChoices() != null &&
                                !deepSeekResponse.getChoices().isEmpty() &&
                                deepSeekResponse.getChoices().get(0) != null &&
                                deepSeekResponse.getChoices().get(0).getMessage() != null) {

                            aiReply = deepSeekResponse.getChoices().get(0).getMessage().getContent();
                        } else {
                            throw new Exception("API响应格式不正确");
                        }

                        // 回到主线程更新UI
                        runOnUiThread(() -> {
                            removeLoadingMessage(loadingPos);
                            if (aiReply != null && !aiReply.trim().isEmpty()) {
                                mMessageList.add(new Message(aiReply.trim(), Message.TYPE_AI, getCurrentTimestamp()));
                                mAdapter.notifyItemInserted(mMessageList.size() - 1);
                                scrollToBottom();
                            } else {
                                try {
                                    throw new Exception("AI回复内容为空");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                    } else {
                        final String errorMsg = "请求失败: " + response.code();
                        runOnUiThread(() -> {
                            removeLoadingMessage(loadingPos);
                            mMessageList.add(new Message(errorMsg, Message.TYPE_AI, getCurrentTimestamp()));
                            mAdapter.notifyItemInserted(mMessageList.size() - 1);
                            scrollToBottom();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final String finalErrorMsg = "回复解析失败: " + e.getMessage();
                    runOnUiThread(() -> {
                        removeLoadingMessage(loadingPos);
                        mMessageList.add(new Message(finalErrorMsg, Message.TYPE_AI, getCurrentTimestamp()));
                        mAdapter.notifyItemInserted(mMessageList.size() - 1);
                        scrollToBottom();
                    });
                } finally {
                    if (response != null && response.body() != null) {
                        response.body().close(); // 防止连接泄漏
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    removeLoadingMessage(loadingPos);
                    String errorMsg = "网络请求失败: " + e.getMessage();
                    mMessageList.add(new Message(errorMsg, Message.TYPE_AI, getCurrentTimestamp()));
                    mAdapter.notifyItemInserted(mMessageList.size() - 1);
                    scrollToBottom();
                });
            }
        }).start();
    }

    private void removeLoadingMessage(int loadingPos) {
        if (loadingPos < mMessageList.size() && mMessageList.get(loadingPos).getContent().equals("AI is thinking...")) {
            mMessageList.remove(loadingPos);
            mAdapter.notifyItemRemoved(loadingPos);
        }
    }

    private void scrollToBottom() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mMessageList.size() > 0) {
                rvChat.scrollToPosition(mMessageList.size() - 1);
            }
        }, 100);
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        return sdf.format(new Date());
    }

    // API请求相关类
    static class DeepSeekRequest {
        private String model;
        private List<DeepSeekMessage> messages;
        private double temperature;
        private int maxTokens = 2048;

        public void setModel(String model) { this.model = model; }
        public void setMessages(List<DeepSeekMessage> messages) { this.messages = messages; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    static class DeepSeekMessage {
        private String role;
        private String content;

        public DeepSeekMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getContent() { return content; }
        public String getRole() { return role; }
    }

    static class DeepSeekResponse {
        private List<Choice> choices;
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }

        static class Choice {
            private int index;
            private DeepSeekMessage message;
            private String finish_reason;

            public DeepSeekMessage getMessage() { return message; }
            public void setMessage(DeepSeekMessage message) { this.message = message; }
            public int getIndex() { return index; }
            public void setIndex(int index) { this.index = index; }
            public String getFinish_reason() { return finish_reason; }
            public void setFinish_reason(String finish_reason) { this.finish_reason = finish_reason; }
        }
    }
}