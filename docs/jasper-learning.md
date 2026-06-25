# JasperReports Learning Guide

This document explains the **advanced patterns** used in `team_performance.jrxml` and how they connect to Java code. Read it alongside the template — every major block in the JRXML has a matching `<!-- LEARNING: ... -->` comment.

---

## 1. Mental Model: Three Layers

```
┌─────────────────────────────────────────────────────────────┐
│  JRXML template     — layout, parameters, subDatasets       │
├─────────────────────────────────────────────────────────────┤
│  ReportService.java — compile, fill parameters, data sources │
├─────────────────────────────────────────────────────────────┤
│  DTOs + entities    — row shape must match subDataset fields │
└─────────────────────────────────────────────────────────────┘
```

| Jasper concept | Java equivalent | Expression in template |
|----------------|-----------------|------------------------|
| Parameter | `Map<String, Object>` entry | `$P{teamName}` |
| Field (main) | Main data source bean property | `$F{year}` |
| Field (subDataset) | Row in `JRBeanCollectionDataSource` | `$F{matchDate}` |
| Band | Section of the page | `<title>`, `<detail>`, `<summary>` |

---

## 2. Parameters vs Fields vs SubDatasets

### Parameters (`$P{}`)

**What:** Values you pass once from Java — team name, totals, image URLs, data sources.

```java
// ReportService.java
parameters.put("teamName", teamName);
parameters.put("teamFlagUrl", TeamFlagResolver.teamFlagUrl(teamName));
parameters.put("reportDate", LocalDate.now());  // java.time.LocalDate
parameters.put("wins", wins);
```

```xml
<!-- team_performance.jrxml -->
<parameter name="teamName" class="java.lang.String"/>
<parameter name="reportDate" class="java.time.LocalDate"/>

<textFieldExpression><![CDATA["Team: " + $P{teamName}]]></textFieldExpression>
```

**Why parameters for summary stats?** They are computed once in Java and displayed in `<summary>` without iterating rows.

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
```

### SubDatasets

**What:** A separate schema + data source for nested content (tables, charts, subreports).

**Problem it solves:** The main report might only need to render once, but you have **two tables** with different row types (matches vs tournaments).

**Pattern used in this project:**

| SubDataset | Java DTO | Parameter |
|------------|----------|-----------|
| `MatchHistoryDataset` | `TeamPerformanceDTO` | `matchDataSource` |
| `TournamentDataset` | `TournamentSummaryDTO` | `tournamentDataSource` |

```java
parameters.put("matchDataSource", new JRBeanCollectionDataSource(matchDetails));
parameters.put("tournamentDataSource", new JRBeanCollectionDataSource(tournaments));

// Main report body does not iterate match rows
JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
```

`JREmptyDataSource(1)` = "render the report once"; tables pull their own rows from parameters.

---

## 3. Table Component (`jr:table`)

**Dependency required:**

```groovy
implementation 'net.sf.jasperreports:jasperreports-components:6.20.0'
```

**Manual detail band (old approach):**

```
columnHeader → static labels
detail       → one textField per column, repeated per row
```

**Table component (advanced):**

```xml
<componentElement>
    <jr:table>
        <datasetRun subDataset="MatchHistoryDataset">
            <dataSourceExpression><![CDATA[$P{matchDataSource}]]></dataSourceExpression>
        </datasetRun>
        <jr:column width="72">
            <jr:columnHeader>...</jr:columnHeader>
            <jr:detailCell>...</jr:detailCell>
        </jr:column>
    </jr:table>
</componentElement>
```

**Benefits:**
- Column widths are declared in one place
- Headers and cells stay aligned when data grows
- Easier to add/remove columns
- Supports nested subDatasets cleanly

---

## 4. LocalDate (`java.time`)

**Old way:** `java.util.Date` + `SimpleDateFormat` in expressions.

**Modern way:** Pass `LocalDate` from Java; use `pattern` on the text field.

```java
.matchDate(match.getMatchDate().toLocalDate())
```

```xml
<field name="matchDate" class="java.time.LocalDate"/>

<textField pattern="yyyy-MM-dd">
    <textFieldExpression><![CDATA[$F{matchDate}]]></textFieldExpression>
</textField>
```

| Pattern | Example output |
|---------|----------------|
| `yyyy-MM-dd` | 2022-12-18 |
| `MMM dd, yyyy` | Dec 18, 2022 |
| `MMMM dd, yyyy` | December 18, 2022 |

**Rule:** The `class` on the `<field>` or `<parameter>` must match the Java type exactly.

---

## 5. External Images (URL)

JasperReports can load images when the expression evaluates to a **String URL**.

```java
// TeamFlagResolver.java
return "https://flagcdn.com/w80/ar.png";
```

```xml
<!-- Header: team flag from parameter -->
<image scaleImage="FillFrame" onErrorType="Blank">
    <imageExpression><![CDATA[$P{teamFlagUrl}]]></imageExpression>
</image>

<!-- Table row: opponent flag from subDataset field -->
<image scaleImage="FillFrame" onErrorType="Blank">
    <imageExpression><![CDATA[$F{opponentFlagUrl}]]></imageExpression>
</image>
```

| Attribute | Purpose |
|-----------|---------|
| `scaleImage="FillFrame"` | Fit image in the box |
| `onErrorType="Blank"` | Skip image if URL fails (offline PDF generation) |

**Note:** PDF generation needs network access at fill time unless you switch to classpath images or pre-downloaded bytes.

---

## 6. Expression Building

### Concatenation

```xml
<textFieldExpression><![CDATA[$F{goalsFor} + " - " + $F{goalsAgainst}]]></textFieldExpression>
```

### Parameter + literal

```xml
<textFieldExpression><![CDATA["Team: " + $P{teamName}]]></textFieldExpression>
```

### Conditional styling (future)

```xml
<textFieldExpression><![CDATA[$F{result}.equals("Win") ? "✓ " + $F{result} : $F{result}]]></textFieldExpression>
```

Expressions use Java syntax inside `<![CDATA[ ... ]]>`.

---

## 7. Report Bands in This Template

```
┌──────────────────────────────────────┐
│ TITLE                                │  Team flag, title, LocalDate
├──────────────────────────────────────┤
│ DETAIL (×1, empty main DS)           │  Match History jr:table
├──────────────────────────────────────┤
│ SUMMARY                              │  Stats parameters + Tournament jr:table
└──────────────────────────────────────┘
```

| Band | Prints when | Our content |
|------|-------------|-------------|
| `title` | Once, before detail | Branding + team header |
| `detail` | Per main data source record | Match table |
| `summary` | Once, after all detail | Aggregates + tournament table |

---

## 8. DTO Field Checklist (7+ fields with dates)

### `TeamPerformanceDTO` — match table rows

| Field | Type | Source |
|-------|------|--------|
| `year` | Integer | `WorldCup.year` |
| `matchDate` | **LocalDate** | `Match.matchDate` |
| `opponent` | String | derived |
| `opponentFlagUrl` | String | `TeamFlagResolver` |
| `goalsFor` | Integer | derived |
| `goalsAgainst` | Integer | derived |
| `result` | String | Win/Loss/Draw |
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
| `hostFlagUrl` | String | `TeamFlagResolver` |

---

## 9. Compile → Fill → Export Pipeline

```java
// 1. COMPILE (once, at startup)
JasperReport compiled = JasperCompileManager.compileReport(jrxmlInputStream);

// 2. FILL (per request)
JasperPrint print = JasperFillManager.fillReport(compiled, parameters, mainDataSource);

// 3. EXPORT
byte[] pdf = JasperExportManager.exportReportToPdf(print);
```

| Stage | Input | Output |
|-------|-------|--------|
| Compile | `.jrxml` | `JasperReport` (cached) |
| Fill | parameters + data sources | `JasperPrint` (in-memory doc) |
| Export | `JasperPrint` | PDF bytes |

---

## 10. Skill Comparison (You vs Baseline)

| Skill | Status in this project |
|-------|------------------------|
| Report design (header/body/summary) | ✅ Title + detail table + summary |
| Table component | ✅ `jr:table` with 8 columns |
| SubDataset | ✅ Two subDatasets, two parameters |
| Image handling | ✅ Team + opponent + host flags via URL |
| LocalDate | ✅ `matchDate`, `tournamentStart/End`, `reportDate` |
| Complex data (7+ fields) | ✅ 9 match fields, 6 tournament fields |
| Expression building | ✅ Concatenation, parameter labels |
| Parameter system | ✅ Stats + URLs + data sources |
| Styling | ✅ Color-coded stats, band backgrounds |

---

## 11. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `Field not found: matchDate` | DTO property ≠ subDataset field name | Align names and types |
| Table is empty | Wrong parameter type or empty list | Log `matchDetails.size()` |
| `ClassCastException` on date | Used `java.util.Date` in XML but `LocalDate` in Java | Match `class` attribute |
| Compile error on `jr:table` | Missing components JAR | Add `jasperreports-components` |
| Blank flag images | No network at fill time | Use `onErrorType="Blank"` or local images |
| Report compiles but PDF fails | JRXML XML typo | Check logs in `ReportService.init()` |

---

## 12. Files to Study

| File | What to learn |
|------|---------------|
| `src/main/resources/reports/team_performance.jrxml` | Template structure, comments |
| `service/ReportService.java` | Parameter wiring, empty main DS |
| `dto/response/TeamPerformanceDTO.java` | Match row shape |
| `dto/response/TournamentSummaryDTO.java` | Tournament row shape |
| `util/TeamFlagResolver.java` | External image URLs |
| `docs/report.md` | Setup and test commands |

---

**Next experiments:** conditional row colors by result, charts in summary band, subreport for match detail pages, Excel export via `JRXlsxExporter`.
