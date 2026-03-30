# FIFA World Cup 2026 API

> This is a personal project to keep practicing creating RESTful APIs using Java. More will be added to this project for different applications I will be building with different tech stacks in the near future.

A read-only REST API serving FIFA World Cup 2026 team and match (mock) data.

---

## Technology

- **Java 25**
- **Spring Boot 4.0.5**
- **Spring Data JPA** — repository layer and ORM
- **MySQL 8.0+** — database
- **Lombok** — boilerplate reduction
- **springdoc-openapi 3.0.2** — auto-generated API docs and Swagger UI

---

## Prerequisites

- Java 25+
- Maven
- MySQL 8.0+
- Python 3 with `mysql-connector-python` installed

---

## Setup

### 1. Database

The project includes a Python script that creates the database, tables, and seeds all data — 48 teams with full squads and 104 matches (72 group stage + 32 knockout). Match dates are generated dynamically starting from tomorrow relative to when the script is run.

Install the required Python dependency:

```bash
pip install mysql-connector-python
```

Run the setup script:

```bash
python src/main/resources/tools/setup_worldcup.py
```

The script will prompt for your MySQL root password and handle everything else.

### 2. Environment Variable

The app reads the database password from an environment variable. Set it before running:

```bash
# macOS / Linux
export DB_PASSWORD=your_mysql_password

# Windows (Command Prompt)
set DB_PASSWORD=your_mysql_password

# Windows (PowerShell)
$env:DB_PASSWORD="your_mysql_password"
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

---

## API Docs

Once running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

---

## Endpoints

### Teams

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/teams` | Get all teams |
| GET | `/api/teams/{id}` | Get team by ID |
| GET | `/api/teams/group/{group}` | Get teams by group (A–L) |

### Events

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events` | Get all events |
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/group/{group}` | Get events by group (A–L) |
| GET | `/api/events/stage/{stage}` | Get events by stage |
| GET | `/api/events/status/{status}` | Get events by status |
| GET | `/api/events/team/{teamId}` | Get all events involving a team |

**Stage values:** `GROUP`, `ROUND_OF_32`, `ROUND_OF_16`, `QUARTERFINAL`, `SEMIFINAL`, `THIRD_PLACE`, `FINAL`

**Status values:** `SCHEDULED`, `IN_PROGRESS`, `HALFTIME`, `FINISHED`, `POSTPONED`, `CANCELLED`
