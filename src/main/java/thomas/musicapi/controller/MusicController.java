package thomas.musicapi.controller;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import thomas.musicapi.dto.UploadMusicRequest;
import thomas.musicapi.model.Music;
import thomas.musicapi.service.MusicService;

import java.io.IOException;

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
}
