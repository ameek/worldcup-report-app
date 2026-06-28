# JasperReports Learning Guide

Complete walkthrough for this project: how `team_performance.jrxml` connects to `ReportService.java`, how to test APIs in Swagger UI, and how to extend the PDF.

Read this alongside the template — major JRXML blocks include `<!-- LEARNING: ... -->` comments.

---

## 0. Quick links (after `./gradlew bootRun`)

| What | URL |
|------|-----|
| **Swagger UI** (try APIs in browser) | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON (machine-readable spec) | http://localhost:8080/v3/api-docs |
| Download Argentina PDF | http://localhost:8080/api/v1/reports/team/Argentina |
| Health check | http://localhost:8080/actuator/health |

### Using Swagger UI

1. Start the app: `./gradlew bootRun`
2. Open http://localhost:8080/swagger-ui.html in your browser
3. Expand **World Cups** to try JSON endpoints (`GET /api/v1/worldcups`, etc.)
4. Expand **Reports** → **GET /api/v1/reports/team/{teamName}**
5. Click **Try it out**, enter `Argentina`, click **Execute**
6. Use **Download file** in the response to save the PDF

Swagger is powered by **springdoc-openapi 3.x** (Spring Boot 4 compatible). Config lives in `application.yaml` under `springdoc:` and `OpenApiConfig.java`.

---

## 1. Mental Model: Three Layers

```
┌─────────────────────────────────────────────────────────────┐
│  JRXML template     — layout, parameters, subDatasets, tables │
├─────────────────────────────────────────────────────────────┤
│  ReportService.java — compile, fill parameters, data sources │
├─────────────────────────────────────────────────────────────┤
│  DTOs + entities    — row shape must match subDataset fields  │
└─────────────────────────────────────────────────────────────┘
```

| Jasper concept | Java equivalent | Expression in template |
|----------------|-----------------|------------------------|
| Parameter | `Map<String, Object>` entry | `$P{teamName}` |
| Field (main) | Main data source bean property | `$F{year}` |
| Field (subDataset) | Row in `JRBeanCollectionDataSource` | `$F{matchDate}` |
| Band | Section of the page | `<title>`, `<detail>`, `<summary>` |

---

## 2. End-to-end flow (one PDF request)

```
GET /api/v1/reports/team/Argentina
         │
         ▼
ReportController  →  ReportService.generateTeamPerformanceReport("Argentina")
         │
         ├─ matchRepository.findByTeam1OrTeam2(...)
         ├─ buildMatchDetails()     → List<TeamPerformanceDTO>
         ├─ buildTournamentSummary() → List<TournamentSummaryDTO>
         ├─ calculateTeamStats()    → wins, losses, goals, ...
         │
         ├─ parameters.put("matchDataSource", new JRBeanCollectionDataSource(matchDetails))
         ├─ parameters.put("tournamentDataSource", new JRBeanCollectionDataSource(tournaments))
         │
         ├─ JasperFillManager.fillReport(cachedReport, parameters, new JREmptyDataSource(1))
         └─ JasperExportManager.exportReportToPdf(jasperPrint)  →  byte[] PDF
```

**Key insight:** The main data source is empty (`JREmptyDataSource(1)` = render once). Tables pull rows from **parameter-bound** subDatasets.

---

## 3. Parameters vs Fields vs SubDatasets

### Parameters (`$P{}`)

**What:** Values passed once from Java — scalars, URLs, and data sources.

```java
// ReportService.buildReportParameters()
stats.put("teamName", teamName);
stats.put("teamFlagUrl", TeamFlagResolver.teamFlagUrl(teamName));
stats.put("reportDate", LocalDate.now());
stats.put("wins", wins);
stats.put("matchDataSource", new JRBeanCollectionDataSource(matchDetails));
stats.put("tournamentDataSource", new JRBeanCollectionDataSource(tournaments));
```

```xml
<parameter name="teamName" class="java.lang.String"/>
<parameter name="matchDataSource" class="net.sf.jasperreports.engine.JRDataSource"/>

<textFieldExpression><![CDATA["Team: " + $P{teamName}]]></textFieldExpression>
```

| Parameter kind | Used for | Example |
|----------------|----------|---------|
| Scalar stats | Summary band text fields | `$P{wins}`, `$P{goalDifference}` |
| URLs | Header / table images | `$P{teamFlagUrl}` |
| Data sources | `jr:table` rows | `$P{matchDataSource}` |

### Fields (`$F{}`)

**What:** Properties on the **current row** of a data source.

- In a classic detail band, fields come from the **main** data source.
- Inside a `jr:table` with `datasetRun subDataset="..."`, fields come from the **subDataset** row.

```java
// TeamPerformanceDTO — property names MUST match subDataset field names
private LocalDate matchDate;
private String opponent;
```

```xml
<subDataset name="MatchHistoryDataset">
    <field name="matchDate" class="java.time.LocalDate"/>
    <field name="opponent" class="java.lang.String"/>
</subDataset>

<!-- Inside match table detail cell -->
<textFieldExpression><![CDATA[$F{opponent}]]></textFieldExpression>
```

### SubDatasets

**What:** A separate schema + data source for nested content (tables, charts, subreports).

**Problem it solves:** One PDF needs **two tables** with different row types (matches vs tournaments), but the main report only renders once.

| SubDataset | Java DTO | Parameter | Table location |
|------------|----------|-----------|----------------|
| `MatchHistoryDataset` | `TeamPerformanceDTO` | `matchDataSource` | `<detail>` band |
| `TournamentDataset` | `TournamentSummaryDTO` | `tournamentDataSource` | `<summary>` band |

```java
JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
```

---

## 4. Report bands in this template

```
┌──────────────────────────────────────┐
│ TITLE                                │  Team flag, title, report date
├──────────────────────────────────────┤
│ DETAIL (×1, empty main DS)           │  Match History jr:table (8 columns)
├──────────────────────────────────────┤
│ SUMMARY                              │  Stats ($P{…}) + Tournament jr:table
└──────────────────────────────────────┘
```

| Band | Prints when | Our content |
|------|-------------|-------------|
| `title` | Once, before detail | Branding + team header |
| `detail` | Per main data source record (once here) | Match history table |
| `summary` | Once, after detail | Win/loss stats + tournament participation |

---

## 5. Table component (`jr:table`)

The `jr:table` component ships inside the main `jasperreports` JAR (6.20). No separate `jasperreports-components` artifact is needed.

**Manual detail band (old approach):**

```
columnHeader → static labels
detail       → one textField per column, repeated per row
```

**Table component (used in this project):**

```xml
<componentElement>
    <reportElement x="0" y="30" width="555" height="240" isPrintWhenDetailOverflows="true"/>
    <jr:table xmlns:jr="http://jasperreports.sourceforge.net/jasperreports/components">
        <datasetRun subDataset="MatchHistoryDataset">
            <dataSourceExpression><![CDATA[$P{matchDataSource}]]></dataSourceExpression>
        </datasetRun>
        <jr:column width="72">
            <jr:columnHeader height="26" rowSpan="1">...</jr:columnHeader>
            <jr:detailCell height="22">...</jr:detailCell>
        </jr:column>
    </jr:table>
</componentElement>
```

### Match history table (detail band)

| Column | Width | Field / expression |
|--------|-------|-------------------|
| Flag | 38 | `$F{opponentFlagUrl}` (image) |
| Year | 42 | `$F{year}` |
| Date | 72 | `$F{matchDate}` (pattern `yyyy-MM-dd`) |
| Opponent | 95 | `$F{opponent}` |
| Score | 48 | `$F{goalsFor} + " - " + $F{goalsAgainst}` |
| Result | 48 | `$F{result}` |
| Stage | 95 | `$F{stage}` |
| Stadium | 117 | `$F{stadium}` |

Column widths must sum to `columnWidth` (555).

### Tournament table (summary band)

| Column | Width | Field / expression |
|--------|-------|-------------------|
| Host flag | 38 | `$F{hostFlagUrl}` (image) |
| Year | 50 | `$F{year}` |
| Host Country | 110 | `$F{hostCountry}` |
| Start | 95 | `$F{tournamentStart}` (pattern `MMM dd, yyyy`) |
| End | 95 | `$F{tournamentEnd}` (pattern `MMM dd, yyyy`) |
| Team Role | 167 | `$F{role}` (Champion / Runner-up / Participant) |

---

## 6. LocalDate (`java.time`)

**Old way:** `java.util.Date` + `SimpleDateFormat` in expressions.

**Modern way:** Pass `LocalDate` from Java; use `pattern` on the text field.

```java
.matchDate(match.getMatchDate().toLocalDate())
```

```xml
<field name="matchDate" class="java.time.LocalDate"/>

<textField pattern="yyyy-MM-dd">
    <reportElement x="0" y="0" width="72" height="22"/>
    <textFieldExpression><![CDATA[$F{matchDate}]]></textFieldExpression>
</textField>
```

| Pattern | Example output |
|---------|----------------|
| `yyyy-MM-dd` | 2022-12-18 |
| `MMM dd, yyyy` | Dec 18, 2022 |
| `MMMM dd, yyyy` | December 18, 2022 |

**Rule:** The `class` on `<field>` or `<parameter>` must match the Java type exactly.

---

## 7. External images (URL)

JasperReports loads images when the expression evaluates to a **String URL**.

```java
// TeamFlagResolver.java
return "https://flagcdn.com/w80/ar.png";
```

```xml
<image scaleImage="FillFrame" onErrorType="Blank">
    <reportElement x="4" y="2" width="30" height="18"/>
    <imageExpression><![CDATA[$F{opponentFlagUrl}]]></imageExpression>
</image>
```

| Attribute | Purpose |
|-----------|---------|
| `scaleImage="FillFrame"` | Fit image in the box |
| `onErrorType="Blank"` | Skip image if URL fails (offline PDF generation) |

PDF generation needs network access at fill time unless you switch to classpath images.

---

## 8. Expression building

### Concatenation

```xml
<textFieldExpression><![CDATA[$F{goalsFor} + " - " + $F{goalsAgainst}]]></textFieldExpression>
```

### Parameter + literal

```xml
<textFieldExpression><![CDATA["Team: " + $P{teamName}]]></textFieldExpression>
```

Expressions use Java syntax inside `<![CDATA[ ... ]]>`.

---

## 9. DTO field checklist

### `TeamPerformanceDTO` — match table rows

| Field | Type | Source |
|-------|------|--------|
| `year` | Integer | `WorldCup.year` |
| `matchDate` | **LocalDate** | `Match.matchDate` |
| `opponent` | String | derived from team1/team2 |
| `opponentFlagUrl` | String | `TeamFlagResolver` |
| `goalsFor` | Integer | derived |
| `goalsAgainst` | Integer | derived |
| `result` | String | Win / Loss / Draw |
| `stage` | String | `Match.stage` |
| `stadium` | String | `Match.stadium` |

### `TournamentSummaryDTO` — tournament table rows

| Field | Type | Source |
|-------|------|--------|
| `year` | Integer | `WorldCup.year` |
| `hostCountry` | String | `WorldCup.hostCountry` |
| `tournamentStart` | **LocalDate** | `WorldCup.startDate` |
| `tournamentEnd` | **LocalDate** | `WorldCup.endDate` |
| `role` | String | Champion / Runner-up / Participant |
| `hostFlagUrl` | String | `TeamFlagResolver.hostCountryFlagUrl()` |

**Naming rule:** Java bean property name = subDataset `<field name="…">` = `$F{…}` in expressions.

---

## 10. Compile → Fill → Export pipeline

```java
// 1. COMPILE (once, at startup — ReportService.init())
JasperReport compiled = JasperCompileManager.compileReport(jrxmlInputStream);

// 2. FILL (per request)
JasperPrint print = JasperFillManager.fillReport(compiled, parameters, new JREmptyDataSource(1));

// 3. EXPORT
byte[] pdf = JasperExportManager.exportReportToPdf(print);
```

| Stage | Input | Output |
|-------|-------|--------|
| Compile | `.jrxml` | `JasperReport` (cached in memory) |
| Fill | parameters + data sources | `JasperPrint` (in-memory document) |
| Export | `JasperPrint` | PDF bytes |

Templates are compiled on startup. After editing `.jrxml`, restart the app.

---

## 11. JRXML pitfalls (learned the hard way)

| Pitfall | Symptom | Fix |
|---------|---------|-----|
| `--` inside XML comments | `Parse Fatal Error: The string "--" is not permitted within comments` | Remove dashed separator lines from comments |
| Missing `x/y/width/height` on `<reportElement>` | `Attribute 'x' must appear on element 'reportElement'` | Add all four attributes; match width to column width in tables |
| `reportElement` after `<box>` in textField | `Invalid content was found starting with element reportElement` | `reportElement` must be the **first** child of textField/image |
| DTO name ≠ field name | `Field not found: matchDate` | Align Java property, subDataset field, and `$F{}` |
| Wrong Java type in XML | `ClassCastException` on date | Use `java.time.LocalDate`, not `java.util.Date` |
| Empty table | Wrong parameter or empty list | Log `matchDetails.size()` in `ReportService` |
| Template not found at runtime | `500` on report endpoint | Check startup log for `Compiled report: team_performance` |

---

## 12. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Field not found: matchDate` | DTO property ≠ subDataset field name | Align names and types |
| Table is empty | Wrong parameter type or empty list | Log list sizes in `ReportService` |
| `ClassCastException` on date | Type mismatch in XML | Match `class` attribute to Java |
| Blank flag images | No network at fill time | Expected with `onErrorType="Blank"` |
| Report compiles but PDF fails | JRXML typo | Grep logs for `Failed to compile report` |
| Swagger UI 404 | App not running | `./gradlew bootRun`, then open `/swagger-ui.html` |

**Startup check:**

```bash
./gradlew bootRun 2>&1 | grep -iE 'Compiled report|Failed to compile'
```

**Generate test PDF:**

```bash
curl -o argentina.pdf "http://localhost:8080/api/v1/reports/team/Argentina" -H "Accept: application/pdf"
```

---

## 13. Files to study (in order)

| # | File | What to learn |
|---|------|---------------|
| 1 | `src/main/resources/reports/team_performance.jrxml` | Bands, subDatasets, both tables |
| 2 | `service/ReportService.java` | Parameter wiring, DTO building, compile/fill/export |
| 3 | `dto/response/TeamPerformanceDTO.java` | Match row shape |
| 4 | `dto/response/TournamentSummaryDTO.java` | Tournament row shape |
| 5 | `util/TeamFlagResolver.java` | External image URLs |
| 6 | `controller/ReportController.java` | REST → PDF download |
| 7 | `resources/jasperreports.properties` | Fonts and PDF encoding |
| 8 | `config/OpenApiConfig.java` | Swagger metadata |

---

## 14. Hands-on exercises

1. **Swagger first** — Open Swagger UI, call `GET /worldcups`, then download Argentina's PDF from the Reports section.
2. **Add a column** — Add `attendance` to `TeamPerformanceDTO`, seed data, subDataset field, and a new `jr:column`.
3. **Conditional color** — Color the Result cell green/red/orange based on `$F{result}`.
4. **New report** — Copy `team_performance.jrxml` → `champion_history.jrxml`, wire a new method in `ReportService`.
5. **Excel export** — After fill, use `JRXlsxExporter` instead of `JasperExportManager.exportReportToPdf`.

---

## 15. Skill checklist

| Skill | Status in this project |
|-------|------------------------|
| Report design (title / detail / summary) | ✅ Three bands |
| Table component | ✅ Two `jr:table` instances |
| SubDataset | ✅ Match + tournament datasets |
| Image handling | ✅ Team, opponent, host flags via URL |
| LocalDate | ✅ Match dates, tournament dates, report date |
| Complex data (7+ fields) | ✅ 9 match fields, 6 tournament fields |
| Expression building | ✅ Concatenation, score formatting |
| Parameter system | ✅ Stats + URLs + data sources |
| API testing | ✅ Swagger UI + OpenAPI JSON |
| Styling | ✅ Color-coded stats, band backgrounds |

---

**Next experiments:** conditional row colors by result, charts in summary band, subreport for match detail pages, async report generation, Excel export via `JRXlsxExporter`.
