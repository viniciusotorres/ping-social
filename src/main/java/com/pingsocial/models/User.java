package com.pingsocial.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "users_tb")
@Entity(name = "user")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_roles_tb",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    private Double latitude;

    private Double longitude;

    private LocalDateTime ultimoLogin;

    private LocalDateTime dataDeRegistro;


    public User() {
    }

    public LocalDateTime getDataDeRegistro() {
        return dataDeRegistro;
    }

    public void setDataDeRegistro(LocalDateTime dataDeRegistro) {
        this.dataDeRegistro = dataDeRegistro;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public User(Long id, String email, String password, List<Role> roles, Double latitude, Double longitude, LocalDateTime ultimoLogin, LocalDateTime dataDeRegistro) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ultimoLogin = ultimoLogin;
        this.dataDeRegistro = dataDeRegistro;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Long getId() {
        return id;
    }
}
