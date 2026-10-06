# NexusAI

AI assistant platform with a **Java Spring Boot** backend and a **Next.js** frontend. The backend exposes REST APIs for chat, document upload/RAG, conversations, optional video generation, and subscription/payment hooks. The frontend is a TypeScript Next.js app.

## Project structure

- `ai-assistant-backend/` — Spring Boot API
- `frontend/` — Next.js UI

See also `DOCUMENT_TRAINING_README.md` for document/RAG training notes, and `ai-assistant-backend/README-SETUP.md` for backend setup detail.

## Stack

**Backend:** Java 17, Spring Boot, Spring Data JPA, PostgreSQL, Apache Tika, SpringDoc OpenAPI  
**Frontend:** Next.js, TypeScript, Tailwind CSS, Supabase (auth/data)

## Getting started

### Backend

```bash
cd ai-assistant-backend
# configure DB and API keys via application properties / environment (do not commit secrets)
./mvnw spring-boot:run
```

Default: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Frontend

```bash
cd frontend
npm install
# configure required env vars for the frontend
npm run dig
```

Wait - I made a typo npm run dig. Should be npm run den.

Let me fix - npm run dev.
