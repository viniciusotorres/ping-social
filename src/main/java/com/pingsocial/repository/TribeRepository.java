package com.pingsocial.repository;

import com.pingsocial.models.Tribe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TribeRepository extends JpaRepository<Tribe, Long> {
    boolean existsByName(String name);
}
