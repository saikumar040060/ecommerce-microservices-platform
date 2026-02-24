# E-Commerce Microservices Platform

Backend system built with Spring Boot microservices. Simulates a real e-commerce backend with user auth, product catalog, order management, and async payment processing via Kafka.

## Services

| Service | Port | Description |
|---------|------|-------------|
| eureka-server | 8761 | Service registry |
| api-gateway | 8080 | Single entry point, JWT validation |
| user-service | 8081 | Auth, user management |
| product-service | 8082 | Product catalog, Redis caching |
| order-service | 8083 | Order management, Kafka producer |
| payment-service | 8084 | Payment processing, Kafka consumer |

## Quick Start

```bash
git clone https://github.com/saikumar040060/ecommerce-microservices-platform.git
cd ecommerce-microservices-platform
docker compose up --build
```

First build takes ~5-10 minutes. All services, DBs, Kafka and Redis spin up automatically.

## API Endpoints

### Users (no auth required)
```
POST http://localhost:8080/api/users/register
POST http://localhost:8080/api/users/login
```

### Users (auth required)
```
GET  http://localhost:8080/api/users/{id}
PUT  http://localhost:8080/api/users/{id}
GET  http://localhost:8080/api/users
```

### Products (public)
```
GET  http://localhost:8080/api/products
GET  http://localhost:8080/api/products/{id}
GET  http://localhost:8080/api/products/category/{category}
GET  http://localhost:8080/api/products/search?keyword=laptop
POST http://localhost:8080/api/products       (admin)
PUT  http://localhost:8080/api/products/{id}  (admin)
DELETE http://localhost:8080/api/products/{id} (admin, soft delete)
```

### Orders (auth required)
```
POST http://localhost:8080/api/orders
GET  http://localhost:8080/api/orders/{id}
GET  http://localhost:8080/api/orders/user/{userId}
PUT  http://localhost:8080/api/orders/{id}/cancel
PUT  http://localhost:8080/api/orders/{id}/status?status=SHIPPED  (admin)
```

### Payments (auth required)
```
GET  http://localhost:8080/api/payments/order/{orderId}
GET  http://localhost:8080/api/payments/user/{userId}
PUT  http://localhost:8080/api/payments/{id}/refund
```

## Sample Requests

### Register
```json
POST /api/users/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "1234567890"
}
```

### Create Product (admin)
```json
POST /api/products
{
  "name": "MacBook Pro 14",
  "description": "Apple M3 chip, 16GB RAM",
  "price": 1999.99,
  "stock": 50,
  "category": "Laptops",
  "brand": "Apple"
}
```

### Place Order
```json
POST /api/orders
Authorization: Bearer <token>
{
  "userId": 1,
  "shippingAddress": "123 Main St, New York",
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

## Service Discovery

Once running, view all registered services at:
```
http://localhost:8761
```

## How Payment Flow Works

1. User places order → `order-service` saves order, reduces product stock
2. `order-service` publishes `ORDER_CREATED` event to Kafka
3. `payment-service` consumes event, processes payment
4. `payment-service` publishes `PAYMENT_SUCCESS` or `PAYMENT_FAILED` to Kafka
5. `order-service` consumes result, updates order status to `CONFIRMED` or `PAYMENT_FAILED`

## Tech Stack

- Java 17, Spring Boot 3.1, Spring Cloud 2022
- MySQL (separate DB per service)
- Redis (product caching, 10min TTL)
- Apache Kafka (async order→payment flow)
- Spring Cloud Gateway + Eureka
- Docker Compose
- JWT authentication
