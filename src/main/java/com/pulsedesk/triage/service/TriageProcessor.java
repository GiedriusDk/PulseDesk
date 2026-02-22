package com.pulsedesk.triage.service;

import com.pulsedesk.triage.dto.AiTriageResult;
import com.pulsedesk.triage.entity.Comment;
import com.pulsedesk.triage.entity.Ticket;
import com.pulsedesk.triage.repository.CommentRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TriageProcessor {

    private final CommentRepository commentRepository;
    private final HuggingFaceService hf;
    private final TicketService ticketService;

    public TriageProcessor(
            CommentRepository commentRepository,
            HuggingFaceService hf,
            TicketService ticketService
    ) {
        this.commentRepository = commentRepository;
        this.hf = hf;
        this.ticketService = ticketService;
    }

    @Async
    public void runTriageAsync(Long commentId) {
        Comment c = commentRepository.findById(commentId).orElse(null);
        if (c == null) return;
        try {
            AiTriageResult ai = hf.analyzeToResult(c.getText());
            if (Boolean.TRUE.equals(ai.getIsTicket())) {
                Ticket t = ticketService.createFromAi(ai, c);
                c.setConvertedToTicket(true);
                c.setTicketId(t.getId());
                commentRepository.save(c);
            }
        } catch (Exception ignored) {
        }
    }
}
