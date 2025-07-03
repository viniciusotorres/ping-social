package com.pingsocial.models;

public enum PostFilterType {

    ALL("Todos"),
    MY_POSTS("Meus Posts"),
    TRIBE_POSTS("Posts da Minha Tribo"),
    FRIENDS_POSTS("Posts dos Meus Amigos");

    private final String description;

    PostFilterType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
