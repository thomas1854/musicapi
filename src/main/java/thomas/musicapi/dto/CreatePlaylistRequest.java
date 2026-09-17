package thomas.musicapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlaylistRequest(
        @NotBlank(message = "play list title is required")
        String title,
        String description
) {
}
