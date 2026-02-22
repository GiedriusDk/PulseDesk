package com.pulsedesk.triage.controller;

import com.pulsedesk.triage.dto.TicketResponse;
import com.pulsedesk.triage.entity.Ticket;
import com.pulsedesk.triage.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService tickets;

    public TicketController(TicketService tickets) {
        this.tickets = tickets;
    }

    @GetMapping
    public List<TicketResponse> list() {
        return tickets.list().stream()
                .map(TicketResponse::from)
                .toList();
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> get(@PathVariable Long ticketId) {
        Ticket t = tickets.findById(ticketId);
        if (t == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(TicketResponse.from(t));
    }
}