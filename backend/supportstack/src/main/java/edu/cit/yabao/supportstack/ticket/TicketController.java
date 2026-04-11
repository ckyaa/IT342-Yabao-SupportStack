package edu.cit.yabao.supportstack.ticket;

import edu.cit.yabao.supportstack.dto.ApiResponse;
import edu.cit.yabao.supportstack.dto.CreateTicketRequest;
import edu.cit.yabao.supportstack.dto.TicketResponse;
import edu.cit.yabao.supportstack.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody CreateTicketRequest request,
                                                                     Authentication authentication) {
        TicketResponse response = ticketService.createTicket(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets(Authentication authentication) {
        List<TicketResponse> response = ticketService.getMyTickets(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> getMyTicket(@PathVariable Long ticketId,
                                                                   Authentication authentication) {
        TicketResponse response = ticketService.getMyTicket(ticketId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}