package com.fifa.reporting.worldcup_report_app.repository;

import com.fifa.reporting.worldcup_report_app.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByTeam1OrTeam2(String team1, String team2);

    boolean existsByWorldCup_Id(Long worldCupId);
}
