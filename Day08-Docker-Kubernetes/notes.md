# 📘 Day 8 – Docker & Kubernetes (May 9th)

> **Goal:** Package and deploy your Spring Boot app like a DevOps engineer.

---

## ✅ Checklist

- [ ] **Write a production-ready Dockerfile**
  → Use **Multi-stage builds** to keep images small. Run as a non-root user for security.
- [ ] **docker-compose.yml for local dev**
  → Defines your app + DB + Redis + Kafka as a single stack for easy local testing.
- [ ] **Kubernetes core objects (Pod, Deployment, Service)**
  **Pod**: Smallest unit. **Deployment**: Manages replicas/rollouts. **Service**: Stable IP/DNS for pods.
- [ ] **ConfigMaps and Secrets**
  → Decouple configuration (ConfigMap) and sensitive data (Secrets) from the container image.
- [ ] **Health checks and resource limits**
  → **Liveness/Readiness probes** for auto-healing. **CPU/Memory limits** to prevent one container from crashing the node.

---

## 1. Production Dockerfile for Spring Boot

```dockerfile
# ===== Stage 1: Build =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven/Gradle files first for layer caching
COPY mvnw pom.xml ./
COPY .mvn .mvn/
RUN ./mvnw dependency:go-offline -B   # Cache dependencies

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ===== Stage 2: Run (smaller image) =====
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Security: Don't run as root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy only the built jar from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Health check (K8s and Docker can use this)
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build & Run:**
```bash
docker build -t order-service:1.0 .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://host:5432/orders \
  order-service:1.0
```

---

## 2. docker-compose.yml — Full Local Dev Stack

```yaml
version: '3.8'

services:
  # Your Spring Boot App
  order-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_URL: jdbc:postgresql://postgres:5432/orders_db
      DB_USER: admin
      DB_PASS: secret
      MONGO_URI: mongodb://mongo:27017/products_db
      REDIS_HOST: redis
      KAFKA_BROKERS: kafka:9092
    depends_on:
      postgres:
        condition: service_healthy
      mongo:
        condition: service_started
      redis:
        condition: service_started
    networks:
      - app-network

  # PostgreSQL
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: orders_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d orders_db"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

  # MongoDB
  mongo:
    image: mongo:6
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    networks:
      - app-network

  # Redis
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - app-network

  # Kafka + Zookeeper
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    networks:
      - app-network

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    depends_on:
      - zookeeper
    networks:
      - app-network

volumes:
  postgres_data:
  mongo_data:
  redis_data:

networks:
  app-network:
    driver: bridge
```

```bash
# Start all services
docker compose up -d

# View logs of app only
docker compose logs -f order-service

# Stop everything
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

---

## 3. Kubernetes — Core Concepts

```
Cluster
  └── Node (VM / Physical server)
        └── Pod (smallest deployable unit — 1+ containers)
```

### Deployment (manages Pods)
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  labels:
    app: order-service
spec:
  replicas: 3          # Run 3 pods
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
        - name: order-service
          image: your-registry/order-service:1.0
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: DB_PASS
              valueFrom:
                secretKeyRef:           # Read from Secret
                  name: db-secrets
                  key: postgres-password
            - name: APP_CONFIG
              valueFrom:
                configMapKeyRef:        # Read from ConfigMap
                  name: app-config
                  key: log-level
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
```

### Service (exposes Pods)
```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: order-service
spec:
  selector:
    app: order-service     # Routes to Pods with this label
  ports:
    - protocol: TCP
      port: 80             # Service port
      targetPort: 8080     # Container port
  type: ClusterIP          # Internal only (use LoadBalancer for external)
```

### ConfigMap & Secret
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  log-level: "INFO"
  max-connections: "100"

---
# k8s/secret.yaml (values are base64 encoded)
apiVersion: v1
kind: Secret
metadata:
  name: db-secrets
type: Opaque
data:
  postgres-password: c2VjcmV0   # echo -n 'secret' | base64
  postgres-username: YWRtaW4=   # echo -n 'admin' | base64
```

### Useful kubectl Commands
```bash
# Apply all configs in a folder
kubectl apply -f k8s/

# View pods
kubectl get pods
kubectl describe pod order-service-abc123

# View logs
kubectl logs order-service-abc123
kubectl logs -f order-service-abc123  # Follow logs

# Scale up/down
kubectl scale deployment order-service --replicas=5

# Rolling update (zero downtime)
kubectl set image deployment/order-service order-service=your-registry/order-service:2.0

# Rollback
kubectl rollout undo deployment/order-service

# Port forward (for local testing)
kubectl port-forward pod/order-service-abc123 8080:8080
```

---

## 4. Spring Boot Actuator (Required for K8s)

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true  # Enables /actuator/health/liveness and /readiness
```

---

## 5. Key Interview Q&A

| Question | Answer |
|---|---|
| Container vs VM? | Container: shares OS kernel, lightweight, fast. VM: full OS, isolated, slower to start |
| What is a Docker layer? | Each instruction in Dockerfile creates a read-only layer. Layers are cached |
| Pod vs Deployment? | Pod = single instance. Deployment = manages pod replicas, rolling updates, rollbacks |
| What is a Kubernetes Service? | Stable network endpoint (IP/DNS) that load-balances traffic to Pod replicas |
| Liveness vs Readiness probe? | Liveness: is the app alive (restart if not). Readiness: is app ready to receive traffic |
| How does K8s ensure HA? | ReplicaSets, node affinity, pod disruption budgets, auto-healing (restarts failed pods) |
| What is HPA? | Horizontal Pod Autoscaler — scales pod count based on CPU/memory metrics |
