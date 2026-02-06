# Auth User Service - Standalone Microservice

A production-ready Spring Boot microservice for authentication and user management using AWS Cognito and PostgreSQL with **comprehensive observability**.

## 🎯 Key Features
✅ AWS Cognito Authentication  
✅ PostgreSQL Database  
✅ **User Profile Management**  
✅ **User Address Management**  
✅ **Admin User Management**  
✅ **Prometheus Metrics**  
✅ **Grafana Dashboards**  
✅ **Structured JSON Logging**  
✅ **Distributed Tracing**  
✅ Docker Containerization

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- AWS Account with Cognito configured
- Maven 3.8+ (optional, for local development)

### Access Services

After starting, access:
- **Application:** http://localhost:8080
- **Grafana Dashboard:** http://localhost:3000 (admin/admin)
- **Prometheus:** http://localhost:9090
- **Metrics Endpoint:** http://localhost:8080/actuator/prometheus

### 1. Configure Environment

Copy the example environment file:
```bash
cp .env.example .env
```

Edit `.env` with your AWS Cognito credentials:
```env
AWS_COGNITO_REGION=us-east-1
AWS_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
AWS_COGNITO_CLIENT_ID=your_client_id
AWS_COGNITO_CLIENT_SECRET=your_client_secret
AWS_COGNITO_JWK_SET_URI=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

### 2. Start the Service

```bash
docker-compose up -d
```

### 3. Verify

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}
```

## 📋 API Endpoints

**Complete API Documentation:**
- **[USER_MANAGEMENT_API.md](USER_MANAGEMENT_API.md)** - User management & address endpoints
- Authentication endpoints below

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "SecurePass123!"
}

Response:
{
  "token": "eyJraWQiOiJ...",
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer YOUR_JWT_TOKEN
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your_refresh_token"
}
```

#### Change Password
```http
POST /api/auth/change-password
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "oldPassword": "OldPass123!",
  "newPassword": "NewPass123!"
}
```

#### Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "username": "john.doe"
}
```

#### Confirm Forgot Password
```http
POST /api/auth/confirm-forgot-password
Content-Type: application/json

{
  "username": "john.doe",
  "confirmationCode": "123456",
  "newPassword": "NewPass123!"
}
```

### User Management

For complete documentation on user management and address endpoints, see **[USER_MANAGEMENT_API.md](USER_MANAGEMENT_API.md)**

**Quick Reference:**
```http
GET    /api/users/me                    # Get current user profile
PUT    /api/users/me                    # Update current user profile
GET    /api/users/me/addresses          # Get user addresses
POST   /api/users/me/addresses          # Create new address
PUT    /api/users/me/addresses/{id}     # Update address
DELETE /api/users/me/addresses/{id}     # Delete address
PATCH  /api/users/me/addresses/{id}/default  # Set default address

# Admin endpoints (requires ADMIN role)
GET    /api/admin/users                 # List all users
GET    /api/admin/users/{id}            # Get user by ID
PUT    /api/admin/users/{id}            # Update user
DELETE /api/admin/users/{id}            # Delete user
GET    /api/admin/users/statistics      # User statistics
```

### Health & Monitoring

```http
GET /actuator/health       # Health check
GET /actuator/metrics      # Application metrics
GET /actuator/info         # Application info
GET /actuator/prometheus   # Prometheus metrics
```

## 🏗️ Project Structure

```
auth-user-service/
├── src/
│   └── main/
│       ├── java/com/authservice/
│       │   ├── config/
│       │   │   ├── AwsCognitoConfig.java
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AdminUserController.java
│       │   │   ├── AuthController.java
│       │   │   ├── UserAddressController.java
│       │   │   └── UserManagementController.java
│       │   ├── dto/
│       │   │   ├── AddressDTO.java
│       │   │   ├── AdminUserDTO.java
│       │   │   ├── CreateAddressRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── LoginResponse.java
│       │   │   ├── RegisterRequest.java
│       │   │   ├── UpdateAddressRequest.java
│       │   │   ├── UpdateUserProfileRequest.java
│       │   │   └── UserDTO.java
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   └── ResourceNotFoundException.java
│       │   ├── model/
│       │   │   ├── User.java
│       │   │   └── UserAddress.java
│       │   ├── observability/
│       │   │   └── MetricsService.java
│       │   ├── repository/
│       │   │   ├── UserAddressRepository.java
│       │   │   └── UserRepository.java
│       │   ├── service/
│       │   │   ├── CognitoService.java
│       │   │   ├── UserAddressService.java
│       │   │   ├── UserManagementService.java
│       │   │   └── UserService.java
│       │   └── AuthUserServiceApplication.java
│       └── resources/
│           ├── application.yml
│           └── logback-spring.xml
├── database/
│   └── 01-init.sql
├── monitoring/
│   ├── grafana-dashboard-auth.json
│   ├── grafana-datasource.yml
│   └── prometheus.yml
├── Dockerfile
├── Dockerfile.dev
├── docker-compose.yml
├── pom.xml
├── .env.example
├── .gitignore
├── README.md
├── SETUP_GUIDE.md
├── OBSERVABILITY.md
└── USER_MANAGEMENT_API.md
```

## 🛠️ Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.2.5 | Application framework |
| Spring Security | 6.2.0 | Security framework |
| AWS Cognito | SDK 2.24.9 | Authentication provider |
| PostgreSQL | 16 | Database |
| Docker | Latest | Containerization |
| Maven | 3.8+ | Build tool |

## 📦 Features

### Authentication
✅ User registration with AWS Cognito  
✅ Email-based authentication  
✅ JWT token-based sessions  
✅ Token refresh mechanism  
✅ Password change  
✅ Forgot password flow  
✅ Role-based access control  

### User Management
✅ User profile management  
✅ User address CRUD operations  
✅ Default address management  
✅ Multiple address support (shipping/billing)  
✅ Admin user management dashboard  
✅ User search and filtering  
✅ User statistics and analytics  

### Security
✅ JWT validation using Cognito JWK Set  
✅ SECRET_HASH calculation  
✅ BCrypt password hashing  
✅ CORS configuration  
✅ Stateless session management  
✅ Non-root Docker container user  

### Observability ⭐
✅ **Prometheus metrics** with custom auth & user metrics  
✅ **Grafana dashboards** pre-configured  
✅ **Structured JSON logging** (ELK-compatible)  
✅ **Distributed tracing** with OpenTelemetry  
✅ **Health probes** for Kubernetes  
✅ **PostgreSQL metrics** via postgres-exporter  
✅ Custom metrics for user operations and addresses  

### Database
✅ PostgreSQL 16  
✅ HikariCP connection pooling  
✅ JPA/Hibernate ORM  
✅ Automatic schema management  
✅ Database initialization scripts  
✅ User and address tables with relationships  
✅ Cascading deletes and constraints  

### DevOps
✅ Docker containerization  
✅ Multi-stage Docker builds  
✅ Docker Compose orchestration  
✅ Health check endpoints  
✅ Development & production images  
✅ Full monitoring stack included  

## 🚢 Deployment

### Docker Compose (Recommended)

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Remove volumes (complete cleanup)
docker-compose down -v
```

### Docker (Standalone)

```bash
# Build image
docker build -t auth-user-service:latest .

# Run container
docker run -d \
  --name auth-service \
  -p 8080:8080 \
  --env-file .env \
  auth-user-service:latest
```

### Local Development

```bash
# Start PostgreSQL
docker run -d \
  --name auth-postgres \
  -e POSTGRES_DB=authdb \
  -e POSTGRES_USER=authuser \
  -e POSTGRES_PASSWORD=authpassword \
  -p 5432:5432 \
  postgres:16

# Run application
mvn spring-boot:run
```

## 🧪 Testing

### Manual Testing with cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'

# Get current user (replace TOKEN with actual JWT)
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer TOKEN"
```

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify
```

## 🔧 Configuration

### Environment Variables

| Variable | Required | Description | Default |
|----------|----------|-------------|---------|
| `AWS_COGNITO_REGION` | Yes | AWS region | us-east-1 |
| `AWS_COGNITO_USER_POOL_ID` | Yes | User Pool ID | - |
| `AWS_COGNITO_CLIENT_ID` | Yes | App Client ID | - |
| `AWS_COGNITO_CLIENT_SECRET` | No | App Client Secret | - |
| `AWS_COGNITO_JWK_SET_URI` | Yes | JWK Set URI | - |
| `AWS_ACCESS_KEY_ID` | Yes | AWS Access Key | - |
| `AWS_SECRET_ACCESS_KEY` | Yes | AWS Secret Key | - |
| `SPRING_DATASOURCE_URL` | No | Database URL | jdbc:postgresql://localhost:5432/authdb |
| `SPRING_DATASOURCE_USERNAME` | No | Database username | authuser |
| `SPRING_DATASOURCE_PASSWORD` | No | Database password | authpassword |
| `SERVER_PORT` | No | Server port | 8080 |

### application.yml

Key configuration in `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: auth-user-service
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/authdb}
    username: ${SPRING_DATASOURCE_USERNAME:authuser}
    password: ${SPRING_DATASOURCE_PASSWORD:authpassword}
  jpa:
    hibernate:
      ddl-auto: update

aws:
  cognito:
    region: ${AWS_COGNITO_REGION:us-east-1}
    user-pool-id: ${AWS_COGNITO_USER_POOL_ID:}
    client-id: ${AWS_COGNITO_CLIENT_ID:}
    client-secret: ${AWS_COGNITO_CLIENT_SECRET:}
    jwk-set-uri: ${AWS_COGNITO_JWK_SET_URI:}

server:
  port: ${SERVER_PORT:8080}
```

## 🐛 Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose logs auth-service

# Common issues:
# 1. PostgreSQL not ready - wait 30 seconds
# 2. Missing environment variables - check .env file
# 3. Port 8080 in use - change SERVER_PORT
```

### Authentication Fails

```bash
# Verify Cognito configuration
aws cognito-idp describe-user-pool --user-pool-id YOUR_POOL_ID

# Check environment variables
docker exec auth-service env | grep COGNITO
```

### Database Connection Issues

```bash
# Check PostgreSQL
docker exec -it auth-postgres psql -U authuser -d authdb

# Verify connection
psql -h localhost -U authuser -d authdb -c "SELECT version();"
```

## 📊 Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health (requires authentication)
curl http://localhost:8080/actuator/health \
  -H "Authorization: Bearer TOKEN"
```

### Metrics

```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Logs

```bash
# View logs
docker-compose logs -f auth-service

# View specific number of lines
docker-compose logs --tail=100 auth-service
```

## 🔐 Security Best Practices

### Production Deployment

- [ ] Use AWS Secrets Manager for credentials
- [ ] Enable HTTPS/SSL
- [ ] Configure proper CORS origins
- [ ] Enable MFA in Cognito
- [ ] Set up CloudWatch monitoring
- [ ] Configure automated backups
- [ ] Use strong passwords
- [ ] Enable audit logging
- [ ] Implement rate limiting
- [ ] Regular security audits

### Development

- [ ] Never commit .env files
- [ ] Use separate AWS accounts for dev/prod
- [ ] Enable DEBUG logging for troubleshooting
- [ ] Use local PostgreSQL for testing

## 📊 Observability

The service includes a complete observability stack. See **[OBSERVABILITY.md](OBSERVABILITY.md)** for:
- Custom authentication metrics
- Pre-built Grafana dashboards
- Structured logging configuration
- Distributed tracing setup
- Alert rule examples

**Quick Access:**
```bash
# View metrics
curl http://localhost:8080/actuator/prometheus

# Access Grafana
open http://localhost:3000

# View logs
docker logs -f auth-service
```

## 📚 Documentation

### Project Documentation
- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Detailed AWS Cognito setup
- **[USER_MANAGEMENT_API.md](USER_MANAGEMENT_API.md)** - Complete API reference for user management
- **[OBSERVABILITY.md](OBSERVABILITY.md)** - Monitoring and observability guide
- **[PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)** - Architecture and design overview

### External Resources
- [AWS Cognito Documentation](https://docs.aws.amazon.com/cognito/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)

## 📝 License

Copyright © 2024. All rights reserved.

## 🤝 Support

For issues and questions:
1. Check the SETUP_GUIDE.md for detailed setup instructions
2. Review application logs
3. Check AWS Cognito console for auth issues
4. Verify environment variables

---

**Status:** ✅ Production Ready  
**Version:** 1.0.0  
**Last Updated:** February 2026
