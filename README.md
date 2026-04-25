# Birthday Notification System

## Overview

`birthday-notification` is a Spring Boot application for managing employees and sending birthday greetings automatically, mainly via email and WhatsApp.

## Tech Stack

- Java 21
- Spring Boot
- Thymeleaf + Bootstrap
- PostgreSQL
- Maven
- Spring Mail
- Docker / Docker Compose

## Quick Start

### Run with Docker Compose

This project already includes a `docker-compose.yml` with:

- `db`: PostgreSQL 16
- `app`: Spring Boot application
- a bind mount for `Info/Database/Script.sql` to initialize the database on the first run
- bind mounts for `src`, `static`, and `templates` for development convenience

Start everything:

```bash
docker compose up -d --build
```

Open the app at:

```text
http://localhost:8080
```

Check logs:

```bash
docker compose logs -f app
docker compose logs -f db
```

Stop containers:

```bash
docker compose down
```

If you want to remove PostgreSQL data too, use:

```bash
docker compose down -v
```

Notes:

- The current compose file uses `host.docker.internal:5432` for the app datasource, which works with Docker Desktop on Windows.
- The SQL script in `Info/Database/Script.sql` runs only the first time the `pgdata` volume is created.
- If you change the database schema or want a fresh init, remove the volume with `docker compose down -v`.

### Run locally

1. Make sure Java 21, Maven, and PostgreSQL are installed.
2. Create the database:

```text
CREATE DATABASE birthday_notification;
```

3. Update database settings in `src/main/resources/application.yaml` or override them with environment variables.
4. Start the app:

```bash
./mvnw spring-boot:run
```

Or build a JAR:

```bash
./mvnw clean package
java -jar target/birthday-notification-*.jar
```

## Configuration

Common environment variables:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `GEMINI_API_KEY`

Default values are defined in `src/main/resources/application.yaml`.

## Database Initialization

The SQL script is stored at `Info/Database/Script.sql`.

To initialize manually:

```bash
psql -U postgres -d birthday_notification -f Info/Database/Script.sql
```

If you use Docker Compose, the script can be mounted into the PostgreSQL container for first-time initialization.

## Notes

- The project already contains Docker support and a ready-made database script.
- Keep secrets out of Git; prefer environment variables for passwords and API keys.


# Init database;
#   MAC
docker exec -i <container_id> psql -U postgres -d birthday_notification < Info/Database/Script.sql
#   Windows
docker cp Info/Database/Script.sql <container_id>:/tmp/script.sql
docker exec -i <container_id> psql -U postgres -d birthday_notification -f /tmp/script.sql