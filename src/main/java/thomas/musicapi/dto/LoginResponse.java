package thomas.musicapi.dto;

import org.springframework.security.core.userdetails.UserDetails;
import thomas.musicapi.model.User;

public record LoginResponse(UserDetails user, String token) {
}
