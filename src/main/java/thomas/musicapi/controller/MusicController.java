package thomas.musicapi.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thomas.musicapi.dto.UploadMusicRequest;
import thomas.musicapi.model.Music;
import thomas.musicapi.service.MusicService;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestController
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @PostMapping("/upload")
    public Music uploadMusic(@Validated @RequestPart("metadata") UploadMusicRequest uploadMusicRequest, @RequestPart("file") MultipartFile multipartFile) throws IOException
    {
        return musicService.uploadMusic(uploadMusicRequest, multipartFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMusic(@PathVariable Long id)
    {
        musicService.deleteMusic(id);
        return ResponseEntity.ok().body(Map.of("message", "Music deleted successfully"));
    }
}
