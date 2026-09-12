package thomas.musicapi.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import thomas.musicapi.model.User;

import java.time.LocalDateTime;

public class UploadMusicRequest {
    @NotBlank(message = "music title is required")
    private String title;
    @NotBlank(message = "username is required")
    private String username;

    public UploadMusicRequest(String username, String title) {
        this.username = username;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
