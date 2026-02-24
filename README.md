# Trade Risk System

A backend system for executing trades, tracking portfolio positions, and computing real-time risk metrics. Built with Spring Boot 3, MySQL, Redis, and Docker.

## What it does

Traders submit buy/sell orders through a REST API. The system validates the order, updates the trader's position and portfolio value atomically, then persists an audit trail. A separate risk engine computes exposure, concentration risk, and a normalized risk score for any portfolio on demand.

Positions use a weighted average price model. Sells are validated against held quantity before execution. All financial arithmetic uses `BigDecimal` with explicit scale and rounding to avoid floating-point drift.

## Architecture

| Layer | Responsibility |
|-------|---------------|
| Controllers | REST endpoints, pagination, validation, role gating |
| Services | Trade execution, risk calculation, portfolio management |
| Security | Stateless JWT auth, BCrypt passwords, RBAC (ADMIN, TRADER) |
| Persistence | JPA with Flyway migrations, pessimistic locking on positions |
| Caching | Redis-backed with targeted eviction on writes |

Schema is managed by Flyway (not Hibernate `ddl-auto`). The position lookup used during trade execution acquires a `SELECT FOR UPDATE` lock to prevent race conditions on concurrent sells.

## Tech

Spring Boot 3.5 / Spring Security 6 / Hibernate / MySQL 8 / Redis 7 / Docker

## Running locally

Copy the example env file and fill in your values:

```
cp .env.example .env
```

You need three variables:

| Variable | What it is |
|----------|-----------|
| `DB_PASSWORD` | MySQL root password |
| `JWT_SECRET` | Base64 HS256 key, generate with `openssl rand -base64 32` |
| `REDIS_PASSWORD` | Redis AUTH password |

Then start everything:

```
docker compose up --build
```

The app starts on port 8080. Check health:

```
curl http://localhost:8080/actuator/health
```

API docs are available at `/swagger-ui.html` once the app is running.

## Access control

| Role | Access |
|------|--------|
| ADMIN | Instrument CRUD (market data) |
| TRADER | Trade execution |
| Any authenticated user | Portfolio and risk queries |

## Risk model

Given a portfolio's positions:

```
total_exposure    = sum(quantity * market_price)  for each position
concentration     = max_position_value / total_exposure
risk_score        = concentration * 100
```

These are persisted as snapshots for historical analysis.

## Trade execution flow

1. Validate instrument exists. For sells, verify sufficient held quantity.
2. Create trade record in PENDING status.
3. Update position (weighted avg price on buys, reduce/delete on sells). This step holds a row-level lock.
4. Recalculate portfolio total value from all current positions.
5. Mark trade EXECUTED. Write audit log.

## Project structure

```
src/main/java/com/trade_risk_system/
    config/         Redis, security, Swagger config
    controller/     REST endpoints
    dto/            Request/response records
    exception/      Global error handling
    model/          JPA entities
    repository/     Data access, custom queries
    security/       JWT filter, auth provider
    service/        Business logic
    util/           MoneyUtils (BigDecimal helpers)

src/main/resources/
    db/migration/   Flyway SQL scripts (V1 baseline, V2 decimal, V3 versioning)
    application.yml Config (all secrets via env vars)
```

## License

None specified.
