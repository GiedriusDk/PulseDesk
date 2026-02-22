package com.pulsedesk.triage.service;

import com.pulsedesk.triage.dto.AiTriageResult;
import com.pulsedesk.triage.entity.Comment;
import com.pulsedesk.triage.entity.Ticket;
import com.pulsedesk.triage.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket createFromAi(AiTriageResult ai, Comment comment) {
        Ticket t = new Ticket();
        t.setTitle(ai.getTitle());
        t.setCategory(ai.getCategory());
        t.setPriority(ai.getPriority());
        String summary = ai.getSummary();
        if (summary != null && summary.length() > 400) {
            summary = summary.substring(0, 400).trim();
            int lastSpace = summary.lastIndexOf(' ');
            if (lastSpace > 300) summary = summary.substring(0, lastSpace);
        }
        t.setSummary(summary != null ? summary : "");
        t.setCreatedAt(Instant.now());
        t.setComment(comment);
        return ticketRepository.save(t);
    }

    public List<Ticket> list() {
        return ticketRepository.findAll();
    }

    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    public Ticket getById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
    }
}