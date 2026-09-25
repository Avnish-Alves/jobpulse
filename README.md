# JobPulse

JobPulse is a Spring Boot REST API for tracking job applications. It provides JWT-based authentication, per-user application data, and a scheduled follow-up job that flags applications whose follow-up date has passed.

## Tech stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA / Hibernate
- H2 for local development
- PostgreSQL for production
- JWT (JJWT)
- Maven
- JUnit 5 / Mockito

## Features

- User registration and login
- BCrypt password hashing
- Stateless JWT authentication
- Per-user access to job applications
- Create, list, update, and delete applications
- Application status tracking with a stats summary endpoint
- Scheduled follow-up processing
- H2 development profile
- PostgreSQL production profile using environment variables
- Unit and Spring context tests

## Run locally

Requirements:

- Java 21
- Maven 3.9+

The default `dev` profile uses an in-memory H2 database, so no database setup is required for local development.

```bash
mvn spring-boot:run
```

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"password123"}'
```

The response contains a JWT. Use that token for protected endpoints.

### Create an application

```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"company":"Acme","role":"SDE 1","dateApplied":"2026-09-08","followUpDue":"2026-09-10"}'
```

### List applications

```bash
curl http://localhost:8080/api/applications \
  -H "Authorization: Bearer <token>"
```

The H2 console is available in the development profile at `http://localhost:8080/h2-console`.

JDBC URL:

```text
jdbc:h2:mem:jobpulse
```

## Tests

Run the test suite with:

```bash
mvn clean test
```

The test suite covers authentication service behavior, the scheduled follow-up job, and Spring application context startup.

## Production configuration

The `prod` profile expects database and JWT configuration through environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
```

Start the application with:

```bash
java -jar target/jobpulse-0.1.0.jar --spring.profiles.active=prod
```

## API overviews

| Method | Endpoint | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | No | Create an account and return a JWT |
| POST | `/api/auth/login` | No | Authenticate and return a JWT |
| POST | `/api/applications` | Yes | Create a job application |
| GET | `/api/applications` | Yes | List the current user's applications |
| PATCH | `/api/applications/{id}/status` | Yes | Update an application's status |
| GET | `/api/applications/follow-ups` | Yes | List applications flagged for follow-up |
| GET | `/api/applications/stats` | Yes | Summary counts: total, flagged, and per-status breakdown |
| DELETE | `/api/applications/{id}` | Yes | Delete an application |
