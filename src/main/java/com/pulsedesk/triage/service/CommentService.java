package com.pulsedesk.triage.service;

import com.pulsedesk.triage.dto.AiTriageResult;
import com.pulsedesk.triage.dto.CommentResponse;
import com.pulsedesk.triage.dto.CreateCommentRequest;
import com.pulsedesk.triage.entity.Comment;
import com.pulsedesk.triage.entity.Ticket;
import com.pulsedesk.triage.repository.CommentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final HuggingFaceService hf;
    private final TicketService ticketService;

    public CommentService(
            CommentRepository commentRepository,
            HuggingFaceService hf,
            TicketService ticketService
    ) {
        this.commentRepository = commentRepository;
        this.hf = hf;
        this.ticketService = ticketService;
    }

    public CommentResponse create(CreateCommentRequest req) {
        AiTriageResult ai = hf.analyzeToResult(req.getText());

        Comment c = new Comment();
        c.setText(req.getText());
        c.setSource(req.getSource());
        c.setCreatedAt(Instant.now());
        c = commentRepository.save(c);

        if (Boolean.TRUE.equals(ai.getIsTicket())) {
            Ticket t = ticketService.createFromAi(ai, c);
            c.setConvertedToTicket(true);
            c.setTicketId(t.getId());
            c = commentRepository.save(c);
        }

        return CommentResponse.from(c);
    }

    public List<CommentResponse> list() {
        return commentRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(CommentResponse::from)
                .toList();
    }
}