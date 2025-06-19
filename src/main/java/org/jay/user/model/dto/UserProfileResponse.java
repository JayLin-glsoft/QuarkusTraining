package org.jay.user.model.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private Instant createdTime;
}
