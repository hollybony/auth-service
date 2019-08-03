package com.gs.bustrack.auth.controllers;

import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping("/users")
    @PreAuthorize("hasRole('USER')")
    public Iterable<User> getUsers() {
        return userRepository.findAll();
    }
}
