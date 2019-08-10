package com.gs.bustrack.auth.controllers;

import com.gs.bustrack.auth.domain.User;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.gs.bustrack.auth.services.UserService;

/**
 * 
 * @author Carlos Juarez
 */
@RestController
@RequestMapping("/auth")
public class ResourceController {
    
    @Autowired
    private UserService userService;

    @RequestMapping(value ="/cities")
    @PreAuthorize("hasAuthority('ADMIN_USER') or hasAuthority('STANDARD_USER')")
    public List<String> getUser(){
        return Arrays.asList("Patagonia", "Huasteca", "La laguna");
    }

    @RequestMapping(value ="/users", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('ADMIN_USER')")
    public List<User> getUsers(){
        return userService.findAllUsers();
    }
}
