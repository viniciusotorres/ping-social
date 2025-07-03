package com.pingsocial.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table(name = "posts_tb")
@Entity(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToMany
    @JoinTable(
            name = "post_tribes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tribe_id")
    )
    private Set<Tribe> tribes = new HashSet<>();

    private String content;

    private LocalDateTime createdAt;

    public Post() {
    }

    public Post(Long id, User author, Set<Tribe> tribes, String content, LocalDateTime createdAt) {
        this.id = id;
        this.author = author;
        this.tribes = tribes;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Set<Tribe> getTribes() {
        return tribes;
    }

    public void setTribes(Set<Tribe> tribes) {
        this.tribes = tribes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }




}
