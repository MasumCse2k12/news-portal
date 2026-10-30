package com.newsportal.domain.user.dto;

import com.newsportal.domain.user.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String fullNameBn;
    private Role role;
    private String profileImage;
    private boolean active;
    private Instant createdAt;
}
