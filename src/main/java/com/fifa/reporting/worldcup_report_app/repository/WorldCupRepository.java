package com.fifa.reporting.worldcup_report_app.repository;

import com.fifa.reporting.worldcup_report_app.entity.WorldCup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorldCupRepository extends JpaRepository<WorldCup, Long> {

    Optional<WorldCup> findByYear(Integer year);

    List<WorldCup> findByChampionIgnoreCase(String champion);

    List<WorldCup> findByHostCountryIgnoreCase(String hostCountry);

    boolean existsByYear(Integer year);

    boolean existsByYearAndIdNot(Integer year, Long id);
}
