# 🚀 JasperReports Setup Guide - No Studio Required!

## Zero Dependencies, Pure Code Approach

This guide will get you generating PDF reports **without installing Jaspersoft Studio**. We hand-code JRXML templates and compile them programmatically.

> **📚 Deep dive:** For commented explanations of subDatasets, table components, LocalDate, and image URLs, read **[jasper-learning.md](./jasper-learning.md)** alongside `src/main/resources/reports/team_performance.jrxml`.

---

## 📦 Step 1: Add Dependencies

Add these to your `build.gradle`:

```groovy
dependencies {
    // ... existing dependencies ...

    // JasperReports Core
    implementation 'net.sf.jasperreports:jasperreports:6.20.0'

    // Required for PDF export
    implementation 'net.sf.jasperreports:jasperreports-fonts:6.20.0'

    // Expression helpers (optional but recommended)
    implementation 'net.sf.jasperreports:jasperreports-functions:6.20.0'

    // LEARNING: Required for jr:table (advanced table component)
    implementation 'net.sf.jasperreports:jasperreports-components:6.20.0'
}
```

**Run:**
```bash
./gradlew build -x test
```

---

## 📁 Step 2: Create Directory Structure

```bash
# Create reports directory
mkdir -p src/main/resources/reports
mkdir -p src/main/java/com/fifa/reporting/worldcup_report_app/service
mkdir -p src/main/java/com/fifa/reporting/worldcup_report_app/controller
mkdir -p src/main/java/com/fifa/reporting/worldcup_report_app/dto/response
```

---

## 📝 Step 3: JRXML Template (Advanced)

The live template is at `src/main/resources/reports/team_performance.jrxml`. **Do not duplicate it here** — open the file directly; it contains `<!-- LEARNING: ... -->` comments on every major concept.

### What the template demonstrates

| Feature | Where in JRXML | Java side |
|---------|----------------|-----------|
| **SubDataset** | `<subDataset name="MatchHistoryDataset">` | `TeamPerformanceDTO` + `matchDataSource` param |
| **Second SubDataset** | `<subDataset name="TournamentDataset">` | `TournamentSummaryDTO` + `tournamentDataSource` param |
| **Table component** | `<jr:table>` inside `<componentElement>` | Requires `jasperreports-components` |
| **LocalDate** | `class="java.time.LocalDate"` + `pattern=` | `LocalDate.now()`, `match.getMatchDate().toLocalDate()` |
| **External images** | `<imageExpression>$P{teamFlagUrl}</imageExpression>` | `TeamFlagResolver.teamFlagUrl(name)` |
| **Parameters** | `$P{wins}`, `$P{teamName}`, etc. | `ReportService.buildReportParameters()` |
| **Empty main DS** | Detail runs once | `new JREmptyDataSource(1)` |

See **[jasper-learning.md](./jasper-learning.md)** for a full walkthrough.

---

## 💻 Step 4: DTOs (Row Shape for SubDatasets)

DTO property names and types **must match** `<field>` declarations inside each `<subDataset>`.

### TeamPerformanceDTO — match table (9 fields, includes LocalDate)

```java
@Data @Builder
public class TeamPerformanceDTO {
    private Integer year;
    private LocalDate matchDate;      // LEARNING: use LocalDate, not java.util.Date
    private String opponent;
    private String opponentFlagUrl;   // LEARNING: URL for <imageExpression>
    private Integer goalsFor;
    private Integer goalsAgainst;
    private String result;
    private String stage;
    private String stadium;
}
```

### TournamentSummaryDTO — tournament table (second data source)

```java
@Data @Builder
public class TournamentSummaryDTO {
    private Integer year;
    private String hostCountry;
    private LocalDate tournamentStart;
    private LocalDate tournamentEnd;
    private String role;              // Champion, Runner-up, Participant
    private String hostFlagUrl;
}
```

---

## 🛠️ Step 5: Report Service (Key Patterns)

The live implementation is in `service/ReportService.java`. Critical patterns:

```java
// LEARNING: Pass subDataset rows as parameters, not as the main data source
parameters.put("matchDataSource", new JRBeanCollectionDataSource(matchDetails));
parameters.put("tournamentDataSource", new JRBeanCollectionDataSource(tournaments));
parameters.put("teamFlagUrl", TeamFlagResolver.teamFlagUrl(teamName));
parameters.put("reportDate", LocalDate.now());

// LEARNING: Main report renders once; tables iterate their own subDatasets
JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
```

See `ReportService.java` and **[jasper-learning.md](./jasper-learning.md)** for the full service.

---

## 🌐 Step 6: Create the Report Controller

```bash
cat > src/main/java/com/fifa/reporting/worldcup_report_app/controller/ReportController.java << 'EOF'
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
            log.info("📄 Generating team report for: {}", teamName);
            
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
            log.warn("⚠️ Team not found: {}", teamName);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error generating team report for: {}", teamName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
EOF
```

---

## 🚀 Step 7: Build and Run

```bash
# Clean build
./gradlew clean build -x test

# Run the application
./gradlew bootRun
```

---

## 🧪 Step 8: Test the Report

In a **new terminal**:

```bash
mkdir -p output/reports

# Generate report for Argentina
curl -X GET "http://localhost:8080/api/v1/reports/team/Argentina" \
  -H "Accept: application/pdf" \
  --output output/reports/argentina_report.pdf

# Generate report for France
curl -X GET "http://localhost:8080/api/v1/reports/team/France" \
  -H "Accept: application/pdf" \
  --output output/reports/france_report.pdf

# Generate report for Brazil
curl -X GET "http://localhost:8080/api/v1/reports/team/Brazil" \
  -H "Accept: application/pdf" \
  --output output/reports/brazil_report.pdf
```

## 📂 Check the PDF

```bash
# Open the PDF (Linux)
xdg-open output/reports/argentina_report.pdf

# Mac
open output/reports/argentina_report.pdf

# Windows
start output/reports/argentina_report.pdf
```

---

## ✅ Success Criteria

You'll know it's working when:

```bash
✅ PDF file is created under output/reports/
✅ File opens without errors
✅ Report shows:
   - Team flag image (external URL) in header
   - Match history table (jr:table + subDataset) with dates, flags, stadium
   - Tournament participation table (second subDataset) with LocalDate range
   - Overall statistics (wins, losses, draws, goal difference)
   - Generation date formatted from LocalDate
```

---

## 🚨 Troubleshooting

### Issue: "Report template not found"
```bash
# Check if JRXML file exists
ls src/main/resources/reports/team_performance.jrxml

# If not, create it using the template above
```

### Issue: "Failed to compile report"
```bash
# Check for XML syntax errors in team_performance.jrxml
# Ensure jasperreports-components is on the classpath (required for jr:table)
./gradlew dependencies --configuration runtimeClasspath | grep jasperreports
```

### Issue: Blank flag images
```bash
# Flags load from flagcdn.com at PDF fill time — network required
# Template uses onErrorType="Blank" so missing images won't crash the report
```

### Issue: "No matches found"
```bash
# Check your database has match data
docker exec -it fifa_postgres psql -U fifa_user -d fifa_worldcup -c "SELECT COUNT(*) FROM matches;"
```

### Issue: "PDF is blank"
```bash
# Check the data source is populated
# Add debug logging to see the data being passed
```

---

## 🎯 Next Steps After This Works

1. **Read [jasper-learning.md](./jasper-learning.md)** — subDatasets, tables, LocalDate, images
2. **Add World Cup Detail Report**
3. **Add Champion History Report**
4. **Add Charts to Reports**
5. **Add Excel Export**
6. **Add Caching for Reports**
7. **Add Async Report Generation**

---

**You're now ready to generate PDF reports without any GUI tool! 🚀**

**This is the fastest way to get JasperReports working in Spring Boot.**