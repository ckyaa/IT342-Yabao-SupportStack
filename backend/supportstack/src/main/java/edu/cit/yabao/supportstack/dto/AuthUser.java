package edu.cit.yabao.supportstack.dto;

public record AuthUser(
        Long id,
        String username,
        String name,
        String email
) {
}
