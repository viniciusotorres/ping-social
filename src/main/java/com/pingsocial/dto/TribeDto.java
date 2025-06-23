package com.pingsocial.dto;

import com.pingsocial.models.Tribe;

/**
 * DTO para representar uma tribo na API.
 */
public class TribeDto {

    private Long id;
    private String name;
    private String description;
    private int memberCount;

    public TribeDto() {
    }

    public TribeDto(Long id, String name, String description, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
    }

    /**
     * Cria um DTO a partir de uma entidade Tribe.
     *
     * @param tribe Entidade Tribe
     * @return DTO TribeDto
     */
    public static TribeDto fromEntity(Tribe tribe) {
        return new TribeDto(
                tribe.getId(),
                tribe.getName(),
                tribe.getDescription(),
                tribe.getMembers().size()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }
}