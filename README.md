# OpenRouter Chat Application - Madlen Case Study

A modern web-based chat application that allows you to interact with various AI models through the OpenRouter API.

## Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.x (Java 17)
- **Database**: PostgreSQL
- **Cache**: Redis (Spring Data Redis + Spring Cache)
- **Authentication**: JWT (JSON Web Tokens) with token caching
- **Rate Limiting**: Redis-based sliding window (10 requests/minute per user)
- **API Gateway**: OpenRouter
- **Observability**: OpenTelemetry + Jaeger
- **Architecture**: Clean Architecture (Controller → Service Interface → Service Implementation → Repository)

### Frontend (React + TypeScript)
- **Framework**: React 19 + TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **Routing**: React Router DOM
- **HTTP Client**: Axios
- **State Management**: React Context API + Hooks

## Project Structure

```
madlen/
├── backend/
│   ├── src/main/java/com/madlen/chat/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── service/impl/
│   │   ├── repository/
│   │   ├── model/
│   │   ├── dto/
│   │   ├── security/
│   │   └── config/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── auth/
│   │   │   └── chat/
│   │   ├── services/
│   │   ├── contexts/
│   │   ├── types/
│   │   └── utils/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
└── README.md
```

## Installation and Running

### Requirements
- Docker & Docker Compose
- Java 17 (for local development only)
- Node.js 20+ (for local development only)

### Environment Variables

Create a `.env` file (referencing the `.env.example` file):

```bash
# OpenRouter API Configuration
OPENROUTER_API_KEY=your_openrouter_api_key_here

# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/chatdb
DATABASE_USERNAME=chatuser
DATABASE_PASSWORD=chatpassword

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here_minimum_256_bits_recommended_512_bits
JWT_EXPIRATION=86400000

# Jaeger Configuration (OpenTelemetry)
JAEGER_ENDPOINT=http://localhost:4318

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

### Running with Docker (Recommended)

```bash
docker-compose up -d
docker-compose logs -f
docker-compose down
```

Services will run on the following ports:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Jaeger UI**: http://localhost:16686
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

### Local Development

#### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## API Endpoints

### Authentication (Public)
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### Chat (Protected - JWT Required)
- `POST /api/chat` - Send message (Rate limited: 10 requests/minute per user)
- `GET /api/models` - List available models (Cached for 1 hour)
- `POST /api/models/refresh` - Refresh models cache

### Conversations (Protected - JWT Required)
- `GET /api/conversations` - List all conversations (Cached for 5 minutes)
- `GET /api/conversations/{id}` - Get conversation details
- `POST /api/conversations` - Create new conversation
- `DELETE /api/conversations/{id}` - Delete conversation

### Authentication (Protected)
- `POST /api/auth/logout` - Logout and invalidate JWT token

## Authentication

The application uses JWT-based authentication with Redis token caching:
1. Obtain a token via `/api/auth/register` or `/api/auth/login`
2. Tokens are cached in Redis and validated on each request
3. Send the token in the `Authorization: Bearer <token>` header
4. The token is stored in localStorage and automatically added to all requests
5. Use `/api/auth/logout` to invalidate tokens (removed from Redis cache)

## Frontend Features

- Modern and responsive UI (Tailwind CSS)
- JWT-based authentication with token caching
- Real-time chat interface
- Model selection dropdown
- Conversation management (create, list, delete)
- Message history viewing
- Image upload support (multi-modal)
- Error handling and user feedback
- Rate limit error handling (HTTP 429)

## Caching and Performance

### Redis Cache Features

The application uses Redis for multiple caching strategies:

1. **Spring Cache Annotations**:
   - `@Cacheable` - Caches method results (models, users, conversations)
   - `@CacheEvict` - Removes cache entries on updates/deletes
   - Cache TTLs: Models (1 hour), Users (30 minutes), Conversations (5 minutes)

2. **JWT Token Caching**:
   - Tokens are cached in Redis with expiration matching JWT expiration
   - Token invalidation on logout removes entries from Redis
   - SHA-256 hashing for secure token storage

3. **Rate Limiting**:
   - Sliding window algorithm using Redis Sorted Sets (ZSET)
   - 10 requests per minute per user for `/api/chat` and `/api/chat/stream`
   - Returns HTTP 429 (Too Many Requests) when limit exceeded
   - Per-user rate limiting (isolated by userId)

### Cache Configuration

- Redis connection pooling (Lettuce client)
- Connection pool: max-active=10, max-idle=10, min-idle=2
- Automatic key expiration based on TTL settings
- Cache serialization: JSON for values, String for keys

## Observability (OpenTelemetry + Jaeger)

The application is instrumented with OpenTelemetry. Traces and spans can be viewed in Jaeger:

1. Go to Jaeger UI: http://localhost:16686
2. Select Service: `chat-backend`
3. View traces

Traces for critical operations:
- `auth.register` - User registration
- `auth.login` - User login
- `chat.send_message` - Message sending
- `openrouter.api_call` - OpenRouter API call

## Testing

### Backend Integration Tests

The application includes comprehensive integration tests for Redis cache features:

```bash
# Run all integration tests
cd backend
mvn test

# Run Redis cache integration tests
mvn test -Dtest=RedisCacheIntegrationTest

# Run Spring Cache integration tests
mvn test -Dtest=SpringCacheIntegrationTest
```

Test coverage includes:
- Redis connection and basic operations
- Rate limiting service (sliding window algorithm)
- Token caching and invalidation
- Spring Cache annotations (@Cacheable, @CacheEvict)
- Cache TTL and expiration
- Multiple cache isolation

### Backend API Tests

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

curl http://localhost:8080/api/models

curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello","model":"meta-llama/llama-3.2-3b-instruct:free"}'

# Test rate limiting (make 11 requests quickly)
for i in {1..11}; do
  curl -X POST http://localhost:8080/api/chat \
    -H "Authorization: Bearer <token>" \
    -H "Content-Type: application/json" \
    -d '{"message":"Test","model":"meta-llama/llama-3.2-3b-instruct:free"}'
done
# 11th request should return HTTP 429
```

## Technology Stack

### Backend
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- Spring Data Redis + Spring Cache
- PostgreSQL
- Redis 7 (caching, rate limiting, token management)
- OpenTelemetry Java SDK
- Lombok
- Maven

### Frontend
- React 19
- TypeScript
- Vite
- Tailwind CSS
- Axios
- React Router DOM

### Infrastructure
- Docker & Docker Compose
- Nginx (Frontend production)
- Redis (Caching and rate limiting)
- Jaeger (Distributed Tracing)

## Security Notes

### Before Production Deployment

1. **`.env` File**: 
   - The `.env` file should not be committed to Git (already in `.gitignore`)
   - Use strong, unique values in production
   - Create your own `.env` file by referencing the `.env.example` file

2. **Required Environment Variables**:
   - `OPENROUTER_API_KEY`: Your OpenRouter API key (must be set)
   - `JWT_SECRET`: Minimum 256 bits (32 characters), recommended 512 bits (64 characters) strong secret key
   - `POSTGRES_PASSWORD`: Change in production (default value in docker-compose.yml is for development only)
   - `REDIS_HOST`: Redis host (default: localhost for development, redis for Docker)
   - `REDIS_PORT`: Redis port (default: 6379)

3. **JWT Secret Generation**:
   ```bash
   openssl rand -base64 64
   openssl rand -hex 32
   ```

4. **API Key Security**:
   - OpenRouter API key is used only in the backend
   - Frontend does not and should not have an API key
   - All API calls are made through the backend

5. **Docker Compose Production Usage**:
   - Default values in `docker-compose.yml` are for development only
   - In production, set all values completely in the `.env` file
   - There is no default value for `JWT_SECRET` - it must be defined in `.env`

## Notes

- Backend and Frontend run in separate Docker containers
- Frontend uses Vite proxy in development mode (`/api` → `http://localhost:8080/api`)
- Nginx reverse proxy can be used in production
- JWT tokens are stored in localStorage (httpOnly cookies recommended in production)
- OpenRouter API key should be stored in the `.env` file and used in the backend
- Redis is required for caching, rate limiting, and token management
- Rate limiting uses sliding window algorithm (10 requests/minute per user)
- Cache TTLs: Models (1 hour), Users (30 minutes), Conversations (5 minutes)
- All cache operations are automatically handled by Spring Cache annotations
- Redis is required for caching, rate limiting, and token management
- Rate limiting uses sliding window algorithm (10 requests/minute per user)
- Cache TTLs: Models (1 hour), Users (30 minutes), Conversations (5 minutes)
- All cache operations are automatically handled by Spring Cache annotations

## Troubleshooting

### Backend won't start
- Make sure PostgreSQL is running
- Make sure Redis is running
- Check the `DATABASE_URL` in the `.env` file
- Check the `REDIS_HOST` and `REDIS_PORT` in the `.env` file
- Make sure port 8080 is available

### Frontend can't connect to API
- Make sure the backend is running
- Check CORS settings
- Check for errors in the browser console

### OpenRouter API errors
- Make sure the API key is correct
- Be aware of rate limiting
- Make sure model names are correct

### Rate limiting errors (HTTP 429)
- Rate limit is 10 requests per minute per user
- Wait 60 seconds before retrying
- Rate limiting is per-user, so different users have separate limits

### Redis connection errors
- Make sure Redis container is running: `docker-compose ps`
- Check Redis logs: `docker-compose logs redis`
- Verify Redis connection: `docker exec chat-redis redis-cli PING`

### Rate limiting errors (HTTP 429)
- Rate limit is 10 requests per minute per user
- Wait 60 seconds before retrying
- Rate limiting is per-user, so different users have separate limits

### Redis connection errors
- Make sure Redis container is running: `docker-compose ps`
- Check Redis logs: `docker-compose logs redis`
- Verify Redis connection: `docker exec chat-redis redis-cli PING`

## License

This project was developed as a case study.
