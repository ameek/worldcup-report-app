package com.fifa.reporting.worldcup_report_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelApprovalDTO {

    private String formNumber;
    private String docControlNumber;
    private String applicantName;
    private String pin;
    private String designation;
    private String program;
    private String destination;
    private String travelStartDateTime;
    private String probableReturnDateTime;
    private String purpose;
    private String applicationDate;
    private String applicantSignatureDate;
    private String approverName;
    private String approverPin;
    private String approverDesignation;
    private String generatedAt;
    private String footerUrl;
    private String footerUser;
}
