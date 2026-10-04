package thomas.musicapi.service;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Set;

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
        user.setUsername(signupRequest.username());
        user.setPassword(passwordEncoder.encode(signupRequest.password()));
        user.setEmail(signupRequest.email());
        user.setUserRole("USER");
        user.setCreatedAt(currentDateTime);
        return user;
    }

    public AuthResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.username()))
            throw new UsernameExistsException("this username is already used");
        User userRequest = signupRequestToUser(signupRequest);
        User savedUser = userRepository.save(userRequest);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        var jwtToken = jwtService.generateToken(userDetails);
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
        CustomUserDetails user = (CustomUserDetails) userDetailsService.loadUserByUsername(loginRequest.username());
        var jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    @Transactional
    public void follow(String username) {
        User followedUser = userRepository.findByUsername(username);
        if (followedUser == null)
            throw new UsernameNotFoundException("followed user not found");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        User followingUser = userRepository.findByUsername(authentication.getName());
        if (followingUser == null)
            throw new UsernameNotFoundException("Authentication required");

        if (followingUser.equals(followedUser))
            throw new IllegalArgumentException("You cannot follow yourself");

        if (followingUser.getFollowing().contains(followedUser))
            throw new IllegalArgumentException("Already following this user");

        followingUser.getFollowing().add(followedUser);
    }

    public Set<User> getFollowers(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("username not found");
        return user.getFollowedBy();
    }

    public Set<User> getFollowings(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("username not found");
        return user.getFollowing();
    }

    @Transactional
    public void unfollow(String username) {
        User followedUser = userRepository.findByUsername(username);
        if (followedUser == null)
            throw new UsernameNotFoundException("followed user not found");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new UsernameNotFoundException("Authentication required");

        User followingUser = userRepository.findByUsername(authentication.getName());
        if (followingUser == null)
            throw new UsernameNotFoundException("Authentication required");

        if (followingUser.equals(followedUser))
            throw new IllegalArgumentException("You cannot unfollow yourself");

        if (!followingUser.getFollowing().contains(followedUser))
            throw new IllegalArgumentException("You are not following this user");

        followingUser.getFollowing().remove(followedUser);
    }
}
