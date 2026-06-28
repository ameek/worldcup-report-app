# FIFA World Cup Reporting App

Spring Boot app that serves World Cup match data from PostgreSQL and **generates PDF reports with JasperReports 6.20**.

Use this repo to learn how JRXML templates connect to Java services and how to download or extend team performance reports.

---

## Quick start (3 steps)

**Requirements:** Java 21, Docker

```bash
# 1. Clone and enter the project
git clone <your-repo-url> worldcup-report-app
cd worldcup-report-app

# 2. Start PostgreSQL
docker compose up -d postgres

# 3. Run the app
./gradlew bootRun
```

On first startup the app seeds sample data (World Cups 2014, 2018, 2022 and their matches).

| Check | URL / command |
|-------|----------------|
| Health | http://localhost:8080/actuator/health |
| **Swagger UI** (try APIs + download PDF) | **http://localhost:8080/swagger-ui.html** |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Run tests | `./gradlew test` |

### Swagger / OpenAPI

After `./gradlew bootRun`, open **http://localhost:8080/swagger-ui.html** in your browser.

- **World Cups** — try JSON endpoints interactively
- **Reports → GET /api/v1/reports/team/{teamName}** — enter `Argentina`, click **Execute**, then **Download file** for the PDF

Raw OpenAPI spec: http://localhost:8080/v3/api-docs

**Database** (from `docker-compose.yml`):

| Host | Port | Database | User | Password |
|------|------|----------|------|----------|
| `localhost` | `5432` | `fifa_worldcup` | `fifa_user` | `fifa_pass` |

---

## JasperReports — the main focus

### What you get today

A **team performance PDF** for any seeded team (e.g. Argentina, France, Brazil):

```bash
mkdir -p output/reports
curl -o output/reports/argentina.pdf \
  "http://localhost:8080/api/v1/reports/team/Argentina" \
  -H "Accept: application/pdf"
```

Open `output/reports/argentina.pdf` — you should see the team flag, **match history table**, win/loss statistics, and **tournament participation table**.

Same endpoint in Swagger UI: **Reports → Generate team performance PDF report**.

### How report generation works

```
PostgreSQL (matches)
       │
       ▼
ReportService.java          team_performance.jrxml
  · load matches by team  ←→  · parameters ($P{teamName}, $P{wins}, …)
  · compute stats             · title band (flag + header)
  · build DTO lists           · detail band (match history jr:table)
  · compile JRXML at startup  · summary band (stats + tournament jr:table)
  · fill + export PDF
       │
       ▼
  PDF bytes → ReportController → browser / curl download
```

| Step | Where | What happens |
|------|--------|----------------|
| **Compile** | `ReportService.init()` | `JasperCompileManager.compileReport()` reads `classpath:reports/team_performance.jrxml` and caches the compiled `JasperReport` |
| **Query** | `ReportService.generateTeamPerformanceReport()` | Loads matches from DB, builds DTO lists and summary stats |
| **Fill** | same method | `JasperFillManager.fillReport(report, parameters, new JREmptyDataSource(1))` |
| **Export** | same method | `JasperExportManager.exportReportToPdf(jasperPrint)` |
| **Deliver** | `ReportController` | Returns `application/pdf` with `Content-Disposition: attachment` |

### Key files (reporting)

| File | Purpose |
|------|---------|
| `src/main/resources/reports/team_performance.jrxml` | Report layout — edit this to change the PDF |
| `src/main/java/.../service/ReportService.java` | Compile, fill parameters, export PDF |
| `src/main/java/.../controller/ReportController.java` | REST endpoint `/api/v1/reports/team/{teamName}` |
| `src/main/resources/jasperreports.properties` | Default font (DejaVu Sans), PDF encoding |
| `src/main/java/.../util/TeamFlagResolver.java` | Flag image URLs passed into the report |
| `docs/jasper-learning.md` | Deep dive: parameters, subDatasets, `jr:table`, common pitfalls |

### Parameters the template expects

These are set in `ReportService.buildReportParameters()` and referenced in the JRXML as `$P{name}`:

| Parameter | Type | Example use in PDF |
|-----------|------|---------------------|
| `teamName` | String | Title: "Team: Argentina" |
| `teamFlagUrl` | String | Flag image in header |
| `reportDate` | LocalDate | "Generated: …" |
| `totalWorldCups`, `totalMatches` | Integer | Summary stats |
| `wins`, `losses`, `draws` | Integer | Summary stats |
| `goalsScored`, `goalsConceded`, `goalDifference` | Integer | Summary stats |
| `matchDataSource` | JRDataSource | Match history table (detail band) |
| `tournamentDataSource` | JRDataSource | Tournament participation table (summary band) |

The template renders **title + match history table + summary statistics + tournament table**. See `docs/jasper-learning.md` for the full walkthrough.

### Design or edit reports (Jaspersoft Studio)

1. Install [Jaspersoft Studio 6.20](https://community.jaspersoft.com/project/jaspersoft-studio) (match the library version in `build.gradle`).
2. Open `src/main/resources/reports/team_performance.jrxml`.
3. Design visually; keep parameter names in sync with `ReportService`.
4. Restart the app (templates are compiled on startup) or run `./gradlew bootRun` again.
5. Hit the report endpoint and verify the PDF.

**JRXML rules that bite newcomers:**

- XML comments cannot contain `--` (double hyphen).
- Every `<reportElement>` needs `x`, `y`, `width`, and `height`.
- DTO property names must match subDataset `<field name="…">` entries when using bean data sources.

### Gradle dependencies (JasperReports)

```gradle
implementation 'net.sf.jasperreports:jasperreports:6.20.0'
implementation 'net.sf.jasperreports:jasperreports-fonts:6.20.0'
implementation 'net.sf.jasperreports:jasperreports-functions:6.20.0'
```

Extra Jaspersoft Maven repos are configured in `build.gradle` for transitive artifacts.

---

## REST API (supporting the reports)

Base URL: `http://localhost:8080/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/worldcups` | List all World Cups |
| GET | `/worldcups/year/{year}` | World Cup by year (e.g. `2022`) |
| GET | `/worldcups/champion/{team}` | Titles won by team |
| GET | `/worldcups/host/{country}` | Tournaments hosted by country |
| GET | `/reports/team/{teamName}` | **Download team performance PDF** |

Interactive docs: http://localhost:8080/swagger-ui.html

---

## Project layout

```
src/main/
├── java/.../
│   ├── controller/     WorldCupController, ReportController
│   ├── service/        ReportService, WorldCupService
│   ├── config/         DataInitializer (seed data), OpenApiConfig
│   ├── entity/         WorldCup, Match
│   └── dto/            Request/response + report row DTOs
└── resources/
    ├── reports/        ← JRXML templates live here
    ├── jasperreports.properties
    └── application.yaml

src/test/.../controller/
    WorldCupE2ETest.java    API + PDF integration tests
    OpenApiE2ETest.java      Swagger / OpenAPI smoke tests
```

---

## Testing

```bash
./gradlew test
```

Includes end-to-end checks for World Cup APIs, PDF generation (status 200 + non-empty PDF), and OpenAPI endpoints.

---

## Troubleshooting reports

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `500` on `/reports/team/...` | JRXML failed to compile at startup | Check app logs for `Failed to compile report`; fix XML errors in `.jrxml` |
| `404` on report endpoint | Team has no matches in DB | Use a seeded team: `Argentina`, `France`, `Brazil`, `Germany` |
| Empty or missing flag | CDN unreachable | Expected — template uses `onErrorType="Blank"`; PDF still generates |
| `Report template not found` | Compile step failed silently | Fix JRXML, restart app, confirm log line `Compiled report: team_performance` |
| Database connection error | PostgreSQL not running | `docker compose up -d postgres` |

**Useful log filter after startup:**

```bash
./gradlew bootRun 2>&1 | grep -i jasper
```

---

## Further reading

- [docs/readme-full-guide.md](docs/readme-full-guide.md) — original full project guide (architecture, deployment, roadmap)
- [docs/jasper-learning.md](docs/jasper-learning.md) — parameters, subDatasets, tables, Java ↔ JRXML mapping
- [docs/report.md](docs/report.md) — original report design notes
- [JasperReports documentation](https://community.jaspersoft.com/documentation)

---

## Tech stack

| Layer | Choice |
|-------|--------|
| Runtime | Java 21, Spring Boot 4.1 |
| Database | PostgreSQL 15 |
| Reporting | JasperReports 6.20 |
| API docs | springdoc-openapi 3.x (Swagger UI) |
| Build | Gradle |
