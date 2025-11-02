package com.purpura.app.model.mongo;

import java.util.List;

public class ChatRequest {
    private List<String> participants;

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public ChatRequest(List<String> participants) {
        this.participants = participants;
    }

    public ChatRequest(){}

}
