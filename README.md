# Spring Boot JWT Authentication (Access + Refresh Tokens)

A Spring Boot service demonstrating stateless JWT authentication using custom Spring Security
filters, backed by an embedded H2 database, with tests, Docker support, and a Swagger/OpenAPI UI.

---

## Features
- JWT access tokens (returned in the `Authorization` header) and refresh tokens (HttpOnly cookie)
- Custom `UsernamePasswordAuthenticationFilter` for login and `OncePerRequestFilter` for verifying
  the access token on protected routes
- Stateless security configuration (no server-side sessions)
- Embedded H2 database, schema auto-applied on startup from `schema.sql`
- Unit tests (JWT signing/verification, service layer with Mockito), a repository test against a
  real H2 instance, and a full-stack integration test that drives the whole login flow through
  the real security filter chain
- Interactive API docs via Swagger UI
- Multi-stage Dockerfile producing a small, non-root runtime image

---

## Configuration

All configuration lives in `src/main/resources/application.yaml`. The one setting you should
override outside of local development is the JWT signing secret:

```bash
export JWT_SECRET=$(openssl rand -base64 48)
```

If `JWT_SECRET` isn't set, a clearly-labeled development default is used — fine for trying the
app locally, not for anything real.

The H2 database file lives at `./data/authdb` (created automatically). The H2 web console is
enabled at `/h2-console` for convenience; disable it in `application.yaml` before using this
project as a base for anything internet-facing.

---

## Running locally

```bash
./mvnw spring-boot:run
```

Or run the `AuthenticationApplication` class directly from your IDE.

## Running the tests

```bash
./mvnw test
```

## Running with Docker

```bash
docker compose up --build
```

This builds the image, starts the app on `http://localhost:8080`, and persists the H2 database
file in a named volume (`h2-data`) across restarts.

---

## API docs

Once running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

Raw OpenAPI JSON is at `/v3/api-docs`. Register a user, log in via `/login` to get an access
token, then click **Authorize** in Swagger UI and paste the token to call the protected route.

---

## Endpoints

| Endpoint                  | Method | Auth Required | Description                                    |
|----------------------------|--------|----------------|------------------------------------------------|
| `/login`                   | POST   | ❌ No          | Log in; returns access token header + refresh cookie |
| `/api/v1/auth/register`    | POST   | ❌ No          | Register a new user (role `USER`)              |
| `/api/v1/auth/refresh`     | POST   | ❌ No          | Exchange a valid refresh cookie for a new access token |
| `/api/v1/auth/user/{id}`   | GET    | ❌ No          | Get a user by id                                |
| `/api/v1/protected`        | GET    | ✅ Yes         | Example protected route                         |

### Try it with curl

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'

# Log in
curl -i -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'

# Access the protected route with the access token from the login response
curl http://localhost:8080/api/v1/protected \
  -H "Authorization: Bearer <access_token>"
```

---

## Notes / possible next steps

- Swap `schema.sql` for a migration tool (Flyway/Liquibase) if this grows beyond a demo.
- Add a `logout` endpoint that clears the refresh cookie.
- Add role-based authorization checks beyond the single `USER` role currently assigned at
  registration.
