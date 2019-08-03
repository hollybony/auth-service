package com.gs.bustrack.auth.repositories;

import com.gs.bustrack.auth.domain.Role;
import com.gs.bustrack.auth.domain.RoleName;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author Carlos Juarez
 */
public interface RoleRepository extends CrudRepository<Role, Long> {
    
    Optional<Role> findByName(RoleName roleName);
}
