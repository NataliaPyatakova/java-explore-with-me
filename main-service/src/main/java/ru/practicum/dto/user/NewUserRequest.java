package ru.practicum.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NewUserRequest {

    @NotBlank
    @Email
    @Size(min = 6, max = 254, message = "Email must be between 6 and 254 characters")
    private String email;
    @NotBlank
    @Size(min = 2, max = 250, message = "Username must be between 2 and 250 characters")
    private String name;
}
