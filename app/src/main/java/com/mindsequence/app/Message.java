package com.mindsequence.app;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private String content;   // 消息内容
    private int type;         // 消息类型（用户/AI）
    private String timestamp; // 时间戳

    // 构造方法
    public Message(String content, int type, String timestamp) {
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getter方法（必须补全，解决"无法解析getType"报错）
    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }
}