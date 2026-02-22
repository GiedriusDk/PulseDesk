package com.pulsedesk.triage.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4000)
    private String text;

    @Column(length = 64)
    private String source;

    private Instant createdAt;

    private boolean convertedToTicket;

    @Column(nullable = true)
    private Long ticketId;

    // --- getters / setters ---

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isConvertedToTicket() {
        return convertedToTicket;
    }

    public void setConvertedToTicket(boolean convertedToTicket) {
        this.convertedToTicket = convertedToTicket;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
}