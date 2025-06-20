package com.pingsocial.repository;

import com.pingsocial.models.Role;
import com.pingsocial.models.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(String name);
}
