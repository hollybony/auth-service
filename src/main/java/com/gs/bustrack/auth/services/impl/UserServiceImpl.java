package com.gs.bustrack.auth.services.impl;

import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import com.gs.bustrack.auth.repositories.UserRepository;
import com.gs.bustrack.auth.repositories.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import com.gs.bustrack.auth.services.UserService;

/**
 * @author Carlos Juarez
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    @Override
    public boolean existsByUsername(String username){
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return (List<User>)userRepository.findAll();
    }
    
    @Override
    public VerificationToken getVerificationToken(String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    @Override
    public VerificationToken createVerificationToken(User user, String rawToken) {
        VerificationToken token = VerificationToken.builder().token(rawToken).user(user).build();
        return tokenRepository.save(token);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
