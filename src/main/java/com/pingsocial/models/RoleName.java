package com.pingsocial.models;

public enum RoleName {

    ROLE_USER,
    ROLE_ADMIN;

    public String getAuthority() {
        return name();
    }

    public boolean isBlank() {
        return this == null || this.name().isBlank();
    }

    public String toUpperCase() {
        return this.name().toUpperCase();
    }
}
