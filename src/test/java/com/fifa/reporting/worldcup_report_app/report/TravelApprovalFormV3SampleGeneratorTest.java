package com.fifa.reporting.worldcup_report_app.report;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JREmptyDataSource;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

class TravelApprovalFormV3SampleGeneratorTest {

    @Test
    void generateSamplePdf() throws Exception {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path outputPdf = projectRoot.resolve("docs/samples/travel_approval_form_v3.pdf");
        Path logoPath = projectRoot.resolve("src/main/resources/images/brac_logo.png");

        try (InputStream jrxml = getClass().getResourceAsStream("/reports/travel_approval_form_v3.jrxml")) {
            if (jrxml == null) {
                throw new IllegalStateException("travel_approval_form_v3.jrxml not found on classpath");
            }

            var jasperReport = JasperCompileManager.compileReport(jrxml);

            Map<String, Object> params = new HashMap<>();
            params.put("LOGO_PATH", logoPath.toString());

            JasperPrint print = JasperFillManager.fillReport(
                    jasperReport,
                    params,
                    new JREmptyDataSource(1)
            );

            Files.createDirectories(outputPdf.getParent());
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(print);
            Files.write(outputPdf, pdfBytes);
        }
    }
}
