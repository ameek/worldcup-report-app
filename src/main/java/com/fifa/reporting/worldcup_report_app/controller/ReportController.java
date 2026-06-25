package com.fifa.reporting.worldcup_report_app.controller;

import com.fifa.reporting.worldcup_report_app.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/team/{teamName}")
    public ResponseEntity<ByteArrayResource> generateTeamReport(@PathVariable String teamName) {
        try {
            log.info("Generating team report for: {}", teamName);

            byte[] reportBytes = reportService.generateTeamPerformanceReport(teamName);

            String filename = URLEncoder.encode(
                    teamName + "_performance_report.pdf",
                    StandardCharsets.UTF_8
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(reportBytes));

        } catch (IllegalArgumentException e) {
            log.warn("Team not found: {}", teamName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error generating team report for: {}", teamName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
