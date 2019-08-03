package com.gs.bustrack.auth.repositories;

import com.gs.bustrack.auth.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author Carlos Juarez
 */
public interface UserRepository extends CrudRepository<User, Long> {
    
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByIdIn(List<Long> userIds);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
