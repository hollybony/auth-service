package com.gs.bustrack.auth.services;


import java.util.List;
import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;

/**
 * Carlos Juarez
 */
public interface UserService {
    
    User findByUsername(String username);
    
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
            
    List<User> findAllUsers();
    
    VerificationToken getVerificationToken(String VerificationToken);
    
    VerificationToken createVerificationToken(User user, String token);
    
    User save(User user);
}
