package com.pulsedesk.triage.dto;

import com.pulsedesk.triage.entity.Ticket;

import java.time.Instant;

public class TicketResponse {
    private Long id;
    private String title;
    private String category;
    private String priority;
    private String summary;
    private Instant createdAt;
    private Long commentId;

    public static TicketResponse from(Ticket t) {
        TicketResponse r = new TicketResponse();
        r.id = t.getId();
        r.title = t.getTitle();
        r.category = t.getCategory();
        r.priority = t.getPriority();
        r.summary = t.getSummary();
        r.createdAt = t.getCreatedAt();
        r.commentId = t.getComment() != null ? t.getComment().getId() : null;
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getPriority() { return priority; }
    public String getSummary() { return summary; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getCommentId() { return commentId; }
}
