# NexusAI

AI assistant platform with a **Java Spring Boot** backend and a **Next.js** frontend. The backend exposes REST APIs for chat, document upload/RAG, conversations, optional video generation, and subscription/payment hooks.

## Project structure

- `ai-assistant-backend/` — Spring Boot API
- `frontend/` — Next.js UI

Related docs: `DOCUMENT_TRAINING_README.md`, `ai-assistant-backend/README-SETUP.md`.

## Stack

**Backend:** Java 17, Spring Boot, Spring Data JPA, PostgreSQL, Apache Tika, SpringDoc OpenAPI  
**Frontend:** Next.js, TypeScript, Tailwind CSS, Supabase (auth/data)

Configure database and API keys through environment / local config. Do not commit secrets.

## Getting started

### Backend

```bash
cd ai-assistant-backend
./mvnw spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- App: `http://localhost:3000`

---

## Backend API overview

The backend exposes REST controllers including:

### Video Generation (`/api/video`)

- `POST /api/video/generate/text` — generate video from text
- `POST /api/video/generate/image` — generate video from image + prompt
- `GET /api/video/status/{taskId}` — task status
- `POST /api/video/cleanup` — clean up old tasks

Uses Replicate for generation where configured.

### Users (`/api/users`)

- `POST /api/users` — create or fetch user
- `GET /api/users?email={email}` — lookup by email

### Documents (`/api/documents`)

Upload and manage documents with text extraction (Apache Tika), including per-user and per-assistant listing, delete, counts, and context retrieval for AI queries.

### Vector documents / RAG (`/api/vector-documents`)

Chunking, embeddings, semantic search, RAG context, chunk statistics, performance metrics, and health checks.

### Conversations (`/api/conversations`)

Create conversations, list by user/assistant, fetch with messages, append messages, delete.

### AI chat (`/api/ai`)

- `POST /api/ai/chat` — generate a response (supports multiple model backends when configured; can use document RAG context)

### Payment (`/api/payment`)

SSLCommerz-oriented payment initiate/status/validate/success/fail/cancel and IPN handling when credentials are configured.

### Subscription (`/api`)

- `POST /api/cancel-subscription` — cancel subscription / adjust plan handling

Full interactive docs are available from Swagger UI when the backend is running.
