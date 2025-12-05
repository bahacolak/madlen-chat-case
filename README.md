# OpenRouter Chat Application - Madlen Case Study

A modern web-based chat application that allows you to interact with various AI models through the OpenRouter API.

## Architecture

### Backend (Spring Boot)
- **Framework**: Spring Boot 3.x (Java 17)
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
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
- `POST /api/chat` - Send message
- `GET /api/models` - List available models

### Conversations (Protected - JWT Required)
- `GET /api/conversations` - List all conversations
- `GET /api/conversations/{id}` - Get conversation details
- `POST /api/conversations` - Create new conversation
- `DELETE /api/conversations/{id}` - Delete conversation

## Authentication

The application uses JWT-based authentication:
1. Obtain a token via `/api/auth/register` or `/api/auth/login`
2. Send the token in the `Authorization: Bearer <token>` header
3. The token is stored in localStorage and automatically added to all requests

## Frontend Features

- Modern and responsive UI (Tailwind CSS)
- JWT-based authentication
- Real-time chat interface
- Model selection dropdown
- Conversation management (create, list, delete)
- Message history viewing
- Image upload support (multi-modal)
- Error handling and user feedback

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
```

## Technology Stack

### Backend
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
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

## Troubleshooting

### Backend won't start
- Make sure PostgreSQL is running
- Check the `DATABASE_URL` in the `.env` file
- Make sure port 8080 is available

### Frontend can't connect to API
- Make sure the backend is running
- Check CORS settings
- Check for errors in the browser console

### OpenRouter API errors
- Make sure the API key is correct
- Be aware of rate limiting
- Make sure model names are correct

## License

This project was developed as a case study.
