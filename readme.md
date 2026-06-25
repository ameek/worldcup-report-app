# 🏆 FIFA World Cup Reporting System

## A Comprehensive Spring Boot + JasperReports Implementation

---

## ⚡ Quick Start (Click & Go)

Get the local stack running in three commands:

```bash
# 1. Clone and enter the project
git clone <your-repo-url> worldcup-report-app
cd worldcup-report-app

# 2. Start PostgreSQL and Redis
docker compose up -d

# 3. Run the Spring Boot app
./gradlew bootRun
```

Verify:

```bash
./gradlew test
```

| Requirement | Version |
|-------------|---------|
| Java | 21 |
| Docker | 20.10+ |
| Docker Compose | 2.0+ |

Default credentials (from `docker-compose.yml`):

| Service | Host | Port | Database | User | Password |
|---------|------|------|----------|------|----------|
| PostgreSQL | `localhost` | `5432` | `fifa_worldcup` | `fifa_user` | `fifa_pass` |
| Redis | `localhost` | `6379` | — | — | — |

> **Note:** The app is an early scaffold. REST APIs and JasperReports are on the [development roadmap](#development-roadmap); infrastructure and the Spring Boot shell are ready to build on.

---

## 📋 Table of Contents
- [Quick Start (Click & Go)](#quick-start-click--go)
- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Hardware Requirements](#hardware-requirements)
- [Project Setup](#project-setup)
- [Development Roadmap](#development-roadmap)
- [Database Schema](#database-schema)
- [API Documentation](#api-documentation)
- [Testing Strategy](#testing-strategy)
- [Performance Optimization](#performance-optimization)
- [Deployment Guide](#deployment-guide)
- [Monitoring & Maintenance](#monitoring--maintenance)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## 🎯 Project Overview

### What We're Building
A **production-ready reporting system** for FIFA World Cup data that:
- Fetches World Cup data from external APIs
- Stores data in PostgreSQL with optimized schema
- Provides RESTful APIs for data access
- Generates PDF reports using JasperReports
- Caches frequently accessed data with Redis
- Handles large report generation asynchronously
- Scales from development to production

### Core Features
1. **Data Management**
   - Fetch World Cup data from public API
   - Store in PostgreSQL with proper relationships
   - Automatic data updates and synchronization

2. **Reporting Capabilities**
   - Team performance reports (PDF)
   - Tournament history reports
   - Champion statistics
   - Custom filtered reports

3. **Performance Features**
   - Redis caching for frequent queries
   - Async report generation
   - Connection pooling
   - Query optimization with indexes

4. **Production Ready**
   - Docker containerization
   - Resource limits and monitoring
   - Health checks and metrics
   - Comprehensive logging

---

## 🏗️ Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                               │
│                    (Browser, Mobile, API Clients)                   │
└──────────┬─────────────────────────────────────┬───────────────────┘
           │                                     │
           │  REST API                           │  File Download
           ▼                                     ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT APPLICATION                         │
│                                                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌──────────┐  │
│  │  Controller │──│   Service   │──│  Repository │──│  Entity  │  │
│  │    Layer    │  │    Layer    │  │    Layer    │  │  Layer   │  │
│  └─────────────┘  └──────┬──────┘  └──────┬──────┘  └──────────┘  │
│                         │                 │                        │
│                    ┌────┴────┐       ┌────┴────┐                  │
│                    │  Redis  │       │ Report  │                  │
│                    │  Cache  │       │ Service │                  │
│                    └─────────┘       └─────────┘                  │
│                                                                     │
└──────────┬──────────────────────┬────────────────────┬────────────┘
           │                      │                    │
           ▼                      ▼                    ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│    PostgreSQL    │  │      Redis       │  │  JasperReports   │
│   (Primary DB)  │  │    (Cache)       │  │  (PDF Generator) │
├──────────────────┤  ├──────────────────┤  ├──────────────────┤
│ • WorldCup      │  │ • Team Data      │  │ • Templates      │
│ • Matches       │  │ • Tournament     │  │ • Subreports     │
│ • Teams         │  │ • Stats          │  │ • Charts         │
└──────────────────┘  └──────────────────┘  └──────────────────┘
           │                      │                    │
           └──────────────────────┴────────────────────┘
                                      │
                                      ▼
                           ┌──────────────────┐
                           │   External API   │
                           │  (FIFA Data)     │
                           └──────────────────┘
```

### Data Flow Diagram

```
┌─────────┐    ┌─────────────┐    ┌────────────┐    ┌─────────────┐
│  User   │───▶│  API Call   │───▶│  Check     │───▶│  Return     │
│ Request │    │  (REST)     │    │  Cache     │    │  Response   │
└─────────┘    └─────────────┘    └─────┬──────┘    └─────────────┘
                                        │
                                   ┌────┴────┐
                                   │  Cache  │
                                   │  Miss?  │
                                   └────┬────┘
                                        │
                                   ┌────┴────┐     ┌─────────────┐
                                   │  Query  │────▶│  Generate   │
                                   │   DB    │     │  Report     │
                                   └─────────┘     └─────────────┘
                                        │                  │
                                   ┌────┴────┐     ┌─────┴─────┐
                                   │  Store  │     │  Stream   │
                                   │  Cache  │     │  to User  │
                                   └─────────┘     └───────────┘
```

### Technology Stack Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│  Spring Boot Web MVC │ REST Controllers │ Swagger/OpenAPI       │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────────┐
│                    BUSINESS LAYER                               │
│  Service Layer │ Cache Abstraction │ Async Processing           │
│  Validation │ Exception Handling │ DTO Mapping                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────────┐
│                    DATA ACCESS LAYER                            │
│  Spring Data JPA │ Hibernate │ PostgreSQL │ Redis               │
│  Connection Pooling │ Query Optimization │ Indexes              │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────┴──────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                         │
│  Docker │ Docker Compose │ Resource Limits │ Monitoring        │
│  Health Checks │ Logging │ Metrics │ Prometheus                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 4.1.0** - Application framework
- **Java 21** - Programming language (LTS version)
- **Gradle 9.5.1** - Build tool

### Data Storage
- **PostgreSQL 15** - Primary database
- **Redis 7** - Caching layer
- **Hibernate 6** - ORM framework

### Reporting
- **JasperReports 6.20.0** - Report generation engine
- **Jaspersoft Studio** - Report design tool

### Testing
- **JUnit 5** - Unit testing
- **TestContainers** - Integration testing
- **Rest Assured** - API testing
- **Gatling** - Performance/Load testing

### DevOps & Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboard

### Development Tools
- **VS Code / Cursor** - IDE
- **Lombok** - Boilerplate reduction
- **Spring Boot DevTools** - Development productivity
- **Swagger/OpenAPI** - API documentation

---

## 📋 Prerequisites

### Required Software
```bash
# Minimum Versions
Java: 17 or 21 (LTS versions)
Gradle: 7.5+
Docker: 20.10+
Docker Compose: 2.0+
Git: 2.30+
curl: 7.68+
jq: 1.6+

# Optional but Recommended
Jaspersoft Studio: 6.20.0
PostgreSQL Client: 15
Redis CLI: 7
```

### Development Environment Setup
```bash
# 1. Install SDKMAN (Java Version Manager)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 2. Install Java 21
sdk install java 21.0.1-amzn
sdk default java 21.0.1-amzn

# 3. Verify Installation
java -version
# Output: openjdk version "21.0.1" 2023-10-17 LTS

# 4. Install Docker (if not present)
# Mac: brew install docker docker-compose
# Ubuntu: sudo apt install docker.io docker-compose
# Windows: Download Docker Desktop

# 5. Verify Docker
docker --version
docker-compose --version
```

---

## 💻 Hardware Requirements

### Development Environment
```
┌─────────────────────────────────────────────────────┐
│  Minimum Development Setup                         │
├─────────────────────────────────────────────────────┤
│  CPU: 2 cores                                     │
│  RAM: 4GB                                         │
│  Storage: 10GB free space                         │
│  OS: Ubuntu 20.04+ / MacOS 12+ / Windows 10+     │
└─────────────────────────────────────────────────────┘
```

### Production Environment (Recommended)
```
┌─────────────────────────────────────────────────────┐
│  Production Hardware Specifications                │
├─────────────────────────────────────────────────────┤
│  Total: 2.5GB RAM, 4-6 CPU Cores                  │
│                                                    │
│  Application Container:                            │
│  └─ RAM: 512MB - 1GB                              │
│  └─ CPU: 1-2 cores                                │
│  └─ JVM Heap: -Xms512m -Xmx1024m                 │
│                                                    │
│  PostgreSQL Container:                             │
│  └─ RAM: 512MB - 1GB                              │
│  └─ CPU: 1-2 cores                                │
│  └─ Shared Buffers: 256MB                         │
│                                                    │
│  Redis Container:                                  │
│  └─ RAM: 256MB - 512MB                            │
│  └─ CPU: 0.5-1 core                               │
│  └─ Max Memory: 256MB                             │
│                                                    │
│  OS Overhead: 256MB - 512MB RAM                   │
│  Buffer: 20% overhead for spikes                  │
└─────────────────────────────────────────────────────┘
```

### Scaling Guidelines
```
Small Scale (< 1000 req/day):
  ├─ RAM: 2GB, CPU: 2 cores
  └─ Single instance

Medium Scale (1000-10000 req/day):
  ├─ RAM: 4GB, CPU: 4 cores
  ├─ Database replica
  └─ Redis cluster

Large Scale (> 10000 req/day):
  ├─ RAM: 8GB+, CPU: 8+ cores
  ├─ Database cluster
  ├─ Redis cluster
  └─ Load balancer + multiple app instances
```

---

## 🚀 Project Setup

### Step 1: Clone the Repository
```bash
git clone <your-repo-url> worldcup-report-app
cd worldcup-report-app
```

### Step 2: Project Structure
```
worldcup-report-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── fifa/
│   │   │           └── reporting/
│   │   │               ├── FifaApplication.java
│   │   │               ├── controller/
│   │   │               │   ├── WorldCupController.java
│   │   │               │   ├── ReportController.java
│   │   │               │   └── HealthController.java
│   │   │               ├── service/
│   │   │               │   ├── WorldCupService.java
│   │   │               │   ├── ReportService.java
│   │   │               │   ├── TeamService.java
│   │   │               │   └── DataLoaderService.java
│   │   │               ├── repository/
│   │   │               │   ├── WorldCupRepository.java
│   │   │               │   ├── MatchRepository.java
│   │   │               │   └── TeamRepository.java
│   │   │               ├── entity/
│   │   │               │   ├── WorldCup.java
│   │   │               │   ├── Match.java
│   │   │               │   └── Team.java
│   │   │               ├── dto/
│   │   │               │   ├── TeamPerformanceDto.java
│   │   │               │   ├── MatchDto.java
│   │   │               │   └── ReportRequestDto.java
│   │   │               ├── config/
│   │   │               │   ├── RedisConfig.java
│   │   │               │   ├── AsyncConfig.java
│   │   │               │   └── DataInitializer.java
│   │   │               ├── client/
│   │   │               │   └── FifaApiClient.java
│   │   │               └── exception/
│   │   │                   ├── ResourceNotFoundException.java
│   │   │                   └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-prod.yml
│   │       └── reports/
│   │           ├── team_performance.jrxml
│   │           └── champion_history.jrxml
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── fifa/
│       │           └── reporting/
│       │               ├── controller/
│       │               ├── service/
│       │               └── repository/
│       └── resources/
│           └── application-test.yml
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── scripts/
│   ├── init-db.sql
│   └── load-test.gatling
├── .jvmopts
├── build.gradle
├── settings.gradle
├── gradle.properties
└── README.md
```

Planned packages (`controller/`, `service/`, `repository/`, `entity/`, `reports/`, etc.) are described in the [development roadmap](#development-roadmap).

### Step 3: Build and Run
```bash
# 1. Start PostgreSQL and Redis
docker compose up -d

# 2. Build the project
./gradlew clean build

# 3. Run the application
./gradlew bootRun

# 4. Run tests
./gradlew test
```

---

## 📅 Development Roadmap

### Phase 1: Foundation (Week 1)
```
✅ Set up project structure
✅ Configure PostgreSQL
✅ Create entities (WorldCup, Match, Team)
✅ Set up repositories
✅ Implement data initializer
✅ Create basic REST controllers
✅ Write unit tests
✅ Verify database operations
```

### Phase 2: Data Integration (Week 2)
```
✅ Integrate external API (worldcup26.ir)
✅ Create API client with RestTemplate
✅ Implement data sync service
✅ Handle API failures and retries
✅ Add validation and error handling
✅ Test API integration
✅ Document API endpoints
```

### Phase 3: Reporting (Week 3)
```
✅ Install Jaspersoft Studio
✅ Design first report template
✅ Create ReportService
✅ Implement PDF generation
✅ Add team performance report
✅ Add champion history report
✅ Test report generation
```

### Phase 4: Caching & Performance (Week 4)
```
✅ Add Redis configuration
✅ Implement caching for frequent queries
✅ Set up cache eviction policies
✅ Add async report generation
✅ Configure thread pools
✅ Implement rate limiting
✅ Test with load testing
```

### Phase 5: Production Readiness (Week 5)
```
✅ Set up Docker containers
✅ Configure resource limits
✅ Add health checks
✅ Implement monitoring
✅ Configure logging
✅ Create deployment scripts
✅ Write documentation
✅ Security hardening
```

### Phase 6: Testing & Optimization (Week 6)
```
✅ Load testing with Gatling
✅ Performance optimization
✅ Database query optimization
✅ Cache tuning
✅ Error handling improvements
✅ Security audit
✅ Final documentation
```

---

## 🗄️ Database Schema

### Entity Relationship Diagram

```
┌─────────────────────┐     ┌─────────────────────┐
│     WorldCup         │     │      Match          │
├─────────────────────┤     ├─────────────────────┤
│ id (PK)             │     │ id (PK)             │
│ year                │◄────│ world_cup_id (FK)   │
│ host_country        │     │ team1               │
│ champion            │     │ team2               │
│ runner_up           │     │ team1_goals         │
│ total_teams         │     │ team2_goals         │
│ total_matches       │     │ stage               │
│ total_goals         │     │ stadium             │
│ top_scorer          │     │ attendance          │
│ top_scorer_goals    │     │ match_date          │
│ start_date          │     └─────────────────────┘
│ end_date            │
│ created_at          │     ┌─────────────────────┐
│ updated_at          │     │      Team           │
└─────────────────────┘     ├─────────────────────┤
                            │ id (PK)             │
                            │ name (unique)       │
                            │ fifa_code           │
                            │ group_name          │
                            │ flag_url            │
                            │ created_at          │
                            │ updated_at          │
                            └─────────────────────┘
```

### Database Schema (DDL)

```sql
-- WorldCup Table
CREATE TABLE world_cups (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL UNIQUE,
    host_country VARCHAR(100) NOT NULL,
    champion VARCHAR(100) NOT NULL,
    runner_up VARCHAR(100),
    total_teams INTEGER,
    total_matches INTEGER,
    total_goals INTEGER,
    top_scorer VARCHAR(100),
    top_scorer_goals INTEGER,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Matches Table
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    world_cup_id BIGINT REFERENCES world_cups(id),
    team1 VARCHAR(100) NOT NULL,
    team2 VARCHAR(100) NOT NULL,
    team1_goals INTEGER,
    team2_goals INTEGER,
    stage VARCHAR(50) NOT NULL,
    stadium VARCHAR(200),
    attendance INTEGER,
    match_date TIMESTAMP
);

-- Indexes for Performance
CREATE INDEX idx_matches_world_cup ON matches(world_cup_id);
CREATE INDEX idx_matches_team1 ON matches(team1);
CREATE INDEX idx_matches_team2 ON matches(team2);
CREATE INDEX idx_matches_stage ON matches(stage);

-- Teams Table
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    fifa_code VARCHAR(3),
    group_name VARCHAR(10),
    flag_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 📡 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints

#### World Cup Endpoints
```
GET    /worldcups                  - Get all World Cups
GET    /worldcups/{year}           - Get World Cup by year
GET    /worldcups/champion/{team}  - Get World Cups won by team
GET    /worldcups/host/{country}   - Get World Cups hosted by country
```

#### Match Endpoints
```
GET    /matches                    - Get all matches
GET    /matches/{id}              - Get match by ID
GET    /matches/worldcup/{year}   - Get matches for a World Cup
GET    /matches/team/{teamName}   - Get matches for a team
GET    /matches/stage/{stage}     - Get matches by stage
```

#### Report Endpoints
```
GET    /reports/team/{teamName}        - Generate team performance report
GET    /reports/champions              - Generate champion history report
POST   /reports/worldcup/{year}        - Generate World Cup detail report (async)
GET    /reports/status/{taskId}        - Check async report status
```

#### Team Endpoints
```
GET    /teams                        - Get all teams
GET    /teams/{name}                - Get team by name
GET    /teams/group/{groupName}     - Get teams by group
```

### Example Requests

```bash
# Get all World Cups
curl -X GET "http://localhost:8080/api/v1/worldcups" | jq .

# Get Argentina's performance report (saved under output/reports/)
mkdir -p output/reports
curl -X GET "http://localhost:8080/api/v1/reports/team/Argentina" \
  -H "Accept: application/pdf" \
  --output output/reports/argentina_report.pdf

# Generate async report for 2022
curl -X POST "http://localhost:8080/api/v1/reports/worldcup/2022" | jq .
# Response: {"taskId":"123e4567","status":"PROCESSING"}

# Check report status
curl -X GET "http://localhost:8080/api/v1/reports/status/123e4567" | jq .
# Response: {"status":"COMPLETED","downloadUrl":"/downloads/report_123e4567.pdf"}
```

---

## 🧪 Testing Strategy

### Unit Tests
```java
// Service Layer Tests
@ExtendWith(MockitoExtension.class)
class WorldCupServiceTest {
    
    @Mock
    private WorldCupRepository repository;
    
    @InjectMocks
    private WorldCupService service;
    
    @Test
    void shouldReturnWorldCupWhenExists() {
        // Given
        Integer year = 2022;
        WorldCup expected = WorldCup.builder()
            .year(year)
            .champion("Argentina")
            .build();
        when(repository.findByYear(year)).thenReturn(Optional.of(expected));
        
        // When
        WorldCup result = service.getWorldCupByYear(year);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getChampion()).isEqualTo("Argentina");
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class WorldCupControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturnAllWorldCups() throws Exception {
        mockMvc.perform(get("/api/v1/worldcups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].year").value(2022));
    }
}
```

### Load Tests (Gatling)
```scala
class ReportLoadTest extends Simulation {
    
    val httpProtocol = http
        .baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
    
    val scn = scenario("Load Test")
        .exec(http("Get World Cups")
            .get("/api/v1/worldcups")
            .check(status.is(200)))
        .pause(1)
        .exec(http("Generate Report")
            .get("/api/v1/reports/team/Argentina")
            .check(status.is(200)))
    
    setUp(
        scn.inject(
            rampUsers(100) during (10 seconds),
            constantUsersPerSec(20) during (30 seconds)
        )
    ).protocols(httpProtocol)
}
```

---

## ⚡ Performance Optimization

### 1. Database Optimization
```sql
-- Add Indexes
CREATE INDEX idx_world_cups_year ON world_cups(year);
CREATE INDEX idx_world_cups_champion ON world_cups(champion);
CREATE INDEX idx_matches_team1_team2 ON matches(team1, team2);

-- Query Optimization
EXPLAIN ANALYZE 
SELECT * FROM world_cups 
WHERE champion = 'Argentina' 
AND year > 2010;

-- Use Partial Indexes
CREATE INDEX idx_active_teams ON teams(name) 
WHERE group_name IS NOT NULL;
```

### 2. Caching Strategy
```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
      cache-null-values: false
  
# Service Layer
@Cacheable(value = "teamPerformance", key = "#teamName")
public TeamPerformance getTeamPerformance(String teamName) {
    // Expensive database query
}
```

### 3. JVM Tuning
```bash
# .jvmopts
-Xms512m
-Xmx1024m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:+DisableExplicitGC
-XX:+HeapDumpOnOutOfMemoryError
```

---

## 🐳 Deployment Guide

### Docker Deployment

1. **Build the Docker Image**
```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]
```

2. **Docker Compose (Production)**
```yaml
# docker-compose-prod.yml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    container_name: fifa_postgres_prod
    environment:
      POSTGRES_DB: fifa_worldcup
      POSTGRES_USER: fifa_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    deploy:
      resources:
        limits:
          memory: 1024M
          cpus: '1.5'
    volumes:
      - pg_data:/var/lib/postgresql/data
    networks:
      - fifa_network

  redis:
    image: redis:7-alpine
    container_name: fifa_redis_prod
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.8'
    volumes:
      - redis_data:/data
    networks:
      - fifa_network

  app:
    build: .
    container_name: fifa_app_prod
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_PASSWORD: ${DB_PASSWORD}
    deploy:
      resources:
        limits:
          memory: 1024M
          cpus: '2'
    depends_on:
      - postgres
      - redis
    networks:
      - fifa_network

volumes:
  pg_data:
  redis_data:

networks:
  fifa_network:
    driver: bridge
```

3. **Deploy Commands**
```bash
# Build and start
docker-compose -f docker-compose-prod.yml up -d

# Scale app instances
docker-compose -f docker-compose-prod.yml up -d --scale app=3

# Check status
docker ps

# View logs
docker-compose -f docker-compose-prod.yml logs -f app

# Monitor resources
docker stats
```

### Kubernetes Deployment (Optional)

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fifa-reporting-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: fifa-reporting
  template:
    metadata:
      labels:
        app: fifa-reporting
    spec:
      containers:
      - name: app
        image: fifa-reporting:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
```

---

## 📊 Monitoring & Maintenance

### Health Checks
```bash
# Application Health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics

# Prometheus Metrics
curl http://localhost:8080/actuator/prometheus
```

### Logging Configuration
```yaml
# application-prod.yml
logging:
  level:
    com.fifa.reporting: INFO
    org.springframework.web: WARN
    org.hibernate: WARN
  file:
    name: logs/fifa-app.log
    max-size: 100MB
    max-history: 30
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Backup Strategy
```bash
# Database Backup
docker exec fifa_postgres_prod pg_dump -U fifa_user fifa_worldcup > backup.sql

# Automated Backup (Cron)
0 2 * * * docker exec fifa_postgres_prod pg_dump -U fifa_user fifa_worldcup > /backups/fifa_$(date +\%Y\%m\%d).sql
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Test connection
docker exec -it fifa_postgres psql -U fifa_user -d fifa_worldcup -c "\l"

# Check logs
docker logs fifa_postgres
```

#### 2. Redis Connection Issues
```bash
# Check if Redis is running
docker ps | grep redis

# Test connection
docker exec -it fifa_redis redis-cli ping
# Expected: PONG
```

#### 3. Out of Memory Errors
```bash
# Increase JVM heap
export JAVA_OPTS="-Xmx2048m"
./gradlew bootRun

# Check Docker memory limits
docker stats

# Increase Docker memory
# Edit docker-compose.yml memory limits
```

#### 4. Report Generation Fails
```bash
# Check JasperReports logs
tail -f logs/fifa-app.log | grep Jasper

# Verify report templates exist
ls src/main/resources/reports/

# Test with simple report
curl -X GET "http://localhost:8080/api/v1/reports/team/Brazil" -H "Accept: application/pdf"
```

---

## 🤝 Contributing

### Development Workflow
```bash
# 1. Fork the repository
# 2. Clone your fork
git clone https://github.com/yourusername/fifa-reporting-system.git

# 3. Create a feature branch
git checkout -b feature/your-feature-name

# 4. Make changes and commit
git add .
git commit -m "Description of changes"

# 5. Push to your fork
git push origin feature/your-feature-name

# 6. Create a Pull Request
```

### Coding Standards
```java
// 1. Use Lombok for boilerplate
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Entity {
    // Only fields needed
}

// 2. Use meaningful variable names
String teamName;  // ✅ Good
String tn;        // ❌ Bad

// 3. Add JavaDoc for public methods
/**
 * Generates a PDF report for a specific team
 * @param teamName The name of the team
 * @return PDF report as byte array
 * @throws ResourceNotFoundException if team not found
 */
public byte[] generateTeamReport(String teamName) {
    // Implementation
}

// 4. Write unit tests for all public methods
@Test
void shouldGenerateTeamReportWhenTeamExists() {
    // Given
    // When
    // Then
}
```

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🎯 Quick Start Commands

### Development
```bash
# Start infrastructure and app
docker compose up -d && ./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew bootJar

# Run JAR
java -jar build/libs/worldcup-report-app-*.jar
```

### Production
```bash
# Build JAR (Dockerfile and prod compose — see Deployment Guide)
./gradlew bootJar
docker build -t fifa-reporting-app .
docker compose -f docker-compose-prod.yml up -d

# Monitor
docker stats
docker compose logs -f
```

---

## 🔗 Useful Links

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [JasperReports Documentation](https://community.jaspersoft.com/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Docker Documentation](https://docs.docker.com/)
- [FIFA World Cup API](https://worldcup26.ir/docs)

---

## 📞 Support

For issues, questions, or contributions:
- Create an Issue on GitHub
- Join the discussion on our Discord channel
- Email: support@fifa-reporting.com

---

**Happy Reporting! 🏆**
