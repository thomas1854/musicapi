package thomas.musicapi.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import thomas.musicapi.dto.UpdateMusicRequest;
import thomas.musicapi.dto.UploadMusicRequest;
import thomas.musicapi.exception.ResourceAccessDeniedException;
import thomas.musicapi.exception.ResourceNotFoundException;
import thomas.musicapi.model.Music;
import thomas.musicapi.model.User;
import thomas.musicapi.repository.MusicRepository;
import thomas.musicapi.repository.UserRepository;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MusicService {
    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    @Value("${music.bucket}")
    String bucket;
    public MusicService(S3Client s3Client, UserRepository userRepository, MusicRepository musicRepository) {
        this.s3Client = s3Client;
        this.userRepository = userRepository;
        this.musicRepository = musicRepository;
    }

    private Music uploadMusicRequestToMusic(UploadMusicRequest uploadMusicRequest, MultipartFile multipartFile, User user, String storageKey)
    {
        Music music = new Music();
        music.setTitle(uploadMusicRequest.title());
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");
        User user = userRepository.findByUsername(authentication.getName());

        String storageKey = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(storageKey).contentType(multipartFile.getContentType()).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

        Music music = uploadMusicRequestToMusic(uploadMusicRequest, multipartFile, user, storageKey);
        System.out.println(storageKey);
        return musicRepository.save(music);
    }

    public void deleteMusic(Long id)  {
        Music music = musicRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        if(!music.getUser().getUsername().equals(authentication.getName()))
            throw new ResourceAccessDeniedException("You do not have access to delete this resource");

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(music.getStorageKey())
                .build();

        s3Client.deleteObject(request);
        musicRepository.delete(music);
    }

    @Transactional
    public Music updateMusic(Long id, UpdateMusicRequest updateMusicRequest, MultipartFile multipartFile) throws IOException {
        Music music = musicRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Music not found"));

        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        if(!music.getUser().getUsername().equals(authentication.getName()))
            throw new ResourceAccessDeniedException("You do not have access to delete this resource");

        if (updateMusicRequest != null && updateMusicRequest.title() != null)
            music.setTitle(updateMusicRequest.title());

        if (multipartFile != null)
        {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(music.getStorageKey())
                    .build();
            s3Client.deleteObject(request);

            String newStorageKey = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(newStorageKey).contentType(multipartFile.getContentType()).build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            music.setUpdatedAt(LocalDateTime.now());
            music.setStorageKey(newStorageKey);
            music.setFileSize(multipartFile.getSize());
            music.setDuration(10L);
            music.setMimeType(multipartFile.getContentType());
            music.setFileName(multipartFile.getOriginalFilename());
        }

        return music;
    }
}
