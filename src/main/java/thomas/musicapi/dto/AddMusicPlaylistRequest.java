package thomas.musicapi.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Array;
import thomas.musicapi.model.Music;

import java.util.List;

public record AddMusicPlaylistRequest(
        @Size(min = 1, message = "must add at least one music")
        Long[] musicIds
) {
}
