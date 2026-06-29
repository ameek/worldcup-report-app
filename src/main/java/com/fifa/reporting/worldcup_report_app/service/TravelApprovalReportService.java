package com.fifa.reporting.worldcup_report_app.service;

import com.fifa.reporting.worldcup_report_app.dto.response.TravelApprovalDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TravelApprovalReportService {

    private static final String REPORT_V1 = "travel_approval_form";
    private static final String REPORT_V2 = "travel_approval_form_v2";
    private static final DateTimeFormatter FOOTER_TIMESTAMP =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a", Locale.ENGLISH);

    private final ResourceLoader resourceLoader;

    private final Map<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        compileReport(REPORT_V1);
        compileReport(REPORT_V2);
    }

    private void compileReport(String reportName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:reports/" + reportName + ".jrxml");
            try (InputStream inputStream = resource.getInputStream()) {
                reportCache.put(reportName, JasperCompileManager.compileReport(inputStream));
                log.info("Compiled report: {}", reportName);
            }
        } catch (Exception e) {
            log.error("Failed to compile travel approval report: {}", reportName, e);
        }
    }

    public byte[] generateDummyTravelApprovalReport() throws JRException {
        return generateTravelApprovalReport(REPORT_V1, buildDummyData());
    }

    public byte[] generateDummyTravelApprovalReportV2() throws JRException {
        return generateTravelApprovalReport(REPORT_V2, buildDummyData());
    }

    public byte[] generateTravelApprovalReport(TravelApprovalDTO data) throws JRException {
        return generateTravelApprovalReport(REPORT_V1, data);
    }

    public byte[] generateTravelApprovalReportV2(TravelApprovalDTO data) throws JRException {
        return generateTravelApprovalReport(REPORT_V2, data);
    }

    private byte[] generateTravelApprovalReport(String reportName, TravelApprovalDTO data) throws JRException {
        JasperReport jasperReport = reportCache.get(reportName);
        if (jasperReport == null) {
            throw new IllegalStateException("Travel approval report template not found: " + reportName);
        }

        Map<String, Object> parameters = buildReportParameters(data);

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                new JREmptyDataSource(1)
        );
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private Map<String, Object> buildReportParameters(TravelApprovalDTO data) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("formNumber", data.getFormNumber());
        parameters.put("docControlNumber", data.getDocControlNumber());
        parameters.put("applicantName", data.getApplicantName());
        parameters.put("pin", data.getPin());
        parameters.put("designation", data.getDesignation());
        parameters.put("program", data.getProgram());
        parameters.put("destination", data.getDestination());
        parameters.put("travelStartDateTime", data.getTravelStartDateTime());
        parameters.put("probableReturnDateTime", data.getProbableReturnDateTime());
        parameters.put("purpose", data.getPurpose());
        parameters.put("applicationDate", data.getApplicationDate());
        parameters.put("applicantSignatureDate", data.getApplicantSignatureDate());
        parameters.put("approverName", data.getApproverName());
        parameters.put("approverPin", data.getApproverPin());
        parameters.put("approverDesignation", data.getApproverDesignation());
        parameters.put("generatedAt", data.getGeneratedAt());
        parameters.put("footerUrl", data.getFooterUrl());
        parameters.put("footerUser", data.getFooterUser());

        java.net.URL logoUrl = Thread.currentThread().getContextClassLoader()
                .getResource("images/brac-logo.png");
        if (logoUrl != null) {
            parameters.put("bracLogoUrl", logoUrl.toExternalForm());
        }
        return parameters;
    }

    private TravelApprovalDTO buildDummyData() {
        return TravelApprovalDTO.builder()
                .docControlNumber("1 14/18/2011")
                .formNumber("14/18/2011")
                .applicantName("MUSHRIF AHMED")
                .pin("00254539")
                .designation("Senior Manager, User Experience")
                .program("Technology Division")
                .destination("Cumilla BLC")
                .travelStartDateTime("25 Feb 2025 8:30 AM")
                .probableReturnDateTime("27 Feb 2025 5:15 PM")
                .purpose("ERP v3 HR module")
                .applicationDate("02 Mar 2025")
                .applicantSignatureDate("02 Mar 2025")
                .approverName("M.M. ARAFAT HOSSAIN")
                .approverPin("00250006")
                .approverDesignation("Head of PMU and Applications")
                .generatedAt(FOOTER_TIMESTAMP.format(LocalDateTime.now()))
                .footerUrl("https://ebenefits.brac.net/payroll-selfcare/visit")
                .footerUser("Mushrif Ahmed [PIN NUMBER]")
                .build();
    }
}
