package thomas.musicapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import thomas.musicapi.dto.CreatePlaylistRequest;
import thomas.musicapi.dto.UpdatePlaylistRequest;
import thomas.musicapi.model.Playlist;
import thomas.musicapi.service.PlaylistService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {
    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("")
    Playlist createPlaylist(@RequestBody CreatePlaylistRequest createPlayListRequest) {
        return playlistService.createPlaylist(createPlayListRequest);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.ok().body(Map.of("message", "Playlist deleted successfully"));
    }

    @GetMapping("")
    List<Playlist> getAllPlaylists()
    {
        return playlistService.getAllPlaylist();
    }

    @GetMapping("/{id}")
    Playlist getPlaylist(@PathVariable Long id)
    {
        return playlistService.getPlaylist(id);
    }

    @PatchMapping("/{id}")
    Playlist updatePlaylist(@PathVariable Long id, @RequestBody UpdatePlaylistRequest updatePlaylistRequest)
    {
        return playlistService.updatePlaylist(id, updatePlaylistRequest);
    }
}
