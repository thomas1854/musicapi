package thomas.musicapi.controller;

import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thomas.musicapi.dto.UpdateMusicRequest;
import thomas.musicapi.dto.UploadMusicRequest;
import thomas.musicapi.model.Music;
import thomas.musicapi.service.MusicService;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/musics")
public class MusicController {
    private final MusicService musicService;

    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @PostMapping("")
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

    @PatchMapping("/{id}")
    public Music updateMusic(@PathVariable Long id, @Nullable @RequestPart("metadata")UpdateMusicRequest updateMusicRequest, @Nullable @RequestPart("file") MultipartFile multipartFile) throws IOException
    {
        return musicService.updateMusic(id, updateMusicRequest, multipartFile);
    }

    @GetMapping("")
    public List<Music> getAllMusic()
    {
        return musicService.getAllMusic();
    }

    @GetMapping("/{id}")
    public Music getMusicById(@PathVariable Long id)
    {
        return musicService.getMusicById(id);
    }
}
