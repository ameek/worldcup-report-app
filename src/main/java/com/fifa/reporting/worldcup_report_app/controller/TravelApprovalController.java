package com.fifa.reporting.worldcup_report_app.controller;

import com.fifa.reporting.worldcup_report_app.service.TravelApprovalReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/travel-approval")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Travel Approval", description = "BRAC travel approval form PDF generation")
public class TravelApprovalController {

    private final TravelApprovalReportService travelApprovalReportService;

    @GetMapping("/report")
    @Operation(summary = "Generate dummy travel approval form PDF (v1 template)")
    public ResponseEntity<ByteArrayResource> generateDummyTravelApprovalReport() {
        return generatePdf(
                travelApprovalReportService::generateDummyTravelApprovalReport,
                "travel_approval_form.pdf"
        );
    }

    @GetMapping("/report/v2")
    @Operation(summary = "Generate dummy travel approval form PDF (v2 template — bordered layout)")
    public ResponseEntity<ByteArrayResource> generateDummyTravelApprovalReportV2() {
        return generatePdf(
                travelApprovalReportService::generateDummyTravelApprovalReportV2,
                "travel_approval_form_v2.pdf"
        );
    }

    private ResponseEntity<ByteArrayResource> generatePdf(
            ReportGenerator generator,
            String filename) {
        try {
            log.info("Generating travel approval form report: {}", filename);

            byte[] reportBytes = generator.generate();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(reportBytes));

        } catch (Exception e) {
            log.error("Error generating travel approval form report: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @FunctionalInterface
    private interface ReportGenerator {
        byte[] generate() throws Exception;
    }
}
