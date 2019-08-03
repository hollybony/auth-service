package com.gs.bustrack.auth.services;


import java.util.List;
import com.gs.bustrack.auth.domain.User;

/**
 * Created by nydiarra on 06/05/17.
 */
public interface GenericService {
    
    User findByUsername(String username);

    List<User> findAllUsers();
}
