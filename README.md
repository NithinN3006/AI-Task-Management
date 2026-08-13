# AI Task Management System

A full-stack, production-ready task management application built with Spring Boot, React, and MySQL. It features an AI-powered task detail generator using the Google Gemini API and a cryptographically verifiable "Blockchain-Style" Immutable Task Ledger.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.3, Spring Security, JWT (jjwt), Spring Data JPA, WebClient
- **Database**: PostgreSQL 15
- **Frontend**: React 18, Vite, Tailwind CSS v4, Zustand, Axios, React Hook Form, Zod

## Features
- **Authentication**: Secure JWT-based registration and login with BCrypt password hashing.
- **Task Management**: Kanban-style dashboard for tasks (To Do, In Progress, Done). Supports full CRUD and soft deletion.
- **AI Automation**: Integrated with Google Gemini API (`generativelanguage.googleapis.com`) to generate task descriptions, priorities, and estimated hours based on task titles using structured JSON output. Features graceful fallbacks if the API key is not provided.
- **Blockchain-Style Ledger (Bonus)**: An immutable task history. Every action (CREATE, UPDATE, DELETE, STATUS_CHANGE) is snapshotted into JSON with canonical property sorting and chained together using SHA-256 hashes of `(payload_snapshot + prev_hash + timestamp)`. A dedicated Verification API reconstructs the chain to detect tampering.

## Getting Started

### Local Development (with Docker)

1. Clone the repository and navigate to the root directory.
2. Provide your Gemini API key (optional but recommended for AI features):
   ```bash
   export GEMINI_API_KEY="your_api_key_here"
   ```
3. Start the Backend and PostgreSQL database using Docker Compose:
   ```bash
   docker-compose up --build -d
   ```
   *The backend will be available at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui.html`.*

4. Start the Frontend Development Server:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *The frontend will be available at `http://localhost:5173`.*

### Deployment Instructions

#### Database (Neon PostgreSQL)
1. Create a PostgreSQL project on Neon.
2. Obtain the connection string (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD).

#### Backend (Render)
1. Connect your GitHub repository to Render and deploy a "Web Service".
2. Set the Root Directory to `backend`.
3. Use `mvn clean install` for the build command and `java -jar target/backend-0.0.1-SNAPSHOT.jar` for the start command.
4. Configure the following environment variables:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (from Neon)
   - `JWT_SECRET` (generate a strong 256-bit random string)
   - `GEMINI_API_KEY` (your Google AI Studio API key)
   - `FRONTEND_URL` (the Vercel production URL, e.g., `https://my-app.vercel.app`)
5. Note: Ensure Render uses Java 21 for the build environment.

#### Frontend (Vercel)
1. Connect your GitHub repository to Vercel and deploy the `/frontend` folder.
2. Select `Vite` as the framework preset.
3. Set the following environment variable:
   - `VITE_API_BASE_URL`: The deployed URL of your Render backend + `/api` (e.g., `https://my-backend.onrender.com/api`).

## API Endpoints

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and receive JWT
- `GET /api/tasks` - List tasks (supports `?status=` and `?priority=` filters)
- `POST /api/tasks` - Create a task
- `PUT /api/tasks/{id}` - Update a task
- `PATCH /api/tasks/{id}/status` - Update task status
- `DELETE /api/tasks/{id}` - Soft delete a task
- `POST /api/ai/generate-task-details` - Generate AI task suggestions
- `GET /api/tasks/{id}/history` - Fetch full cryptographic ledger history
- `GET /api/tasks/{id}/verify` - Recomputes and verifies the hash chain

## Architecture Overview
The backend follows a standard layered architecture: **Controller → Service → Repository**.
- **Controllers** handle HTTP routing and parameter validation (`@Valid`).
- **Services** encapsulate business logic, including AI external calls (`AiService`) and hash generation (`LedgerService`).
- **Repositories** interact with MySQL via Spring Data JPA.

The `LedgerService` hooks into the `TaskService` directly after any successful database commit, ensuring that every state change generates a new block in the task's hash chain.
