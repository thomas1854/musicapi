package thomas.musicapi.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import thomas.musicapi.dto.AddDeleteMusicPlaylistRequest;
import thomas.musicapi.dto.CreatePlaylistRequest;
import thomas.musicapi.dto.UpdatePlaylistRequest;
import thomas.musicapi.exception.ResourceAccessDeniedException;
import thomas.musicapi.exception.ResourceNotFoundException;
import thomas.musicapi.model.Music;
import thomas.musicapi.model.Playlist;
import thomas.musicapi.model.User;
import thomas.musicapi.repository.MusicRepository;
import thomas.musicapi.repository.PlaylistRepository;
import thomas.musicapi.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final MusicRepository musicRepository;

    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository, MusicRepository musicRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.musicRepository = musicRepository;
    }

    private Playlist createPlaylistRequestToPlaylist(CreatePlaylistRequest createPlaylistRequest, User user)
    {
        Playlist playlist = new Playlist();
        playlist.setTitle(createPlaylistRequest.title());
        playlist.setDescription(createPlaylistRequest.description());
        playlist.setCreatedAt(LocalDateTime.now());
        playlist.setUpdatedAt(LocalDateTime.now());
        playlist.setUser(user);
        return playlist;
    }
    public Playlist createPlaylist(CreatePlaylistRequest createPlaylistRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");
        User user = userRepository.findByUsername(authentication.getName());
        Playlist playlist = createPlaylistRequestToPlaylist(createPlaylistRequest, user);
        return playlistRepository.save(playlist);
    }

    public void deletePlaylist(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");
        Playlist playlist = playlistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("playlist not found"));

        if (!playlist.getUser().getUsername().equals(authentication.getName()))
            throw new ResourceAccessDeniedException("You do not have access to delete this resource");

        playlistRepository.deleteById(id);
    }

    public List<Playlist> getAllPlaylist() {
        return playlistRepository.findAll();
    }


    public Playlist getPlaylist(Long id) {
        return playlistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("playlist not found"));
    }

    @Transactional
    public Playlist updatePlaylist(Long id, UpdatePlaylistRequest updatePlaylistRequest)
    {
        Playlist playlist = playlistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        if(!playlist.getUser().getUsername().equals(authentication.getName()))
            throw new ResourceAccessDeniedException("You do not have access to update this resource");

        if (updatePlaylistRequest != null && updatePlaylistRequest.title() != null)
            playlist.setTitle(updatePlaylistRequest.title());
        if (updatePlaylistRequest != null && updatePlaylistRequest.description() != null)
            playlist.setDescription(updatePlaylistRequest.description());
        playlist.setUpdatedAt(LocalDateTime.now());
        return playlist;
    }

    @Transactional
    public void addMusicPlaylist(Long playlistId, AddDeleteMusicPlaylistRequest addDeleteMusicPlaylistRequest) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new ResourceNotFoundException("playlist not found"));
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        if(!playlist.getUser().getUsername().equals(authentication.getName()))
            throw new ResourceAccessDeniedException("You do not have access to update this resource");

        Long[] musicIds = addDeleteMusicPlaylistRequest.musicIds();
        for (Long musicId : musicIds)
        {
            Music music = musicRepository.findById(musicId).orElseThrow(() -> new ResourceNotFoundException("music with id " + musicId + " not found"));
            playlist.getMusics().add(music);
        }
    }

    @Transactional
    public void deleteMusicPlaylist(Long playlistId, AddDeleteMusicPlaylistRequest addDeleteMusicPlaylistRequest) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> new ResourceNotFoundException("playlist not found"));

        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        if(!playlist.getUser().getUsername().equals(authentication.getName()))
            throw new ResourceAccessDeniedException("You do not have access to update this resource");

        Long[] musicIds = addDeleteMusicPlaylistRequest.musicIds();
        for (Long musicId : musicIds)
        {
            Music music = musicRepository.findById(musicId).orElseThrow(() -> new ResourceNotFoundException("music with id " + musicId + " not found"));
            playlist.getMusics().remove(music);
        }
    }
}
