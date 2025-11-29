package com.mindsequence.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// 修正：RecyclerView 正确导入路径
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIChatActivity extends AppCompatActivity {
    // ！！！请替换为你自己的DeepSeek API Key（必填，否则无法调用AI）！！！
    // 获取地址：https://platform.deepseek.com/
    private static final String DEEPSEEK_API_KEY = "sk-428e89d1d9744768b7c94128f9019341";
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions";

    // 成员变量（解决"无法解析符号"报错）
    private List<Message> mMessageList;
    private ChatAdapter mAdapter;
    private RecyclerView rvChat;
    private EditText etMessage;
    private OkHttpClient mOkHttpClient;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // 隐藏ActionBar，保持和主页风格一致
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 初始化核心对象
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS) // 超时时间15秒
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build(); // 优化OkHttp配置，避免超时
        mGson = new Gson();
        mMessageList = new ArrayList<>();

        // 绑定控件ID（添加空指针防护）
        bindViews();

        // 初始化RecyclerView
        initRecyclerView();

        // 初始化输入框和发送按钮（适配新布局的发送按钮）
        initInputView();

        // 添加初始AI欢迎消息
        addWelcomeMessage();
    }

    // 绑定控件（解决"无法解析rvChat/etMessage"报错，添加空指针防护）
    private void bindViews() {
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);

        // 空指针检查
        if (rvChat == null) {
            Toast.makeText(this, "控件绑定失败：未找到rv_chat", Toast.LENGTH_SHORT).show();
        }
        if (etMessage == null) {
            Toast.makeText(this, "控件绑定失败：未找到et_message", Toast.LENGTH_SHORT).show();
        }
    }

    // 初始化RecyclerView（步骤3核心：列表初始化）
    private void initRecyclerView() {
        if (rvChat == null) return;

        // 1. 设置线性布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // 列表从底部开始显示（符合聊天习惯）
        rvChat.setLayoutManager(layoutManager);

        // 2. 初始化适配器并绑定
        mAdapter = new ChatAdapter(mMessageList);
        rvChat.setAdapter(mAdapter);

        // 3. 优化RecyclerView性能
        rvChat.setHasFixedSize(true);
        rvChat.setItemViewCacheSize(20);
        rvChat.setNestedScrollingEnabled(false); // 避免滚动冲突
    }

    // 初始化输入框和发送按钮（适配新布局，支持按钮点击+回车发送）
    private void initInputView() {
        Button btnSend = findViewById(R.id.btn_send);

        // 空指针防护：发送按钮不存在时提示
        if (btnSend == null) {
            Toast.makeText(this, "控件绑定失败：未找到btn_send", Toast.LENGTH_SHORT).show();
            return;
        }

        // 发送按钮点击事件
        btnSend.setOnClickListener(v -> sendMessage());

        // 软键盘回车发送（适配布局的imeActionId，兼容多换行输入）
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            // 适配IME_ACTION_SEND和自定义的send actionId
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == R.id.send) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    // 添加初始AI欢迎消息（步骤3核心：初始状态）
    private void addWelcomeMessage() {
        String welcomeTime = getCurrentTimestamp();
        mMessageList.add(new Message(
                "Hi, I'm your empathetic AI partner. How are you feeling today? I'm here to listen without judgment.",
                Message.TYPE_AI,
                welcomeTime
        ));
        // 通知适配器更新
        mAdapter.notifyItemInserted(mMessageList.size() - 1);
        // 滚动到最新消息
        rvChat.scrollToPosition(mMessageList.size() - 1);
    }

    // 发送消息+调用DeepSeek API（核心业务逻辑，添加API Key校验）
    private void sendMessage() {
        // 1. 输入内容校验
        String userContent = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();
        if (userContent.isEmpty()) {
            Toast.makeText(this, "请输入消息后发送", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. API Key校验（必填）
        if (DEEPSEEK_API_KEY.equals("YOUR_DEEPSEEK_API_KEY_HERE")) {
            Toast.makeText(this, "请先替换DEEPSEEK_API_KEY为你的真实密钥", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. 添加用户消息到列表
        String userTime = getCurrentTimestamp();
        mMessageList.add(new Message(userContent, Message.TYPE_USER, userTime));
        mAdapter.notifyItemInserted(mMessageList.size() - 1);
        etMessage.setText(""); // 清空输入框
        rvChat.scrollToPosition(mMessageList.size() - 1); // 滚动到最新消息

        // 4. 显示"AI正在输入..."占位（提升用户体验）
        String loadingTime = getCurrentTimestamp();
        int loadingPos = mMessageList.size();
        mMessageList.add(new Message("AI is typing...", Message.TYPE_AI, loadingTime));
        mAdapter.notifyItemInserted(loadingPos);
        rvChat.scrollToPosition(loadingPos);

        // 5. 构建DeepSeek API请求参数
        DeepSeekRequest requestBody = new DeepSeekRequest();
        requestBody.setModel("deepseek-chat"); // 指定DeepSeek对话模型
        List<DeepSeekMessage> messages = new ArrayList<>();

        // 遍历历史消息，构建上下文（排除"正在输入"占位）
        for (int i = 0; i < mMessageList.size() - 1; i++) {
            Message msg = mMessageList.get(i);
            String role = msg.getType() == Message.TYPE_USER ? "user" : "assistant";
            messages.add(new DeepSeekMessage(role, msg.getContent()));
        }
        requestBody.setMessages(messages);
        requestBody.setTemperature(0.9); // 共情场景，提高回复自然度
        requestBody.setMaxTokens(2048); // 设置最大回复长度

        // 6. 转为JSON字符串
        String jsonBody = mGson.toJson(requestBody);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonBody
        );

        // 7. 构建HTTP请求
        Request request = new Request.Builder()
                .url(DEEPSEEK_API_URL)
                .addHeader("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        // 8. 异步调用API（避免主线程网络请求）
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 请求失败，切回主线程更新UI
                runOnUiThread(() -> {
                    // 移除"正在输入"占位
                    if (loadingPos < mMessageList.size()) {
                        mMessageList.remove(loadingPos);
                        mAdapter.notifyItemRemoved(loadingPos);
                    }

                    // 添加失败提示（友好提示）
                    String errorMsg = "网络请求失败：" + e.getMessage();
                    mMessageList.add(new Message(errorMsg, Message.TYPE_AI, getCurrentTimestamp()));
                    mAdapter.notifyItemInserted(mMessageList.size() - 1);
                    rvChat.scrollToPosition(mMessageList.size() - 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    // 移除"正在输入"占位
                    if (loadingPos < mMessageList.size()) {
                        mMessageList.remove(loadingPos);
                        mAdapter.notifyItemRemoved(loadingPos);
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            // 解析AI回复
                            String responseStr = response.body().string();
                            DeepSeekResponse deepSeekResponse = mGson.fromJson(responseStr, DeepSeekResponse.class);

                            // 空指针防护：解析结果检查
                            if (deepSeekResponse == null || deepSeekResponse.getChoices() == null || deepSeekResponse.getChoices().isEmpty()) {
                                throw new Exception("AI回复为空");
                            }

                            String aiReply = deepSeekResponse.getChoices().get(0).getMessage().getContent();

                            // 添加AI回复到列表
                            mMessageList.add(new Message(aiReply, Message.TYPE_AI, getCurrentTimestamp()));
                            mAdapter.notifyItemInserted(mMessageList.size() - 1);
                            rvChat.scrollToPosition(mMessageList.size() - 1);
                        } catch (Exception e) {
                            String errorMsg = "回复解析失败：" + e.getMessage();
                            mMessageList.add(new Message(errorMsg, Message.TYPE_AI, getCurrentTimestamp()));
                            mAdapter.notifyItemInserted(mMessageList.size() - 1);
                        }
                    } else {
                        // 非200响应码处理
                        String errorCode = response.code() + "";
                        String errorMsg = "AI回复失败：错误码" + errorCode + "，" + response.message();
                        mMessageList.add(new Message(errorMsg, Message.TYPE_AI, getCurrentTimestamp()));
                        mAdapter.notifyItemInserted(mMessageList.size() - 1);
                    }
                });
            }
        });
    }

    // 生成当前时间戳（格式：HH:MM AM/PM）
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        return sdf.format(new Date());
    }

    // ========== DeepSeek API 实体类 ==========
    static class DeepSeekRequest {
        private String model;
        private List<DeepSeekMessage> messages;
        private double temperature;
        private int maxTokens = 2048; // 最大回复长度

        public void setModel(String model) {
            this.model = model;
        }

        public void setMessages(List<DeepSeekMessage> messages) {
            this.messages = messages;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }
    }

    static class DeepSeekMessage {
        private String role;
        private String content;

        public DeepSeekMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // 必须：补充getContent()方法
        public String getContent() {
            return content;
        }

        // 可选：补充getRole()方法
        public String getRole() {
            return role;
        }
    }

    static class DeepSeekResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        static class Choice {
            private DeepSeekMessage message;

            public DeepSeekMessage getMessage() {
                return message;
            }
        }
    }
}