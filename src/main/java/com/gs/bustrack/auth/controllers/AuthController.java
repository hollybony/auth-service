package com.gs.bustrack.auth.controllers;

import com.gs.bustrack.auth.domain.Role;
import com.gs.bustrack.auth.domain.RoleName;
import com.gs.bustrack.auth.dto.ApiResponse;
import com.gs.bustrack.auth.dto.JwtAuthResponse;
import com.gs.bustrack.auth.dto.LoginRequest;
import com.gs.bustrack.auth.dto.SignUpRequest;
import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import com.gs.bustrack.auth.ex.AppException;
import com.gs.bustrack.auth.repositories.RoleRepository;
import com.gs.bustrack.auth.repositories.VerificationTokenRepository;
import com.gs.bustrack.auth.security.JwtTokenProvider;
import com.gs.bustrack.auth.services.OnRegistrationCompleteEvent;
import com.gs.bustrack.auth.services.UserService;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 * @author Carlos Juarez
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    
    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/confirm/email")
    public String confirmRegistration(WebRequest request, @RequestParam("token") String token) {
        VerificationToken verificationToken = userService.getVerificationToken(token);
        if (verificationToken == null) {
            throw new RuntimeException("There is such a token.");
        }
        User user = verificationToken.getUser();
        Instant now = Instant.now();
        if ((verificationToken.getExpiryDate().getEpochSecond() - now.getEpochSecond()) <= 0) {
            throw new RuntimeException("The token is expired");
        }
        user.setEnabled(true);
        userService.save(user);
        return String.format("Email %s has been verified.", user.getEmail());
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthResponse(jwt));
    }

    /**
     * Register a new user
     * 
     * @param signUpRequest
     * @param request
     * @return 
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletRequest request) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(ApiResponse.builder()
                    .success(false)
                    .message("Username is already taken!").build(),
                    HttpStatus.BAD_REQUEST);
        }
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(ApiResponse.builder()
                    .success(false)
                    .message("Email Address already in use!").build(),
                    HttpStatus.BAD_REQUEST);
        }
        // Creating user's account
        User user = User.builder()
                .name(signUpRequest.getName())
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword())).build();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.setRoles(Collections.singleton(userRole));
        User registered = userService.save(user);
        String baseUrl = String.format("%s://%s:%d/api/auth/confirm/email",request.getScheme(), request.getServerName(),
                request.getServerPort());
        //String appUrl = request.getLocalAddr();
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered,
                request.getLocale(), baseUrl));
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(registered.getUsername()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.builder()
                .success(true)
                .message("User registered successfully").build());
    }
    
    @GetMapping("/tokens")
    public Iterable<VerificationToken> getTokens() {
        return tokenRepository.findAll();
    }

    @GetMapping("/users")
    public Iterable<User> getUserProfile() {
        return userService.findAllUsers();
    }
}
