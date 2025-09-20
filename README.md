# NexusAI

A comprehensive AI-powered platform that provides multiple AI assistants with advanced document processing, conversation management, video generation, and payment integration capabilities.

## Project Structure

- **ai-assistant-backend/**: Java Spring Boot backend API with comprehensive AI services
- **frontend/**: Next.js frontend application with modern UI components

---

## Backend API Documentation

The backend provides a robust REST API with the following main controllers:

### 🎥 Video Generation Controller (`/api/video`)

Handles AI-powered video generation from text prompts and images using Replicate API.

**Endpoints:**

- `POST /api/video/generate/text` - Generate video from text prompt
- `POST /api/video/generate/image` - Generate video from image + prompt
- `GET /api/video/status/{taskId}` - Check video generation status
- `POST /api/video/cleanup` - Clean up old video tasks

**Features:**

- Text-to-video generation with customizable parameters
- Image-to-video generation
- Real-time status tracking
- Support for duration, resolution, and aspect ratio customization

### 👤 User Controller (`/api/users`)

Manages user accounts and authentication.

**Endpoints:**

- `POST /api/users` - Create new user or get existing user
- `GET /api/users?email={email}` - Get user by email

**Features:**

- User creation and retrieval
- Email-based user lookup
- Automatic duplicate prevention

### 📄 Document Controller (`/api/documents`)

Handles document upload, processing, and management with text extraction capabilities.

**Endpoints:**

- `POST /api/documents/upload` - Upload document with text extraction
- `GET /api/documents/user/{userId}` - Get user's documents
- `GET /api/documents/assistant/{userId}/{assistantId}` - Get assistant-specific documents
- `DELETE /api/documents/{documentId}` - Delete document
- `GET /api/documents/count/{userId}` - Get document count
- `GET /api/documents/context` - Get relevant document context for AI queries

**Features:**

- Multi-format document support (PDF, DOC, TXT, etc.)
- Automatic text extraction using Apache Tika
- Document chunking and vector storage
- Context-aware document retrieval for AI responses

### 🔍 Vector Document Controller (`/api/vector-documents`)

Advanced RAG (Retrieval Augmented Generation) implementation with vector search capabilities.

**Endpoints:**

- `POST /api/vector-documents/upload` - Upload document with vector processing
- `GET /api/vector-documents/semantic-search` - Semantic search in documents
- `POST /api/vector-documents/rag-context` - Get RAG context for AI queries
- `GET /api/vector-documents/chunk-statistics` - Get chunk statistics
- `GET /api/vector-documents/performance-metrics` - Get performance metrics
- `GET /api/vector-documents/health` - Health status check

**Features:**

- Advanced document chunking and embedding generation
- Semantic search with similarity scoring
- RAG context generation for enhanced AI responses
- Performance monitoring and analytics
- Health status monitoring

### 💬 Conversation Controller (`/api/conversations`)

Manages AI conversations and message history.

**Endpoints:**

- `POST /api/conversations` - Create new conversation
- `GET /api/conversations/user/{userId}` - Get user's conversations
- `GET /api/conversations/user/{userId}/assistant/{assistantId}` - Get conversations by assistant
- `GET /api/conversations/{conversationId}` - Get specific conversation with messages
- `POST /api/conversations/messages` - Add message to conversation
- `DELETE /api/conversations/{conversationId}` - Delete conversation

**Features:**

- Conversation management with message history
- Assistant-specific conversation filtering
- Message threading and context preservation
- Conversation deletion and cleanup

### 🤖 AI Chat Controller (`/api/ai`)

Core AI chat functionality with multiple AI model support.

**Endpoints:**

- `POST /api/ai/chat` - Generate AI response

**Features:**

- Multi-model AI support (OpenAI, Gemini, Claude, etc.)
- Context-aware responses using document RAG
- Error handling and fallback responses
- Token usage tracking and cost management

### 💳 Payment Controller (`/api/payment`)

Handles subscription payments and billing using SSLCommerz integration.

**Endpoints:**

- `POST /api/payment/initiate` - Initiate payment
- `POST /api/payment/ipn` - Handle IPN notifications
- `GET /api/payment/status/{transactionId}` - Get payment status
- `POST /api/payment/validate/{transactionId}` - Validate transaction
- `POST/GET /api/payment/success` - Handle successful payment
- `POST/GET /api/payment/fail` - Handle failed payment
- `POST/GET /api/payment/cancel` - Handle cancelled payment

**Features:**

- SSLCommerz payment gateway integration
- Real-time payment status tracking
- IPN (Instant Payment Notification) handling
- Payment validation and verification
- Automatic redirect handling for payment callbacks

### 📊 Subscription Controller (`/api`)

Manages user subscriptions and plan management.

**Endpoints:**

- `POST /api/cancel-subscription` - Cancel user subscription

**Features:**

- Subscription cancellation
- Plan downgrade handling
- User credit management

---

## Technology Stack

### Backend

- **Java 17** - Core programming language
- **Spring Boot 3.5.5** - Application framework
- **Spring Data JPA** - Database ORM
- **PostgreSQL** - Primary database
- **Apache Tika** - Document text extraction
- **SpringDoc OpenAPI 2.7.0** - API documentation
- **SSLCommerz** - Payment gateway integration

### Frontend

- **Next.js 14** - React framework
- **TypeScript** - Type-safe JavaScript
- **Tailwind CSS** - Styling framework
- **Supabase** - Authentication and database

---

## Getting Started

### Backend (Spring Boot)

1. Navigate to the backend directory:

   ```sh
   cd ai-assistant-backend
   ```

2. Configure database connection in `application.properties`

3. Build and run the backend:

   ```sh
   ./mvnw spring-boot:run
   ```

4. The backend will start on [http://localhost:8080](http://localhost:8080)
   - API Documentation: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Frontend (Next.js)

1. Navigate to the frontend directory:

   ```sh
   cd frontend
   ```

2. Install dependencies:

   ```sh
   npm install
   ```

3. Configure environment variables

4. Start the development server:

   ```sh
   npm run dev
   ```

5. The frontend will start on [http://localhost:3000](http://localhost:3000)

---

## Key Features

- **Multi-AI Assistant Support**: Integration with OpenAI, Google Gemini, Claude, and other AI models
- **Advanced Document Processing**: Upload and process various document formats with automatic text extraction
- **RAG Implementation**: Retrieval Augmented Generation for context-aware AI responses
- **Video Generation**: AI-powered video creation from text and images
- **Conversation Management**: Persistent chat history with multiple AI assistants
- **Payment Integration**: Complete subscription and billing system
- **Vector Search**: Semantic search capabilities for document retrieval
- **Performance Monitoring**: Comprehensive analytics and health monitoring

---

## API Documentation

The complete API documentation is available via Swagger UI when the backend is running:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Contributing

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.
