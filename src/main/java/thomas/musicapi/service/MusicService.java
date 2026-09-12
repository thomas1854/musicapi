package thomas.musicapi.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import thomas.musicapi.dto.UploadMusicRequest;
import thomas.musicapi.model.Music;
import thomas.musicapi.model.User;
import thomas.musicapi.repository.MusicRepository;
import thomas.musicapi.repository.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MusicService {
    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final MusicRepository musicRepository;

    public MusicService(S3Client s3Client, UserRepository userRepository, MusicRepository musicRepository) {
        this.s3Client = s3Client;
        this.userRepository = userRepository;
        this.musicRepository = musicRepository;
    }

    private Music uploadMusicRequestToMusic(UploadMusicRequest uploadMusicRequest, MultipartFile multipartFile, User user, String storageKey)
    {
        Music music = new Music();
        music.setTitle(uploadMusicRequest.getTitle());
        music.setUser(user);
        music.setCreatedAt(LocalDateTime.now());
        music.setUpdatedAt(LocalDateTime.now());
        music.setStorageKey(storageKey);
        music.setFileSize(multipartFile.getSize());
        music.setDuration(10L);
        music.setMimeType(multipartFile.getContentType());
        music.setFileName(multipartFile.getOriginalFilename());
        return music;
    }

    public Music uploadMusic(UploadMusicRequest uploadMusicRequest, MultipartFile multipartFile) throws IOException
    {
        String content = multipartFile.getContentType();
        if (content == null || !content.startsWith("audio/"))
            throw new IllegalArgumentException("File must be audio");

        User user = userRepository.findByUsername(uploadMusicRequest.getUsername());
        if (user != null)
        {
            String storageKey = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
            String bucket = "music";

            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(storageKey).contentType(multipartFile.getContentType()).build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            Music music = uploadMusicRequestToMusic(uploadMusicRequest, multipartFile, user, storageKey);
            System.out.println(storageKey);
            return musicRepository.save(music);
        }
        else
            throw new UsernameNotFoundException("username not found");
    }
}
