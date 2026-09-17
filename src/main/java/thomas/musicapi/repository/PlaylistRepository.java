package thomas.musicapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.musicapi.model.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
