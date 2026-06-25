package com.fifa.reporting.worldcup_report_app.service;

import com.fifa.reporting.worldcup_report_app.dto.response.TeamPerformanceDTO;
import com.fifa.reporting.worldcup_report_app.dto.response.TournamentSummaryDTO;
import com.fifa.reporting.worldcup_report_app.entity.Match;
import com.fifa.reporting.worldcup_report_app.entity.WorldCup;
import com.fifa.reporting.worldcup_report_app.repository.MatchRepository;
import com.fifa.reporting.worldcup_report_app.repository.WorldCupRepository;
import com.fifa.reporting.worldcup_report_app.util.TeamFlagResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final MatchRepository matchRepository;
    private final WorldCupRepository worldCupRepository;
    private final ResourceLoader resourceLoader;

    private final Map<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            compileReport("team_performance");
            log.info("Reports compiled and cached successfully");
        } catch (Exception e) {
            log.error("Failed to compile reports", e);
        }
    }

    private void compileReport(String reportName) throws JRException {
        Resource resource = resourceLoader.getResource("classpath:reports/" + reportName + ".jrxml");
        try (InputStream inputStream = resource.getInputStream()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(inputStream);
            reportCache.put(reportName, jasperReport);
            log.info("Compiled report: {}", reportName);
        } catch (Exception e) {
            log.error("Failed to compile report: {}", reportName, e);
            throw new JRException("Failed to compile report: " + reportName, e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] generateTeamPerformanceReport(String teamName) throws JRException {
        log.info("Generating team performance report for: {}", teamName);

        List<Match> matches = matchRepository.findByTeam1OrTeam2(teamName, teamName);

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("No matches found for team: " + teamName);
        }

        List<TeamPerformanceDTO> matchDetails = buildMatchDetails(teamName, matches);
        List<TournamentSummaryDTO> tournaments = buildTournamentSummary(teamName, matches);
        Map<String, Object> parameters = buildReportParameters(teamName, matches, matchDetails, tournaments);

        JasperReport jasperReport = reportCache.get("team_performance");
        if (jasperReport == null) {
            throw new IllegalStateException("Report template not found. Did it compile?");
        }

        // Main data source is a single empty record; tables use subDataset parameters.
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                new JREmptyDataSource(1)
        );
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private Map<String, Object> buildReportParameters(
            String teamName,
            List<Match> matches,
            List<TeamPerformanceDTO> matchDetails,
            List<TournamentSummaryDTO> tournaments) {

        Map<String, Object> stats = calculateTeamStats(teamName, matches);

        stats.put("teamFlagUrl", TeamFlagResolver.teamFlagUrl(teamName));
        stats.put("reportDate", LocalDate.now());
        stats.put("matchDataSource", new JRBeanCollectionDataSource(matchDetails));
        stats.put("tournamentDataSource", new JRBeanCollectionDataSource(tournaments));

        return stats;
    }

    private Map<String, Object> calculateTeamStats(String teamName, List<Match> matches) {
        int totalMatches = matches.size();
        int wins = 0, losses = 0, draws = 0;
        int goalsScored = 0, goalsConceded = 0;

        for (Match match : matches) {
            boolean isTeam1 = match.getTeam1().equals(teamName);
            int teamGoals = isTeam1 ? match.getTeam1Goals() : match.getTeam2Goals();
            int opponentGoals = isTeam1 ? match.getTeam2Goals() : match.getTeam1Goals();

            goalsScored += teamGoals;
            goalsConceded += opponentGoals;

            if (teamGoals > opponentGoals) {
                wins++;
            } else if (teamGoals < opponentGoals) {
                losses++;
            } else {
                draws++;
            }
        }

        Set<Integer> tournamentYears = matches.stream()
                .map(m -> m.getWorldCup().getYear())
                .collect(Collectors.toSet());

        int totalWorldCups = tournamentYears.size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("teamName", teamName);
        stats.put("totalWorldCups", totalWorldCups);
        stats.put("totalMatches", totalMatches);
        stats.put("wins", wins);
        stats.put("losses", losses);
        stats.put("draws", draws);
        stats.put("goalsScored", goalsScored);
        stats.put("goalsConceded", goalsConceded);
        stats.put("goalDifference", goalsScored - goalsConceded);

        return stats;
    }

    private List<TeamPerformanceDTO> buildMatchDetails(String teamName, List<Match> matches) {
        return matches.stream()
                .sorted(Comparator.comparing(Match::getMatchDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(match -> {
                    boolean isTeam1 = match.getTeam1().equals(teamName);
                    String opponent = isTeam1 ? match.getTeam2() : match.getTeam1();
                    int goalsFor = isTeam1 ? match.getTeam1Goals() : match.getTeam2Goals();
                    int goalsAgainst = isTeam1 ? match.getTeam2Goals() : match.getTeam1Goals();

                    String result;
                    if (goalsFor > goalsAgainst) {
                        result = "Win";
                    } else if (goalsFor < goalsAgainst) {
                        result = "Loss";
                    } else {
                        result = "Draw";
                    }

                    LocalDate matchDate = match.getMatchDate() != null
                            ? match.getMatchDate().toLocalDate()
                            : null;

                    return TeamPerformanceDTO.builder()
                            .year(match.getWorldCup().getYear())
                            .matchDate(matchDate)
                            .opponent(opponent)
                            .opponentFlagUrl(TeamFlagResolver.teamFlagUrl(opponent))
                            .goalsFor(goalsFor)
                            .goalsAgainst(goalsAgainst)
                            .result(result)
                            .stage(match.getStage())
                            .stadium(match.getStadium())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TournamentSummaryDTO> buildTournamentSummary(String teamName, List<Match> matches) {
        Set<Long> worldCupIds = matches.stream()
                .map(m -> m.getWorldCup().getId())
                .collect(Collectors.toSet());

        return worldCupRepository.findAll().stream()
                .filter(wc -> worldCupIds.contains(wc.getId()))
                .sorted(Comparator.comparing(WorldCup::getYear).reversed())
                .map(wc -> TournamentSummaryDTO.builder()
                        .year(wc.getYear())
                        .hostCountry(wc.getHostCountry())
                        .tournamentStart(wc.getStartDate())
                        .tournamentEnd(wc.getEndDate())
                        .role(resolveTournamentRole(teamName, wc))
                        .hostFlagUrl(TeamFlagResolver.hostCountryFlagUrl(wc.getHostCountry()))
                        .build())
                .collect(Collectors.toList());
    }

    private String resolveTournamentRole(String teamName, WorldCup wc) {
        if (teamName.equals(wc.getChampion())) {
            return "Champion";
        }
        if (teamName.equals(wc.getRunnerUp())) {
            return "Runner-up";
        }
        return "Participant";
    }
}
