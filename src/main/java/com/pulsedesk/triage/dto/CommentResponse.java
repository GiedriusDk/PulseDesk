package com.pulsedesk.triage.dto;

import com.pulsedesk.triage.entity.Comment;

import java.time.Instant;

public class CommentResponse {
    private Long id;
    private String text;
    private String source;
    private Instant createdAt;
    private boolean convertedToTicket;
    private Long ticketId;

    public static CommentResponse from(Comment c) {
        CommentResponse r = new CommentResponse();
        r.id = c.getId();
        r.text = c.getText();
        r.source = c.getSource();
        r.createdAt = c.getCreatedAt();
        r.convertedToTicket = c.isConvertedToTicket();
        r.ticketId = c.getTicketId();
        return r;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isConvertedToTicket() { return convertedToTicket; }
    public Long getTicketId() { return ticketId; }
}