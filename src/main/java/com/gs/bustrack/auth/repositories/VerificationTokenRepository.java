package com.gs.bustrack.auth.repositories;

import com.gs.bustrack.auth.domain.User;
import com.gs.bustrack.auth.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Carlos Juarez
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
 
    VerificationToken findByToken(String token);
 
    VerificationToken findByUser(User user);
}

