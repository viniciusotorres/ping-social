package com.pingsocial.repository;

import com.pingsocial.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAll(Pageable pageable);
    Page<Post> findByAuthor_Id(Long authorId, Pageable pageable);
    Page<Post> findByTribes_IdIn(Set<Long> tribeIds, Pageable pageable);
    Page<Post> findByAuthor_IdIn(List<Long> authorIds, Pageable pageable);
}