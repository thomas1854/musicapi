package thomas.musicapi.dto;

import jakarta.validation.constraints.Size;

public record AddDeleteMusicPlaylistRequest(
        @Size(min = 1, message = "request must contain at least one music")
        Long[] musicIds
) {
}
