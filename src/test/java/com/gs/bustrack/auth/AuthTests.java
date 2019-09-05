package com.gs.bustrack.auth;

import com.gs.bustrack.auth.domain.RoleName;
import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.dto.ApiResponse;
import com.gs.bustrack.auth.dto.Token;
import com.gs.bustrack.auth.dto.LoginRequest;
import com.gs.bustrack.auth.dto.SignUpRequest;
import com.gs.bustrack.auth.repositories.RoleRepository;
import com.gs.bustrack.auth.repositories.UserRepository;
import com.gs.bustrack.auth.repositories.VerificationTokenRepository;
import java.util.Collections;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author Carlos Juarez
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServiceApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthTests {

    /**
     * Tax code repository instance.
     */
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * Random port
     */
    @LocalServerPort
    private int port;

    /**
     * rest template instance.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void init() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void signup() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("user-carlos");
        request.setEmail("carlos@gmail.com");
        request.setPassword("the-Passa");
        request.setServiceId("10-20-30");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<User> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signup",
                new HttpEntity<>(request, headers), User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Optional<User> userGotten = userRepository.findByEmail("carlos@gmail.com");
        assertTrue(userGotten.isPresent());
        assertEquals("user-carlos", userGotten.get().getName());
        assertEquals("10-20-30", userGotten.get().getServiceId());
    }

    @Test
    public void sigin() {
        User user = User.builder()
                .name("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("thePass"))
                .enabled(true)
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).get()));
        userRepository.save(user);
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Token> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), Token.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void signInBadCredentials() {
        User user = User.builder()
                .name("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("thePass"))
                .enabled(true)
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).get()));
        userRepository.save(user);
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("non-carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Token> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), Token.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void signInDisabledUser() {
        User user = User.builder()
                .name("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("thePass"))
                .enabled(false)
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).get()));
        userRepository.save(user);
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Token> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), Token.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getIntoAutorized() {
        User user = User.builder()
                .name("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("the-Pass"))
                .enabled(true)
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).get()));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("the-Pass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Token> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), Token.class);
        Token token = response.getBody();
        assertNotNull(token);
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<User[]> lastResponse = restTemplate.exchange("http://localhost:" + port + "/api/users",
                HttpMethod.GET, entity, User[].class);
        assertEquals(HttpStatus.OK, lastResponse.getStatusCode());
        User[] users = lastResponse.getBody();
        assertNotNull(users);
        assertEquals(1, users.length);
    }

    @Test
    public void refusedAutorized() {
        User user = User.builder()
                .name("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("the-Pass"))
                .enabled(true)
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_ADMIN).get()));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("the-Pass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Token> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), Token.class);
        Token token = response.getBody();
        assertNotNull(token);
        headers.set("Authorization", "Bearer " + token.getAccessToken());
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<Object> lastResponse = restTemplate.exchange("http://localhost:" + port + "/api/users",
                HttpMethod.GET, entity, Object.class);
        assertEquals(HttpStatus.FORBIDDEN, lastResponse.getStatusCode());
    }
}
