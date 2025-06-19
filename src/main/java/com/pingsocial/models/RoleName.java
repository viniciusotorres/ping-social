package com.pingsocial.models;

public enum RoleName {

    ROLE_USER,
    ROLE_ADMIN;

    public String getAuthority() {
        return name();
    }
}
