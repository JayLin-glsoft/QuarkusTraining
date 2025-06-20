package org.jay.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    // 沒有 @NotBlank，允許為 null
    private String username;

    // 如果提供了密碼，長度至少為 8
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}