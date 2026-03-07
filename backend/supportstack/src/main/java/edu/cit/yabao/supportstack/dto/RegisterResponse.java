package edu.cit.yabao.supportstack.dto;

public record RegisterResponse(
        Long id,
        String username,
        String name,
        String email
) {
}
