package com.fifa.reporting.worldcup_report_app.util;

import java.util.Map;

/**
 * Resolves team and host-country names to external flag image URLs.
 * JasperReports loads these at fill time via {@code <imageExpression>}.
 */
public final class TeamFlagResolver {

    private static final String FLAG_CDN = "https://flagcdn.com/w80/%s.png";

    private static final Map<String, String> TEAM_CODES = Map.of(
            "Argentina", "ar",
            "France", "fr",
            "Brazil", "br",
            "Germany", "de",
            "Croatia", "hr",
            "Mexico", "mx",
            "England", "gb-eng",
            "Spain", "es",
            "Portugal", "pt",
            "Netherlands", "nl",
            "Belgium", "be",
            "Uruguay", "uy",
            "Colombia", "co",
            "Chile", "cl",
            "Japan", "jp",
            "South Korea", "kr",
            "USA", "us",
            "Canada", "ca",
            "Morocco", "ma",
            "Senegal", "sn",
            "Poland", "pl",
            "Switzerland", "ch",
            "Denmark", "dk",
            "Australia", "au",
            "Saudi Arabia", "sa",
            "Iran", "ir",
            "Nigeria", "ng",
            "Cameroon", "cm",
            "Serbia", "rs",
            "Costa Rica", "cr",
            "Ecuador", "ec",
            "Tunisia", "tn",
            "Peru", "pe",
            "Iceland", "is",
            "Honduras", "hn",
            "Bosnia and Herzegovina", "ba"
    );

    private static final Map<String, String> HOST_COUNTRY_CODES = Map.of(
            "Qatar", "qa",
            "Russia", "ru",
            "Brazil", "br",
            "South Africa", "za",
            "Germany", "de",
            "Japan", "jp",
            "Korea Republic", "kr",
            "France", "fr",
            "USA", "us"
    );

    private TeamFlagResolver() {
    }

    public static String teamFlagUrl(String teamName) {
        return String.format(FLAG_CDN, TEAM_CODES.getOrDefault(teamName, "un"));
    }

    public static String hostCountryFlagUrl(String hostCountry) {
        return String.format(FLAG_CDN, HOST_COUNTRY_CODES.getOrDefault(hostCountry, "un"));
    }
}
