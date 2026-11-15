# PayTrack

Multi-tenant invoicing and collections platform for the LogicVeda Java Full Stack Phase 1 project.

## Technology baseline
- Java 21
- Spring Boot 3
- PostgreSQL 16
- Redis 7
- Docker Compose

## Local infrastructure

1. Copy the environment template:

   ```bash
   cp .env.example .env
   ```

2. Start PostgreSQL and Redis:

   ```bash
   docker compose up -d
   ```

3. Check service status:

   ```bash
   docker compose ps
   docker compose logs -f
   ```

4. Stop services:

   ```bash
   docker compose down
   ```

PostgreSQL data is persisted in the `postgres_data` Docker volume. Redis data is persisted in `redis_data`.

## Local connection details

| Service | Host | Port | Default database / credentials |
|---|---|---:|---|
| PostgreSQL | localhost | 5432 | Database, user and password from `.env` |
| Redis | localhost | 6379 | No password in local development |

Do not commit `.env`. Use environment variables or a secrets manager for deployed environments.

## Suggested project structure

```text
paytrack/
├── .github/
│   └── PULL_REQUEST_TEMPLATE/
│       └── pull_request_template.md
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

As the application is implemented, add the Spring Boot Maven project, Flyway migrations, tests, API documentation and CI workflow.

## Git workflow

- Keep `main` protected.
- Create feature branches from `main`.
- Submit changes through pull requests.
- Require CI checks and at least one teammate review before merging.
- Use conventional commit messages, for example `feat: add tenant-scoped customer endpoint`.

## Phase 1 engineering reminders

- Scope tenant-owned records by `organisation_id` centrally.
- Use `BigDecimal` for money; never use floating-point types.
- Manage schema changes with Flyway.
- Never edit an issued invoice; correct it with a linked credit note.
- Add automated tenant-isolation and concurrency tests.
