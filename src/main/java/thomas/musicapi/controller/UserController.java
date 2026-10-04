package thomas.musicapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import thomas.musicapi.dto.LoginRequest;
import thomas.musicapi.dto.AuthResponse;
import thomas.musicapi.dto.SignupRequest;
import thomas.musicapi.model.User;
import thomas.musicapi.service.JwtService;
import thomas.musicapi.service.UserService;

import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping("/auth/signup")
    AuthResponse signup(@Validated @RequestBody SignupRequest signupRequest)
    {
        return userService.signup(signupRequest);
    }

    @PostMapping("/auth/login")
    AuthResponse login(@Validated @RequestBody LoginRequest loginRequest)
    {
        return userService.login(loginRequest);
    }

    @PostMapping("{username}/follow")
    ResponseEntity<String> followUser(@PathVariable String username)
    {
        userService.follow(username);
        return ResponseEntity.ok().body("You are now follow this user");
    }

    @PostMapping("{username}/unfollow")
    ResponseEntity<String> unfollowUser(@PathVariable String username)
    {
        userService.unfollow(username);
        return ResponseEntity.ok().body("You are no longer follow this user");
    }

    @GetMapping("{username}/followers")
    Set<User> getFollowers(@PathVariable String username)
    {
        return userService.getFollowers(username);
    }

    @GetMapping("{username}/followings")
    Set<User> getFollowings(@PathVariable String username)
    {
        return userService.getFollowings(username);
    }
}
