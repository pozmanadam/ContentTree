# Content Tree Demo

- `backend/` - Spring Boot API
- `frontend/` - Vue Flow UI

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

### Features

- Creating new nodes only possible if you selected one.
- Deleting a node also removes all the child nodes.
  - Cannot delete the root node.
  - Deleting the root node only removes the child nodes.
- Update the selected node name and content.
- Searching within the nodes name and content.
- Horizontal and Vertical layout.
- Reoganising possible via drag and drop.
  - Selected node cannot be intersected with multiple nodes.   
  - The selected node also carries the child nodes.
  - If the target node is a descedant of the selected node only carries the childs up until the target node. Meanwhile the target node parent will be the selected node previous parent.  

