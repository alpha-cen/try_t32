# Setup Guide - Auth User Service

Complete step-by-step guide to set up and run the Auth User Service microservice with AWS Cognito.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [AWS Cognito Setup](#aws-cognito-setup)
3. [Local Development Setup](#local-development-setup)
4. [Docker Deployment](#docker-deployment)
5. [Testing](#testing)
6. [Production Deployment](#production-deployment)

---

## Prerequisites

### Required Software
- **Java 21** (OpenJDK or Oracle JDK)
- **Docker** 20.10+ and **Docker Compose** 2.0+
- **AWS Account** with administrative access
- **AWS CLI** configured
- **Maven** 3.8+ (optional, for local development)
- **PostgreSQL** 16 (if running locally without Docker)

### Verify Installation

```bash
# Check Java version
java -version
# Should show: openjdk version "21.x.x"

# Check Docker
docker --version
docker-compose --version

# Check AWS CLI
aws --version

# Check Maven (optional)
mvn --version
```

---

## AWS Cognito Setup

### Step 1: Create User Pool

#### Option A: Using AWS CLI (Recommended)

```bash
# Set your region
export AWS_REGION=us-east-1

# Create User Pool
aws cognito-idp create-user-pool \
  --pool-name auth-user-service-pool \
  --auto-verified-attributes email \
  --username-attributes email \
  --policies "PasswordPolicy={MinimumLength=8,RequireUppercase=true,RequireLowercase=true,RequireNumbers=true,RequireSymbols=true}" \
  --region $AWS_REGION

# Save the UserPoolId from the output
export USER_POOL_ID=<your-user-pool-id>
```

#### Option B: Using AWS Console

1. Log in to AWS Console
2. Navigate to **AWS Cognito**
3. Click **Create user pool**
4. **Provider types**: Cognito user pool
5. **Cognito user pool sign-in options**: Email
6. **Password policy**: 
   - Minimum length: 8
   - Require: uppercase, lowercase, numbers, symbols
7. **MFA**: Optional (recommended for production)
8. **User account recovery**: Email only
9. Click **Next** through remaining steps
10. Review and **Create user pool**
11. Note the **User Pool ID**

### Step 2: Create App Client

#### Option A: Using AWS CLI

```bash
# Create App Client
aws cognito-idp create-user-pool-client \
  --user-pool-id $USER_POOL_ID \
  --client-name auth-service-client \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH ALLOW_USER_SRP_AUTH \
  --region $AWS_REGION

# Save the ClientId from the output
export CLIENT_ID=<your-client-id>
```

#### Option B: Using AWS Console

1. In your User Pool, go to **App integration** tab
2. Scroll to **App clients and analytics**
3. Click **Create app client**
4. **App client name**: auth-service-client
5. **Authentication flows**:
   - ✅ ALLOW_USER_PASSWORD_AUTH
   - ✅ ALLOW_REFRESH_TOKEN_AUTH
   - ✅ ALLOW_USER_SRP_AUTH
6. **Generate client secret**: Optional (check if you want extra security)
7. Click **Create app client**
8. Note the **Client ID** (and **Client Secret** if generated)

### Step 3: Create User Groups

```bash
# Create ADMIN group
aws cognito-idp create-group \
  --user-pool-id $USER_POOL_ID \
  --group-name ADMIN \
  --description "Administrator group" \
  --region $AWS_REGION

# Create USER group
aws cognito-idp create-group \
  --user-pool-id $USER_POOL_ID \
  --group-name USER \
  --description "Regular user group" \
  --region $AWS_REGION
```

### Step 4: Get JWK Set URI

The JWK Set URI follows this pattern:
```
https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
```

Example:
```
https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABC123XYZ/.well-known/jwks.json
```

### Step 5: Create IAM User for SDK Access

```bash
# Create IAM policy
cat > cognito-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "cognito-idp:InitiateAuth",
        "cognito-idp:SignUp",
        "cognito-idp:ConfirmSignUp",
        "cognito-idp:ForgotPassword",
        "cognito-idp:ConfirmForgotPassword",
        "cognito-idp:ChangePassword",
        "cognito-idp:GetUser",
        "cognito-idp:GlobalSignOut",
        "cognito-idp:AdminConfirmSignUp"
      ],
      "Resource": "arn:aws:cognito-idp:${AWS_REGION}:*:userpool/${USER_POOL_ID}"
    }
  ]
}
EOF

# Create policy
aws iam create-policy \
  --policy-name AuthServiceCognitoPolicy \
  --policy-document file://cognito-policy.json

# Create IAM user
aws iam create-user --user-name auth-service-user

# Attach policy to user
aws iam attach-user-policy \
  --user-name auth-service-user \
  --policy-arn arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):policy/AuthServiceCognitoPolicy

# Create access keys
aws iam create-access-key --user-name auth-service-user

# Save the AccessKeyId and SecretAccessKey from the output
```

---

## Local Development Setup

### Step 1: Clone/Navigate to Project

```bash
cd auth-user-service
```

### Step 2: Configure Environment

Create `.env` file:

```bash
cp .env.example .env
```

Edit `.env` with your AWS credentials:

```env
# AWS Cognito Configuration
AWS_COGNITO_REGION=us-east-1
AWS_COGNITO_USER_POOL_ID=us-east-1_ABC123XYZ
AWS_COGNITO_CLIENT_ID=your_client_id_here
AWS_COGNITO_CLIENT_SECRET=your_client_secret_here
AWS_COGNITO_JWK_SET_URI=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_ABC123XYZ/.well-known/jwks.json

# AWS Credentials
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# Database Configuration (defaults for Docker)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/authdb
SPRING_DATASOURCE_USERNAME=authuser
SPRING_DATASOURCE_PASSWORD=authpassword

# Server Configuration
SERVER_PORT=8080
```

### Step 3: Start PostgreSQL

```bash
docker run -d \
  --name auth-postgres \
  -e POSTGRES_DB=authdb \
  -e POSTGRES_USER=authuser \
  -e POSTGRES_PASSWORD=authpassword \
  -p 5432:5432 \
  -v $(pwd)/database:/docker-entrypoint-initdb.d \
  postgres:16
```

### Step 4: Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### Step 5: Verify

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP"}
```

---

## Docker Deployment

### Step 1: Prepare Environment

Ensure `.env` file is properly configured (see Local Development Setup).

### Step 2: Start Services

```bash
# Start all services (PostgreSQL + Application)
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### Step 3: Verify Deployment

```bash
# Health check
curl http://localhost:8080/actuator/health

# Check logs
docker-compose logs auth-service

# Check database
docker exec -it auth-postgres psql -U authuser -d authdb -c "SELECT * FROM users;"
```

---

## Testing

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@example.com",
    "email": "testuser@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+1234567890"
  }'
```

**Expected Response:**
```json
{
  "user": {
    "id": 1,
    "username": "testuser@example.com",
    "email": "testuser@example.com",
    "firstName": "Test",
    "lastName": "User",
    "role": "USER"
  },
  "message": "User registered successfully. Please check your email for verification."
}
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@example.com",
    "password": "Test123!"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJraWQiOiJ...",
  "user": {
    "id": 1,
    "username": "testuser@example.com",
    "email": "testuser@example.com",
    "firstName": "Test",
    "lastName": "User",
    "role": "USER"
  }
}
```

**Save the token for subsequent requests!**

### 3. Get Current User

```bash
TOKEN="eyJraWQiOiJ..."  # Replace with your actual token

curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Add User to Admin Group (Optional)

```bash
aws cognito-idp admin-add-user-to-group \
  --user-pool-id $USER_POOL_ID \
  --username testuser@example.com \
  --group-name ADMIN \
  --region $AWS_REGION
```

---

## Production Deployment

### AWS ECS/Fargate

#### 1. Create ECR Repository

```bash
# Create repository
aws ecr create-repository --repository-name auth-user-service

# Get login command
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com
```

#### 2. Build and Push Image

```bash
# Build image
docker build -t auth-user-service:latest .

# Tag image
docker tag auth-user-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/auth-user-service:latest

# Push to ECR
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/auth-user-service:latest
```

#### 3. Create ECS Task Definition

Create `task-definition.json`:

```json
{
  "family": "auth-user-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "auth-service",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/auth-user-service:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "AWS_COGNITO_REGION", "value": "us-east-1"},
        {"name": "AWS_COGNITO_USER_POOL_ID", "value": "us-east-1_ABC123XYZ"},
        {"name": "AWS_COGNITO_CLIENT_ID", "value": "your_client_id"},
        {"name": "SPRING_DATASOURCE_URL", "value": "jdbc:postgresql://your-rds-endpoint:5432/authdb"}
      ],
      "secrets": [
        {"name": "AWS_COGNITO_CLIENT_SECRET", "valueFrom": "arn:aws:secretsmanager:..."},
        {"name": "AWS_ACCESS_KEY_ID", "valueFrom": "arn:aws:secretsmanager:..."},
        {"name": "AWS_SECRET_ACCESS_KEY", "valueFrom": "arn:aws:secretsmanager:..."},
        {"name": "SPRING_DATASOURCE_PASSWORD", "valueFrom": "arn:aws:secretsmanager:..."}
      ]
    }
  ]
}
```

#### 4. Create ECS Service

```bash
aws ecs create-service \
  --cluster your-cluster \
  --service-name auth-user-service \
  --task-definition auth-user-service \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=ENABLED}"
```

### Kubernetes

#### 1. Create ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-service-config
data:
  AWS_COGNITO_REGION: "us-east-1"
  AWS_COGNITO_USER_POOL_ID: "us-east-1_ABC123XYZ"
  AWS_COGNITO_CLIENT_ID: "your_client_id"
  SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/authdb"
```

#### 2. Create Secret

```bash
kubectl create secret generic auth-service-secrets \
  --from-literal=AWS_COGNITO_CLIENT_SECRET=your_secret \
  --from-literal=AWS_ACCESS_KEY_ID=your_key \
  --from-literal=AWS_SECRET_ACCESS_KEY=your_secret_key \
  --from-literal=SPRING_DATASOURCE_PASSWORD=dbpassword
```

#### 3. Create Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: your-registry/auth-user-service:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: auth-service-config
        - secretRef:
            name: auth-service-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

---

## Troubleshooting

### Common Issues

#### 1. "Unable to connect to database"

**Solution:**
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U authuser -d authdb -c "SELECT 1;"

# Verify environment variables
echo $SPRING_DATASOURCE_URL
```

#### 2. "User pool not found"

**Solution:**
```bash
# Verify User Pool ID
aws cognito-idp describe-user-pool --user-pool-id $USER_POOL_ID

# Check environment variable
echo $AWS_COGNITO_USER_POOL_ID
```

#### 3. "JWT validation failed"

**Solution:**
```bash
# Verify JWK Set URI
curl $AWS_COGNITO_JWK_SET_URI

# Should return JSON with keys
```

#### 4. "Port 8080 already in use"

**Solution:**
```bash
# Find process using port
lsof -i :8080

# Kill process or change port in .env
export SERVER_PORT=8081
```

---

## Maintenance

### Backup Database

```bash
# Backup
docker exec auth-postgres pg_dump -U authuser authdb > backup.sql

# Restore
docker exec -i auth-postgres psql -U authuser authdb < backup.sql
```

### View Logs

```bash
# Application logs
docker-compose logs -f auth-service

# Database logs
docker-compose logs -f postgres

# Tail last 100 lines
docker-compose logs --tail=100 auth-service
```

### Update Application

```bash
# Pull latest code
git pull

# Rebuild and restart
docker-compose down
docker-compose up -d --build
```

---

## Next Steps

1. ✅ Set up monitoring with CloudWatch
2. ✅ Configure automated backups
3. ✅ Set up CI/CD pipeline
4. ✅ Enable MFA in Cognito
5. ✅ Configure SSL/TLS
6. ✅ Set up rate limiting
7. ✅ Implement caching
8. ✅ Add comprehensive tests

---

**Setup Complete!** 🎉

Your Auth User Service is now ready for use. For API documentation, see the main README.md file.
