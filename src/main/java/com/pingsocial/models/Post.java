package com.pingsocial.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Table(name = "posts_tb")
@Entity(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "tribe_id", nullable = true)
    private Tribe tribe;

    private String content;

    private LocalDateTime createdAt;

    public Post() {
    }

    public Post(Long id, User author, Tribe tribe, String content, LocalDateTime createdAt) {
        this.id = id;
        this.author = author;
        this.tribe = tribe;
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

    public Tribe getTribe() {
        return tribe;
    }

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
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
