package com.fifa.reporting.worldcup_report_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_cup_id", nullable = false)
    private WorldCup worldCup;

    @Column(name = "team1", nullable = false)
    private String team1;

    @Column(name = "team2", nullable = false)
    private String team2;

    @Column(name = "team1_goals")
    private Integer team1Goals;

    @Column(name = "team2_goals")
    private Integer team2Goals;

    @Column(name = "stage", nullable = false)
    private String stage;

    @Column(name = "stadium")
    private String stadium;

    @Column(name = "attendance")
    private Integer attendance;

    @Column(name = "match_date")
    private LocalDateTime matchDate;
}
