package com.purpura.app.model.mongo;

import java.util.List;

public class ChatResponse {
    private String id;
    private List<String> participants;
    private String lastMessagePreview = null;
    private Long lastUpdated = System.currentTimeMillis();
    private int unreadCount = 0;

    public ChatResponse(String id, List<String> participants, String lastMessagePreview, Long lastUpdated, int unreadCount) {
        this.id = id;
        this.participants = participants;
        this.lastMessagePreview = lastMessagePreview;
        this.lastUpdated = lastUpdated;
        this.unreadCount = unreadCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
