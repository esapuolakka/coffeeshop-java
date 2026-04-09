# Coffee Shop App

Spring Boot + PostgreSQL coffee shop application.

## Run Locally With Docker

### Prerequisites

- Docker Desktop installed and running
- Docker Compose plugin available (the `docker compose` command)

### Start the app

From the project root, run:

```bash
docker compose up -d --build
```

This starts:

- `coffeeshop` app on port `8080`
- `db` PostgreSQL on port `5432`

Open in browser:

- App: `http://localhost:8080`
- Login page: `http://localhost:8080/login`

Default admin user is created automatically at startup:

- Username: `admin`
- Password: `admin123`

You can override these with environment variables:

- `APP_ADMIN_USERNAME`
- `APP_ADMIN_PASSWORD`

### Stop containers

```bash
docker compose down
```

### Rebuild after code changes

```bash
docker compose up -d --build coffeeshop
```

### View logs

```bash
docker compose logs -f coffeeshop
docker compose logs -f db
```

### Reset database (fresh seed data)

If you want to reinitialize PostgreSQL from scratch:

```bash
docker compose down -v
docker compose up -d --build
```

This removes the persistent database volume and recreates it.

## Run Without Docker

See the src/main/resources/application.properties file

### Dev profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Prod profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
