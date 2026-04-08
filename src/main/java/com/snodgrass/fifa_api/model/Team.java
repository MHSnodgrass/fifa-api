package com.snodgrass.fifa_api.model;

import com.snodgrass.fifa_api.model.converter.TeamPlayerListConverter;
import com.snodgrass.fifa_api.model.enums.Group;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_name", nullable = false)
    private String countryName;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_letter", nullable = false)
    private Group groupLetter;

    @Column(name = "squad", columnDefinition = "JSON")
    @Convert(converter = TeamPlayerListConverter.class)
    private List<TeamPlayer> squad;

    @Embedded
    private TeamStats stats;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private String flagUrl;
    private String logoUrl;
    private Integer fifaRanking;
    private String managerName;
}
