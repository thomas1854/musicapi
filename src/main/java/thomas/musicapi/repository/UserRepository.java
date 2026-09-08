package thomas.musicapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import thomas.musicapi.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    User findByUsername(String username);
}
