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

### 1. Database Setup

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
| POST | `/api/teams` | Create a new team (test database only) |
| PUT | `/api/teams/{id}` | Update a team (test database only) |
| DELETE | `/api/teams/{id}` | Delete a team (test database only) |

### Events

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/events` | Get all events |
| GET | `/api/events/{id}` | Get event by ID |
| GET | `/api/events/group/{group}` | Get events by group (A–L) |
| GET | `/api/events/stage/{stage}` | Get events by stage |
| GET | `/api/events/status/{status}` | Get events by status |
| GET | `/api/events/team/{teamId}` | Get all events involving a team |
| POST | `/api/events` | Create a new event (test database only) |
| PUT | `/api/events/{id}` | Update an event (test database only) |
| DELETE | `/api/events/{id}` | Delete an event (test database only) |

**Stage values:** `GROUP`, `ROUND_OF_32`, `ROUND_OF_16`, `QUARTERFINAL`, `SEMIFINAL`, `THIRD_PLACE`, `FINAL`

**Status values:** `SCHEDULED`, `IN_PROGRESS`, `HALFTIME`, `FINISHED`, `POSTPONED`, `CANCELLED`

---

## Multi-Tenancy

The API uses Hibernate's multi-tenancy support to route database queries to different MySQL schemas depending on the request context. This allows the application and its tests to operate against separate databases without any code changes.

### How It Works

Two schemas are used:

| Schema | Purpose |
|--------|---------|
| `fifa_world_cup` | Production schema, used for all normal requests |
| `fifa_world_cup_test` | Test schema, used when the test header is present |

On each request, a `HandlerInterceptor` reads an HTTP header and sets the active schema on a `ThreadLocal`. Hibernate's `CurrentTenantIdentifierResolver` reads that value to determine which schema to query, and `MultiTenantConnectionProvider` switches the JDBC connection to the correct MySQL catalog before executing any query. The `ThreadLocal` is cleared after each request completes.

### Switching to the Test Schema

To route a request to the test schema, include the following HTTP header:

```
X-DB-STATE: MODIFIED
```

Any request without this header, or with a different value, will use the production schema.

Example using curl:

```bash
# Production schema (default)
curl http://localhost:8080/api/teams

# Test schema
curl -H "X-DB-STATE: MODIFIED" http://localhost:8080/api/teams
```

### Configuration

The schema names are controlled by properties in `application.properties`:

```properties
app.tenant.default-schema=fifa_world_cup
app.tenant.test-schema=fifa_world_cup_test
```

The header name and value (`X-DB-STATE: MODIFIED`) are defined as constants in `ApiHeaders.java` so they live in one place and can be referenced across controllers, interceptors, and tests.

### Setup Script

The Python setup script (`setup_worldcup.py`) creates and seeds both schemas. Run it once before starting the application or running integration tests.

---

## CI

Tests run automatically via GitHub Actions on every push made by the repository owner. The workflow can also be triggered manually from the Actions tab in GitHub for other contributors.

### How It Works

A self-hosted GitHub Actions runner is hosted on a personal TrueNAS Scale server running as a Docker container. A shared MySQL container on the same Docker network provides the database for the test run. The runner connects to MySQL using the service name `fifa-mysql` as the hostname, which Docker resolves internally.

Tests run using the `ci` Spring profile, which reads from `src/test/resources/application-ci.properties`. Database credentials are stored as GitHub Actions secrets and injected into the runner as environment variables at runtime — they are not stored in the codebase.

### Running Tests Locally

```bash
./mvnw test
```

Local test runs use `src/main/resources/application.properties` and connect to your local MySQL instance. The `ci` profile is not activated locally so no additional setup is needed.
