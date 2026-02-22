package com.pulsedesk.triage.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AiTriageResult {

    private Boolean ticket;

    private String title;
    private String category;
    private String priority;
    private String summary;

    public Boolean getTicket() {
        return ticket;
    }

    public void setTicket(Boolean ticket) {
        this.ticket = ticket;
    }

    public Boolean getIsTicket() {
        return ticket;
    }

    public void setIsTicket(Boolean isTicket) {
        this.ticket = isTicket;
    }

    public boolean isTicket() {
        return Boolean.TRUE.equals(ticket);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String toJsonString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"failed to serialize\"}";
        }
    }
}