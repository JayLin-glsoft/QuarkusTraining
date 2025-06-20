package org.jay.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank")
    public String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    public String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    public String email;
}
