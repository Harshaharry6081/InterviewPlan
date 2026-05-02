# 📘 Day 9 – Cloud (AWS/Azure) & Observability (May 10th)

> **Goal:** Understand cloud architecture and how to monitor distributed systems.

---

## ✅ Checklist

- [ ] **Core AWS vs Azure service equivalents**
  → Know the basics: EC2/VM, S3/Blob, RDS/SQL DB, Lambda/Functions.
- [ ] **Centralized logging (ELK Stack / Loki)**
  → Ship all logs to a central place (Elasticsearch/Grafana Loki) so you can search them across all microservice instances.
- [ ] **Metrics & Alerting (Prometheus + Grafana)**
  → **Prometheus** scrapes numeric data (CPU, req/sec). **Grafana** visualizes it. Set alerts for high error rates.
- [ ] **Distributed Tracing (Micrometer Tracing / Zipkin)**
  → Tracks a single request as it flows through 5+ microservices. Essential for finding bottlenecks.
- [ ] **Terraform basics (IaC)**
  → Infrastructure as Code. Define your servers/DBs in code so they are version-controlled and repeatable.

---

## 1. AWS vs Azure — Service Equivalents

| Category | AWS | Azure |
|---|---|---|
| **Compute** | EC2 | Azure VMs |
| **Container Hosting** | ECS / EKS | AKS (Azure Kubernetes Service) |
| **Serverless** | Lambda | Azure Functions |
| **Object Storage** | S3 | Azure Blob Storage |
| **Relational DB** | RDS (PostgreSQL) | Azure Database for PostgreSQL |
| **NoSQL DB** | DynamoDB | Cosmos DB |
| **In-memory Cache** | ElastiCache (Redis) | Azure Cache for Redis |
| **Message Queue** | SQS | Azure Service Bus |
| **Event Streaming** | MSK (Kafka) | Azure Event Hubs |
| **API Gateway** | AWS API Gateway | Azure API Management |
| **Secret Management** | AWS Secrets Manager | Azure Key Vault |
| **CI/CD** | CodePipeline | Azure DevOps |
| **Monitoring** | CloudWatch | Azure Monitor |
| **DNS** | Route 53 | Azure DNS |
| **CDN** | CloudFront | Azure CDN |

---

## 2. Observability — The Three Pillars

```
      Observability
     /      |       \
  Logs   Metrics   Traces
  (What  (How      (Why it
happened) much?)   happened?)
```

### Logs — Spring Boot + Structured Logging
```java
// application.yml — Use structured JSON logs in prod
logging:
  pattern:
    console: '{"timestamp":"%d","level":"%p","service":"order-service","trace":"%X{traceId}","message":"%m"}%n'

// In code — always use SLF4J with placeholders (NOT string concat)
@Slf4j
@Service
public class OrderService {
    public Order createOrder(CreateOrderRequest req) {
        log.info("Creating order for customerId={}", req.getCustomerId()); // ✅
        // log.info("Creating order for " + req.getCustomerId()); // ❌ Slow!

        try {
            // ...
        } catch (Exception e) {
            log.error("Failed to create order for customerId={}", req.getCustomerId(), e);
            throw e;
        }
    }
}
```

### Metrics — Micrometer + Prometheus + Grafana
```yaml
# application.yml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: prometheus,health,info
```

```java
// Custom business metrics
@Service
public class OrderService {

    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;

    public OrderService(MeterRegistry registry) {
        this.orderCreatedCounter = Counter.builder("orders.created")
            .description("Total orders created")
            .tag("region", "us-east")
            .register(registry);

        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("Time to process an order")
            .register(registry);
    }

    public Order createOrder(CreateOrderRequest req) {
        return orderProcessingTimer.record(() -> {
            Order order = processOrder(req);
            orderCreatedCounter.increment();
            return order;
        });
    }
}
```

**Grafana Dashboards to talk about:**
- Request Rate (req/sec), Error Rate (%), Latency (p50, p95, p99)
- JVM Heap Usage, GC Pause Duration
- DB Connection Pool Usage
- Kafka Consumer Lag

### Distributed Tracing — Micrometer Tracing
```yaml
# application.yml (Spring Boot 3 + Micrometer Tracing)
management:
  tracing:
    sampling:
      probability: 1.0   # 100% in dev, 0.1 (10%) in prod
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

```
Request → Order Service (TraceId: abc123, SpanId: 1)
              ↓
          Product Service (TraceId: abc123, SpanId: 2)
              ↓
          PostgreSQL Query (TraceId: abc123, SpanId: 3)
              ↓
          Kafka Publish (TraceId: abc123, SpanId: 4)

All spans share the same TraceId → you can trace the full path!
```

---

## 3. Terraform Basics (IaC)

```hcl
# main.tf — Simple AWS setup

# Configure AWS provider
provider "aws" {
  region = "us-east-1"
}

# Create an RDS PostgreSQL instance
resource "aws_db_instance" "orders_db" {
  identifier        = "orders-db"
  engine            = "postgres"
  engine_version    = "15"
  instance_class    = "db.t3.micro"
  allocated_storage = 20

  db_name  = "orders"
  username = var.db_username
  password = var.db_password

  skip_final_snapshot = true  # For dev only!
  tags = {
    Environment = "dev"
    Project     = "order-service"
  }
}

# Variables
variable "db_username" { type = string }
variable "db_password" {
  type      = string
  sensitive = true
}

# Output the DB endpoint
output "db_endpoint" {
  value = aws_db_instance.orders_db.endpoint
}
```

```bash
terraform init      # Download providers
terraform plan      # Preview changes
terraform apply     # Apply changes
terraform destroy   # Tear down resources
```

---

## 4. Architecture: High Availability Checklist

```
☑ Multiple instances (3+ pods/replicas)
☑ Health checks (liveness + readiness probes)
☑ Auto-restart on failure (K8s does this)
☑ Circuit breakers (fail fast, don't cascade)
☑ Database connection pooling (HikariCP)
☑ Database read replicas (separate read/write load)
☑ CDN for static assets
☑ Multi-AZ deployment (survive datacenter failure)
☑ Graceful shutdown (drain connections before stopping)
```

### Graceful Shutdown in Spring Boot
```yaml
server:
  shutdown: graceful   # Wait for in-flight requests before stopping

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s  # Max 30s to finish
```

---

## 5. Key Interview Q&A

| Question | Answer |
|---|---|
| What is SLA/SLO/SLI? | SLA=agreement with customer (99.9% uptime). SLO=internal target. SLI=actual measurement |
| What is a 9s uptime? | 99.9% = 8.7 hrs downtime/yr. 99.99% = 52 min/yr. 99.999% = 5 min/yr |
| What is a canary deployment? | Roll out new version to 5% of traffic first, monitor, then increase |
| What is blue-green deployment? | Run two identical environments, switch traffic instantly |
| What is a CDN? | Content Delivery Network — serves static files from edge servers near the user |
| How do you handle secrets in K8s? | K8s Secrets + external tools like HashiCorp Vault or AWS Secrets Manager |
