package com.gs.bustrack.auth;

import com.gs.bustrack.auth.domain.RoleName;
import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.dto.ApiResponse;
import com.gs.bustrack.auth.dto.JwtAuthResponse;
import com.gs.bustrack.auth.dto.LoginRequest;
import com.gs.bustrack.auth.dto.SignUpRequest;
import com.gs.bustrack.auth.repositories.RoleRepository;
import com.gs.bustrack.auth.repositories.UserRepository;
import java.util.Collections;
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
        userRepository.deleteAll();
    }

    @Test
    public void signup() {
        SignUpRequest request = new SignUpRequest();
        request.setName("Carlos");
        request.setUsername("user-carlos");
        request.setEmail("carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signup",
                new HttpEntity<>(request, headers), ApiResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(userRepository.findByEmail("carlos@gmail.com").isPresent());
    }

    @Test
    public void sigin() {
        User user = User.builder()
                .name("Carlos")
                .username("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("thePass"))
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).get()));
        userRepository.save(user);
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JwtAuthResponse> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), JwtAuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void getIntoAutorized() {
        User user = User.builder()
                .name("Carlos")
                .username("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("thePass"))
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_USER).get()));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JwtAuthResponse> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), JwtAuthResponse.class);
        JwtAuthResponse token = response.getBody();

        headers.set("Authorization", "Bearer " + token.getAccessToken());
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<User[]> lastResponse = restTemplate.exchange("http://localhost:" + port + "/api/users",
                HttpMethod.GET, entity, User[].class);
        assertEquals(HttpStatus.OK, lastResponse.getStatusCode());
        assertEquals(1, lastResponse.getBody().length);
    }

    @Test
    public void refusedAutorized() {
        User user = User.builder()
                .name("Carlos")
                .username("user-carlos")
                .email("carlos@gmail.com")
                .password(passwordEncoder.encode("thePass"))
                .build();
        user.setRoles(Collections.singleton(roleRepository.findByName(RoleName.ROLE_ADMIN).get()));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("carlos@gmail.com");
        request.setPassword("thePass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<JwtAuthResponse> response = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/signin",
                new HttpEntity<>(request, headers), JwtAuthResponse.class);
        JwtAuthResponse token = response.getBody();

        headers.set("Authorization", "Bearer " + token.getAccessToken());
        //headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>("body", headers);

        ResponseEntity<Object> lastResponse = restTemplate.exchange("http://localhost:" + port + "/api/users",
                HttpMethod.GET, entity, Object.class);
        assertEquals(HttpStatus.FORBIDDEN, lastResponse.getStatusCode());
    }

}
