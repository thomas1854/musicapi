package thomas.musicapi.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePlaylistRequest(
        String title,
        String description
) {
}
