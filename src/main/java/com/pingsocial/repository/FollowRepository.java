package com.pingsocial.repository;

import com.pingsocial.models.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    List<Follow> findByFollowedId(Long followedId);

    List<Follow> findByFollowerId(Long followerId);

}