package com.gs.bustrack.auth.services;


import java.util.List;
import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import java.util.Optional;

/**
 * Carlos Juarez
 */
public interface UserService {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
            
    List<User> findAllUsers();
    
    VerificationToken getVerificationToken(String VerificationToken);
    
    VerificationToken createVerificationToken(User user, String token);
    
    User save(User user);
}
