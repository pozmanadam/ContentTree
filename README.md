# Content Tree Demo

Monorepo containing:
- `backend/` - Spring Boot API
- `frontend/` - Vue UI

## Requirements

- Docker Desktop
- Optional for local development:
  - Java 17
  - Maven 3.9+
  - Node.js 20+

## Run With Docker

From the repository root:

```powershell
docker compose up -d --build
```

Application URLs:
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Backend check endpoint: `http://localhost:8080/api/tree/hello`

Stop the containers:

```powershell
docker compose down
```

Check running services:

```powershell
docker compose ps
```

View logs:

```powershell
docker compose logs -f
```

## Run Locally Without Docker

### Backend

From `backend/`:

```powershell
cd backend
mvn spring-boot:run
```

Backend runs on:
- `http://localhost:8080`

### Frontend

From `frontend/`:

```powershell
cd frontend
npm install
npm run dev
```

Frontend runs on:
- `http://localhost:5173`

The frontend calls the backend at:
- `http://localhost:8080/api`

## Run Tests

### Backend Tests Locally

From `backend/`:

```powershell
cd backend
mvn clean test
```

### Backend Tests In Docker

From the repository root:

```powershell
docker run --rm -v "${PWD}/backend:/workspace" -w /workspace maven:3.9.12-eclipse-temurin-17 mvn clean test
```

## Project Layout

```text
.
├─ backend/
├─ frontend/
├─ compose.yaml
└─ README.md
```

## Notes

- Docker builds the frontend with `VITE_API_URL=http://localhost:8080/api`
- Backend CORS allows `http://localhost:5173`
