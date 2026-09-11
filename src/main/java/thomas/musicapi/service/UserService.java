package thomas.musicapi.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import thomas.musicapi.dto.LoginRequest;
import thomas.musicapi.dto.AuthResponse;
import thomas.musicapi.dto.SignupRequest;
import thomas.musicapi.exception.UsernameExistsException;
import thomas.musicapi.model.User;
import thomas.musicapi.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    private User signupRequestToUser(SignupRequest signupRequest) {
        User user = new User();
        LocalDateTime currentDateTime = LocalDateTime.now();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEmail(signupRequest.getEmail());
        user.setUserRole("USER");
        user.setCreatedAt(currentDateTime);
        return user;
    }

    public AuthResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername()))
            throw new UsernameExistsException("this username is already used");
        User userRequest = signupRequestToUser(signupRequest);
        User savedUser = userRepository.save(userRequest);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        var jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(loginRequest.getUsername());
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }
}
