package com.fifa.reporting.worldcup_report_app.util;

import java.util.Map;

/**
 * Resolves team and host-country names to external flag image URLs.
 * JasperReports loads these at fill time via {@code <imageExpression>}.
 */
public final class TeamFlagResolver {

    private static final String FLAG_CDN = "https://flagcdn.com/w80/%s.png";

    private static final Map<String, String> TEAM_CODES = Map.ofEntries(
            Map.entry("Argentina", "ar"),
            Map.entry("France", "fr"),
            Map.entry("Brazil", "br"),
            Map.entry("Germany", "de"),
            Map.entry("Croatia", "hr"),
            Map.entry("Mexico", "mx"),
            Map.entry("England", "gb-eng"),
            Map.entry("Spain", "es"),
            Map.entry("Portugal", "pt"),
            Map.entry("Netherlands", "nl"),
            Map.entry("Belgium", "be"),
            Map.entry("Uruguay", "uy"),
            Map.entry("Colombia", "co"),
            Map.entry("Chile", "cl"),
            Map.entry("Japan", "jp"),
            Map.entry("South Korea", "kr"),
            Map.entry("USA", "us"),
            Map.entry("Canada", "ca"),
            Map.entry("Morocco", "ma"),
            Map.entry("Senegal", "sn"),
            Map.entry("Poland", "pl"),
            Map.entry("Switzerland", "ch"),
            Map.entry("Denmark", "dk"),
            Map.entry("Australia", "au"),
            Map.entry("Saudi Arabia", "sa"),
            Map.entry("Iran", "ir"),
            Map.entry("Nigeria", "ng"),
            Map.entry("Cameroon", "cm"),
            Map.entry("Serbia", "rs"),
            Map.entry("Costa Rica", "cr"),
            Map.entry("Ecuador", "ec"),
            Map.entry("Tunisia", "tn"),
            Map.entry("Peru", "pe"),
            Map.entry("Iceland", "is"),
            Map.entry("Honduras", "hn"),
            Map.entry("Bosnia and Herzegovina", "ba")
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
