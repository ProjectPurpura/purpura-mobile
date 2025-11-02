package com.purpura.app.model.mongo;

import java.util.List;

public class ChatResponse {
    private String id;
    private List<String> participants;
    private String lastMessagePreview = null;
    private Long lastUpdated = System.currentTimeMillis();
    private int unreadCount = 0;

}
