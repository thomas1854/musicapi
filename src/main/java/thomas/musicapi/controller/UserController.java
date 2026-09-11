package thomas.musicapi.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import thomas.musicapi.dto.LoginRequest;
import thomas.musicapi.dto.AuthResponse;
import thomas.musicapi.dto.SignupRequest;
import thomas.musicapi.model.User;
import thomas.musicapi.service.UserService;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    AuthResponse signup(@Validated @RequestBody SignupRequest signupRequest)
    {
        return userService.signup(signupRequest);
    }

    @PostMapping("/login")
    AuthResponse login(@Validated @RequestBody LoginRequest loginRequest)
    {
        return userService.login(loginRequest);
    }
}
