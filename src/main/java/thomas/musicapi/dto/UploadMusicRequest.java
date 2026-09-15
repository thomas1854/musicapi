package thomas.musicapi.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import thomas.musicapi.model.User;

import java.time.LocalDateTime;

public record UploadMusicRequest(
        @NotBlank(message = "music title is required") String title) {
}
