# Deployment Guide - Core Banking System

## 🌍 Environments Overview

| Environment | Profile | Purpose | Database | Configuration Source |
|-------------|---------|---------|----------|---------------------|
| **Local Dev** | `dev` | Development on local machine | Docker PostgreSQL | `.env` file |
| **Testing** | `test` | Automated tests | Testcontainers | `application-test.yml` |
| **Staging** | `staging` | Pre-production testing | AWS RDS (staging) | AWS Parameter Store |
| **Production** | `prod` | Live system | AWS RDS (production) | AWS Secrets Manager |

---

## 🔧 Local Development (Profile: dev)

### Prerequisites
- Docker & Docker Compose installed
- PostgreSQL running in Docker
- Redis running in Docker

### Configuration
1. Copy `.env.example` to `.env`
2. Configure local values:
```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/core_banking_db
REDIS_HOST=localhost
```

3. Start dependencies:
```bash
docker-compose up -d postgres redis
```

4. Run application:
```bash
mvn spring-boot:run
# or from IDE with EnvFile plugin
```

---

## 🧪 Testing (Profile: test)

### Configuration
- Uses `application-test.yml`
- Testcontainers manages PostgreSQL automatically
- Mail and Redis disabled

### Run tests
```bash
mvn test                    # All tests
mvn test -Dtest=*IT         # Integration tests only
```

---

## ☁️ AWS Deployment (Profile: prod)

### Architecture
```
Internet → ALB → EC2 (Auto Scaling) → RDS PostgreSQL
                  ↓
            ElastiCache Redis
```

### Step 1: Setup AWS Resources

#### 1.1 RDS PostgreSQL
```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier core-banking-prod \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 16 \
  --master-username banking_admin \
  --master-user-password "CHANGE_ME" \
  --allocated-storage 20 \
  --vpc-security-group-ids sg-xxxxxx \
  --db-subnet-group-name my-subnet-group \
  --backup-retention-period 7 \
  --multi-az
```

#### 1.2 ElastiCache Redis
```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id core-banking-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1 \
  --security-group-ids sg-xxxxxx
```

### Step 2: Store Secrets in AWS Secrets Manager

```bash
# Database credentials
aws secretsmanager create-secret \
  --name /core-banking/prod/db-credentials \
  --secret-string '{
    "username":"banking_admin",
    "password":"super_secure_password",
    "host":"core-banking-prod.xxxxxx.us-east-1.rds.amazonaws.com",
    "port":"5432",
    "dbname":"core_banking_prod"
  }'

# JWT Secret
aws secretsmanager create-secret \
  --name /core-banking/prod/jwt-secret \
  --secret-string "your_jwt_secret_at_least_32_chars"

# Mail credentials
aws secretsmanager create-secret \
  --name /core-banking/prod/mail-credentials \
  --secret-string '{
    "username":"noreply@yourbank.com",
    "password":"app_specific_password"
  }'
```

### Step 3: Create application-prod.yml

**IMPORTANT:** This file goes in the repository WITHOUT secrets.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate  # NEVER create/update in production
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true

  flyway:
    enabled: true
    baseline-on-migrate: false
    validate-on-migrate: true

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      lettuce:
        pool:
          max-active: 10
          max-idle: 5

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: ${JWT_EXPIRATION_MS:3600000}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS}

mail:
  enabled: true

rate-limiting:
  enabled: true

logging:
  level:
    root: WARN
    com.banking.system: INFO
    org.springframework: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### Step 4: EC2 Deployment Options

#### Option A: JAR Deployment with systemd

**1. Create IAM role for EC2 with permissions:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret"
      ],
      "Resource": "arn:aws:secretsmanager:*:*:secret:/core-banking/prod/*"
    }
  ]
}
```

**2. User data script for EC2:**
```bash
#!/bin/bash
set -e

# Install Java 21
yum update -y
yum install -y java-21-amazon-corretto

# Create app directory
mkdir -p /opt/core-banking
cd /opt/core-banking

# Download JAR from S3
aws s3 cp s3://my-artifacts-bucket/core-banking-system.jar ./app.jar

# Fetch secrets from AWS Secrets Manager
DB_SECRET=$(aws secretsmanager get-secret-value --secret-id /core-banking/prod/db-credentials --query SecretString --output text)
JWT_SECRET=$(aws secretsmanager get-secret-value --secret-id /core-banking/prod/jwt-secret --query SecretString --output text)

DB_HOST=$(echo $DB_SECRET | jq -r '.host')
DB_PORT=$(echo $DB_SECRET | jq -r '.port')
DB_NAME=$(echo $DB_SECRET | jq -r '.dbname')
DB_USERNAME=$(echo $DB_SECRET | jq -r '.username')
DB_PASSWORD=$(echo $DB_SECRET | jq -r '.password')

# Create systemd service
cat > /etc/systemd/system/core-banking.service <<EOF
[Unit]
Description=Core Banking System
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/core-banking
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DB_HOST=$DB_HOST"
Environment="DB_PORT=$DB_PORT"
Environment="DB_NAME=$DB_NAME"
Environment="DB_USERNAME=$DB_USERNAME"
Environment="DB_PASSWORD=$DB_PASSWORD"
Environment="JWT_SECRET=$JWT_SECRET"
Environment="REDIS_HOST=your-elasticache-endpoint.cache.amazonaws.com"
Environment="CORS_ALLOWED_ORIGINS=https://yourbank.com"
ExecStart=/usr/bin/java -jar -Xmx512m -Xms256m /opt/core-banking/app.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Start service
systemctl daemon-reload
systemctl enable core-banking
systemctl start core-banking
```

#### Option B: Docker Deployment

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/core-banking-system.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Deploy script:**
```bash
# Build image
docker build -t core-banking:latest .

# Push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin xxxx.dkr.ecr.us-east-1.amazonaws.com
docker tag core-banking:latest xxxx.dkr.ecr.us-east-1.amazonaws.com/core-banking:latest
docker push xxxx.dkr.ecr.us-east-1.amazonaws.com/core-banking:latest

# Run on EC2
docker run -d \
  --name core-banking \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-rds-endpoint \
  -e DB_USERNAME=banking_admin \
  -e DB_PASSWORD=secure_password \
  -e JWT_SECRET=your_jwt_secret \
  -e REDIS_HOST=your-elasticache-endpoint \
  -p 8080:8080 \
  xxxx.dkr.ecr.us-east-1.amazonaws.com/core-banking:latest
```

### Step 5: Application Load Balancer Setup

```bash
# Create target group
aws elbv2 create-target-group \
  --name core-banking-tg \
  --protocol HTTP \
  --port 8080 \
  --vpc-id vpc-xxxxx \
  --health-check-path /actuator/health \
  --health-check-interval-seconds 30

# Create load balancer
aws elbv2 create-load-balancer \
  --name core-banking-alb \
  --subnets subnet-xxxxx subnet-yyyyy \
  --security-groups sg-xxxxx
```

---

## 🔒 Security Checklist

### Before Production:
- [ ] All secrets in AWS Secrets Manager (not in code)
- [ ] `.env` file in `.gitignore`
- [ ] `application-prod.yml` has NO hardcoded secrets
- [ ] RDS has encryption at rest enabled
- [ ] SSL/TLS certificates configured on ALB
- [ ] Security groups configured (EC2, RDS, ElastiCache)
- [ ] IAM roles follow least privilege principle
- [ ] Database backups configured (7-30 days)
- [ ] CloudWatch alarms configured
- [ ] WAF rules configured on ALB (optional but recommended)

### After Deployment:
- [ ] Health check endpoint responding: `/actuator/health`
- [ ] Flyway migrations executed successfully
- [ ] Admin user seeded (if needed)
- [ ] Rate limiting working with ElastiCache
- [ ] Logs visible in CloudWatch
- [ ] Metrics visible in CloudWatch/Prometheus

---

## 📊 Monitoring

### CloudWatch Logs
```bash
# View application logs
aws logs tail /aws/ec2/core-banking --follow
```

### Health Check
```bash
curl https://api.yourbank.com/actuator/health
```

### Metrics
```bash
curl https://api.yourbank.com/actuator/metrics
curl https://api.yourbank.com/actuator/prometheus
```

---

## 🚨 Troubleshooting

### Application won't start
1. Check logs: `journalctl -u core-banking -f`
2. Verify environment variables: `systemctl show core-banking | grep Environment`
3. Check database connectivity: `telnet rds-endpoint 5432`

### Database connection errors
1. Check security groups (EC2 → RDS)
2. Verify credentials in Secrets Manager
3. Check RDS endpoint is correct

### Redis connection errors
1. Check ElastiCache endpoint
2. Verify security groups (EC2 → ElastiCache)
3. Confirm `rate-limiting.enabled=true` in prod

---

## 📚 Additional Resources

- [AWS Secrets Manager Best Practices](https://docs.aws.amazon.com/secretsmanager/latest/userguide/best-practices.html)
- [Spring Boot Production Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.deployment)
- [12-Factor App Methodology](https://12factor.net/)