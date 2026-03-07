package edu.cit.yabao.supportstack.dto;

public record AuthResponse(
        AuthUser user,
        String accessToken
) {
}
