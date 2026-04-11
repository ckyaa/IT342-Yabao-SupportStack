package edu.cit.yabao.supportstack.dto;

import java.time.OffsetDateTime;

public record TicketResponse(
        Long id,
        String ticketNumber,
        String title,
        String description,
        String department,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}