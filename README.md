# Content Tree Demo

- `backend/` - Spring Boot API
- `frontend/` - Vue UI

## Requirements

- Docker
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
- Backend check endpoint: `http://localhost:8080/api/tree/hello`

Stop the containers:

```powershell
docker compose down
```

Check running services:

```powershell
docker compose ps
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

## Run Tests

### Backend Tests Locally

From `backend/`:

```powershell
cd backend
mvn clean test
```

Jacoco test coverage report availale at:
/backend/target/site/jacoco/index.html
