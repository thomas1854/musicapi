package thomas.musicapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
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
    public Music uploadMusic(@Validated @RequestPart("metadata") UploadMusicRequest uploadMusicRequest, @RequestPart("file") MultipartFile multipartFile) throws IOException {
        return musicService.uploadMusic(uploadMusicRequest, multipartFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMusic(@PathVariable Long id) {
        musicService.deleteMusic(id);
        return ResponseEntity.ok().body(Map.of("message", "Music deleted successfully"));
    }

    @PatchMapping("/{id}")
    public Music updateMusic(@PathVariable Long id, @Nullable @RequestPart("metadata") UpdateMusicRequest updateMusicRequest, @Nullable @RequestPart("file") MultipartFile multipartFile) throws IOException {
        return musicService.updateMusic(id, updateMusicRequest, multipartFile);
    }

    @GetMapping("")
    public List<Music> getAllMusic() {
        return musicService.getAllMusic();
    }

    @GetMapping("/{id}")
    public Music getMusicById(@PathVariable Long id) {
        return musicService.getMusicById(id);
    }


    // For testing in browser audio player: @CrossOrigin(origins = "http://127.0.0.1:5500/")
    @GetMapping("stream/{id}")
    public ResponseEntity<StreamingResponseBody> streamMusic(HttpServletRequest request, @PathVariable Long id) throws BadRequestException {
        // TODO: Avoid querying the music object for every range request.
        Music music = musicService.getMusicById(id);
        List<Integer> byteRanges;
        if (request.getHeader("Range") == null
                || !request.getHeader("Range").startsWith("bytes=")
                || (byteRanges = isValidRange(request.getHeader("Range"), music.getFileSize())) == null)
            throw new BadRequestException("the request must be valid range request");


        int startByte = byteRanges.get(0);
        int endByte = byteRanges.get(1);

        int bytesToSend = endByte - startByte + 1;
        ResponseInputStream<GetObjectResponse> s3InputStream = musicService.streamingResponseBody(startByte, endByte, id);
        StreamingResponseBody streamingResponseBody = outputStream -> {
            try (s3InputStream) {
                int numberOfBytesToWrite = 0;
                byte[] data = new byte[1024];
                while ((numberOfBytesToWrite = s3InputStream.read(data)) != -1) {
                    outputStream.write(data, 0, numberOfBytesToWrite);
                }
            }
        };
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).header("Content-Length", String.valueOf(bytesToSend)).header("Content-Type", music.getMimeType()).header("Accept-Ranges", "bytes").header("Content-Range", "bytes " + startByte + "-" + endByte + "/" + music.getFileSize()).body(streamingResponseBody);
    }

    private List<Integer> isValidRange(String range, long fileSize) {
        int startByte = -1;
        int endByte = -1;
        boolean hasSeparator = false;
        range = range.substring(6);

        for (int i = 0; i < range.length(); i++) {
            if (range.charAt(i) != '-' && !(range.charAt(i) >= '0' && range.charAt(i) <= '9')) {
                return null;
            }
            if (range.charAt(i) == '-')
                hasSeparator = true;
        }

        if (!hasSeparator)
            return null;

        String[] rangeParts = range.split("-");
        if (rangeParts.length < 1 || rangeParts.length > 2)
            return null;

        try
        {
            if (rangeParts.length == 1 && range.charAt(0) == '-')
            {
                // Suffix byte range
                if (rangeParts[0].length() > 9)
                    return null;
                int requestedBytes = Integer.parseInt(rangeParts[0]);
                if (requestedBytes == 0) return null;
                endByte = (int) fileSize - 1;
                startByte = Math.max(0, (int) fileSize - requestedBytes);
            }
            else if (rangeParts.length == 1 && range.charAt(0) != '-')
            {
                // Start byte range
                if (rangeParts[0].length() > 9)
                    return null;
                endByte = (int) fileSize - 1;
                startByte = Integer.parseInt(rangeParts[0]);
            }
            else
            {
                // Start and end byte range
                if (rangeParts[0].length() > 9 || rangeParts[1].length() > 9)
                    return null;
                startByte = Integer.parseInt(rangeParts[0]);
                endByte = Math.min(Integer.parseInt(rangeParts[1]), (int) fileSize - 1);
            }
        } catch (RuntimeException e) {
            return null;
        }

        if (startByte > endByte) return null;
        if (startByte >= (int) fileSize) return null;
        return List.of(startByte, endByte);
    }
}
