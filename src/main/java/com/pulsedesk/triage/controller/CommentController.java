package com.pulsedesk.triage.controller;

import com.pulsedesk.triage.dto.CommentResponse;
import com.pulsedesk.triage.dto.CreateCommentRequest;
import com.pulsedesk.triage.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @PostMapping
    public CommentResponse create(@RequestBody CreateCommentRequest req) {
        return comments.create(req);
    }

    @GetMapping
    public List<CommentResponse> list() {
        return comments.list();
    }
}