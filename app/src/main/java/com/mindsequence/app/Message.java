package com.mindsequence.app;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private String content;
    private int type;
    private String timestamp;

    public Message(String content, int type, String timestamp) {
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getContent() { return content; }
    public int getType() { return type; }
    public String getTimestamp() { return timestamp; }
}