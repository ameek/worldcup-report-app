package com.fifa.reporting.worldcup_report_app.config;

import com.fifa.reporting.worldcup_report_app.entity.Match;
import com.fifa.reporting.worldcup_report_app.entity.WorldCup;
import com.fifa.reporting.worldcup_report_app.repository.MatchRepository;
import com.fifa.reporting.worldcup_report_app.repository.WorldCupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final WorldCupRepository worldCupRepository;
    private final MatchRepository matchRepository;

    @Override
    public void run(String... args) {
        if (matchRepository.count() > 0) {
            log.info("Database already seeded, skipping data initialization");
            return;
        }

        log.info("Seeding World Cup and match data...");

        WorldCup wc2022 = worldCupRepository.save(WorldCup.builder()
                .year(2022)
                .hostCountry("Qatar")
                .champion("Argentina")
                .runnerUp("France")
                .totalTeams(32)
                .totalMatches(64)
                .totalGoals(172)
                .topScorer("Kylian Mbappe")
                .topScorerGoals(8)
                .startDate(LocalDate.of(2022, 11, 20))
                .endDate(LocalDate.of(2022, 12, 18))
                .build());

        WorldCup wc2018 = worldCupRepository.save(WorldCup.builder()
                .year(2018)
                .hostCountry("Russia")
                .champion("France")
                .runnerUp("Croatia")
                .totalTeams(32)
                .totalMatches(64)
                .totalGoals(169)
                .topScorer("Harry Kane")
                .topScorerGoals(6)
                .startDate(LocalDate.of(2018, 6, 14))
                .endDate(LocalDate.of(2018, 7, 15))
                .build());

        WorldCup wc2014 = worldCupRepository.save(WorldCup.builder()
                .year(2014)
                .hostCountry("Brazil")
                .champion("Germany")
                .runnerUp("Argentina")
                .totalTeams(32)
                .totalMatches(64)
                .totalGoals(171)
                .topScorer("James Rodriguez")
                .topScorerGoals(6)
                .startDate(LocalDate.of(2014, 6, 12))
                .endDate(LocalDate.of(2014, 7, 13))
                .build());

        seed2022Matches(wc2022);
        seed2018Matches(wc2018);
        seed2014Matches(wc2014);

        log.info("Seeded {} World Cups and {} matches", worldCupRepository.count(), matchRepository.count());
    }

    private void seed2022Matches(WorldCup wc) {
        saveMatch(wc, "Argentina", "Saudi Arabia", 1, 2, "Group C", "Lusail Stadium", 88012, "2022-11-22");
        saveMatch(wc, "Argentina", "Mexico", 2, 0, "Group C", "Lusail Stadium", 88966, "2022-11-26");
        saveMatch(wc, "Argentina", "Poland", 2, 0, "Group C", "Stadium 974", 44089, "2022-11-30");
        saveMatch(wc, "Argentina", "Australia", 2, 1, "Round of 16", "Ahmad bin Ali Stadium", 45703, "2022-12-03");
        saveMatch(wc, "Argentina", "Netherlands", 2, 2, "Quarter-final", "Lusail Stadium", 88235, "2022-12-09");
        saveMatch(wc, "Argentina", "Croatia", 3, 0, "Semi-final", "Lusail Stadium", 88966, "2022-12-13");
        saveMatch(wc, "Argentina", "France", 3, 3, "Final", "Lusail Stadium", 88966, "2022-12-18");

        saveMatch(wc, "France", "Australia", 4, 1, "Group D", "Al Janoub Stadium", 41676, "2022-11-22");
        saveMatch(wc, "France", "Denmark", 2, 1, "Group D", "Stadium 974", 42049, "2022-11-26");
        saveMatch(wc, "France", "Tunisia", 0, 1, "Group D", "Education City Stadium", 43509, "2022-11-30");
        saveMatch(wc, "France", "Poland", 3, 1, "Round of 16", "Al Thumama Stadium", 44089, "2022-12-04");
        saveMatch(wc, "France", "England", 2, 1, "Quarter-final", "Al Bayt Stadium", 68462, "2022-12-10");
        saveMatch(wc, "France", "Morocco", 2, 0, "Semi-final", "Al Bayt Stadium", 68294, "2022-12-14");

        saveMatch(wc, "Brazil", "Serbia", 2, 0, "Group G", "Lusail Stadium", 88559, "2022-11-24");
        saveMatch(wc, "Brazil", "Switzerland", 1, 0, "Group G", "Stadium 974", 43959, "2022-11-28");
        saveMatch(wc, "Brazil", "Cameroon", 0, 1, "Group G", "Lusail Stadium", 85465, "2022-12-02");
        saveMatch(wc, "Brazil", "South Korea", 4, 1, "Round of 16", "Stadium 974", 44397, "2022-12-05");
        saveMatch(wc, "Brazil", "Croatia", 0, 0, "Quarter-final", "Education City Stadium", 43993, "2022-12-09");
    }

    private void seed2018Matches(WorldCup wc) {
        saveMatch(wc, "France", "Australia", 2, 1, "Group C", "Kazan Arena", 41832, "2018-06-16");
        saveMatch(wc, "France", "Peru", 1, 0, "Group C", "Central Stadium", 32578, "2018-06-21");
        saveMatch(wc, "France", "Denmark", 0, 0, "Group C", "Luzhniki Stadium", 78011, "2018-06-26");
        saveMatch(wc, "France", "Argentina", 4, 3, "Round of 16", "Kazan Arena", 45749, "2018-06-30");
        saveMatch(wc, "France", "Uruguay", 2, 0, "Quarter-final", "Nizhny Novgorod Stadium", 43987, "2018-07-06");
        saveMatch(wc, "France", "Belgium", 1, 0, "Semi-final", "Krestovsky Stadium", 64186, "2018-07-10");
        saveMatch(wc, "France", "Croatia", 4, 2, "Final", "Luzhniki Stadium", 78011, "2018-07-15");

        saveMatch(wc, "Argentina", "Iceland", 1, 1, "Group D", "Spartak Stadium", 44507, "2018-06-16");
        saveMatch(wc, "Argentina", "Croatia", 0, 3, "Group D", "Nizhny Novgorod Stadium", 43983, "2018-06-21");
        saveMatch(wc, "Argentina", "Nigeria", 2, 1, "Group D", "Krestovsky Stadium", 64340, "2018-06-26");

        saveMatch(wc, "Brazil", "Switzerland", 1, 1, "Group E", "Rostov Arena", 43299, "2018-06-17");
        saveMatch(wc, "Brazil", "Costa Rica", 2, 0, "Group E", "Krestovsky Stadium", 64468, "2018-06-22");
        saveMatch(wc, "Brazil", "Serbia", 2, 0, "Group E", "Spartak Stadium", 44551, "2018-06-27");
        saveMatch(wc, "Brazil", "Mexico", 2, 0, "Round of 16", "Samara Arena", 41938, "2018-07-02");
        saveMatch(wc, "Brazil", "Belgium", 1, 2, "Quarter-final", "Kazan Arena", 41837, "2018-07-06");
    }

    private void seed2014Matches(WorldCup wc) {
        saveMatch(wc, "Brazil", "Croatia", 3, 1, "Group A", "Arena Corinthians", 62160, "2014-06-12");
        saveMatch(wc, "Brazil", "Mexico", 0, 0, "Group A", "Estadio Castelao", 63903, "2014-06-17");
        saveMatch(wc, "Brazil", "Cameroon", 4, 1, "Group A", "Estadio Nacional", 69112, "2014-06-23");
        saveMatch(wc, "Brazil", "Chile", 0, 0, "Round of 16", "Estadio Mineirao", 53082, "2014-06-28");
        saveMatch(wc, "Brazil", "Colombia", 2, 1, "Quarter-final", "Estadio Castelao", 64597, "2014-07-04");
        saveMatch(wc, "Brazil", "Germany", 1, 7, "Semi-final", "Estadio Mineirao", 58141, "2014-07-08");

        saveMatch(wc, "Argentina", "Bosnia and Herzegovina", 2, 1, "Group F", "Estadio Maracana", 74193, "2014-06-15");
        saveMatch(wc, "Argentina", "Iran", 1, 0, "Group F", "Estadio Mineirao", 32878, "2014-06-21");
        saveMatch(wc, "Argentina", "Nigeria", 3, 2, "Group F", "Estadio Beira-Rio", 43801, "2014-06-25");
        saveMatch(wc, "Argentina", "Switzerland", 1, 0, "Round of 16", "Estadio Nacional", 74232, "2014-06-01");
        saveMatch(wc, "Argentina", "Belgium", 1, 0, "Quarter-final", "Estadio Nacional", 68911, "2014-07-05");
        saveMatch(wc, "Argentina", "Netherlands", 0, 0, "Semi-final", "Estadio Sao Paulo", 63500, "2014-07-09");
        saveMatch(wc, "Argentina", "Germany", 0, 1, "Final", "Estadio Maracana", 74738, "2014-07-13");

        saveMatch(wc, "France", "Honduras", 3, 0, "Group E", "Estadio Beira-Rio", 43501, "2014-06-15");
        saveMatch(wc, "France", "Switzerland", 5, 2, "Group E", "Arena Fonte Nova", 41351, "2014-06-20");
        saveMatch(wc, "France", "Ecuador", 0, 0, "Group E", "Estadio Maracana", 74193, "2014-06-25");
        saveMatch(wc, "France", "Nigeria", 2, 0, "Round of 16", "Estadio Nacional", 67882, "2014-06-30");
        saveMatch(wc, "France", "Germany", 0, 1, "Quarter-final", "Estadio Maracana", 74738, "2014-07-04");
    }

    private void saveMatch(WorldCup wc, String team1, String team2, int goals1, int goals2,
                           String stage, String stadium, int attendance, String date) {
        matchRepository.save(Match.builder()
                .worldCup(wc)
                .team1(team1)
                .team2(team2)
                .team1Goals(goals1)
                .team2Goals(goals2)
                .stage(stage)
                .stadium(stadium)
                .attendance(attendance)
                .matchDate(LocalDateTime.parse(date + "T15:00:00"))
                .build());
    }
}
