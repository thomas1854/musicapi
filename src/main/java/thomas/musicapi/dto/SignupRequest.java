package thomas.musicapi.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record SignupRequest(
        @NotBlank(message = "username is required")
        String username,

        @Email(message = "valid email is required")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password length should be at least 8")
        String password) {
}
