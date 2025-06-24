package com.pingsocial.dto;

import com.pingsocial.models.User;

/**
 * DTO para representar um usuário na API.
 */
public class UserDto {

    private Long id;
    private String email;
    private String nickname;
    private Double latitude;
    private Double longitude;
    private int tribeCount;

    public UserDto() {
    }

    public UserDto(Long id, String email, Double latitude, Double longitude, int tribeCount, String nickname) {
        this.id = id;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tribeCount = tribeCount;
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Cria um DTO a partir de uma entidade User.
     *
     * @param user Entidade User
     * @return DTO UserDto
     */
    public static UserDto fromEntity(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getLatitude(),
                user.getLongitude(),
                user.getTribes().size(),
                user.getNickname()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getTribeCount() {
        return tribeCount;
    }

    public void setTribeCount(int tribeCount) {
        this.tribeCount = tribeCount;
    }

}