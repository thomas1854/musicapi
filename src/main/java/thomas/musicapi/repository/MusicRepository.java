package thomas.musicapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.musicapi.model.Music;

public interface MusicRepository extends JpaRepository<Music, Long> {
}
