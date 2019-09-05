package com.gs.bustrack.auth.controllers;

import com.gs.bustrack.auth.domain.Role;
import com.gs.bustrack.auth.domain.RoleName;
import com.gs.bustrack.auth.dto.ApiResponse;
import com.gs.bustrack.auth.dto.Token;
import com.gs.bustrack.auth.dto.LoginRequest;
import com.gs.bustrack.auth.dto.SignUpRequest;
import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import com.gs.bustrack.auth.ex.AppException;
import com.gs.bustrack.auth.ex.SigninException;
import com.gs.bustrack.auth.ex.SignupException;
import com.gs.bustrack.auth.repositories.RoleRepository;
import com.gs.bustrack.auth.repositories.VerificationTokenRepository;
import com.gs.bustrack.auth.security.JwtTokenProvider;
import com.gs.bustrack.auth.services.OnRegistrationCompleteEvent;
import com.gs.bustrack.auth.services.UserService;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

/**
 *
 * @author Carlos Juarez
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

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
    public Token authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LOG.debug("signing user {}", loginRequest.getUsernameOrEmail());
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    ));
        } catch (BadCredentialsException ex) {
            throw new SigninException("Bad credentials");
        }
        Optional<User> userFound = userService.findByUsername(loginRequest.getUsernameOrEmail());
        if (userFound.isPresent() && userFound.get().isEnabled() == false) {
            throw new SigninException(String.format("User %s is not enabled.", userFound.get().getName()));
        }
        userFound = userService.findByEmail(loginRequest.getUsernameOrEmail());
        if (userFound.isPresent() && userFound.get().isEnabled() == false) {
            throw new SigninException(String.format("User %s is not enabled.", userFound.get().getName()));
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return new Token(jwt);
    }

    /**
     * Register a new user
     *
     * @param signUpRequest
     * @param request
     * @return
     */
    @PostMapping("/signup")
    public User registerUser(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletRequest request) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            throw new SignupException("Username [%s] is already taken.", signUpRequest.getUsername());
        }
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            throw new SignupException("Email address [%s] is already in use.", signUpRequest.getEmail());
        }
        // Creating user's account
        User user = User.builder()
                .name(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .serviceId(signUpRequest.getServiceId())
                .build();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.setRoles(Collections.singleton(userRole));
        User registered = userService.save(user);
        String baseUrl = String.format("%s://%s:%d/api/auth/confirm/email", request.getScheme(), request.getServerName(),
                request.getServerPort());
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered,
                request.getLocale(), baseUrl));
        return registered;
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
