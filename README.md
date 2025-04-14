# Spring Boot JWT Authentication Example

This is a minimal Spring Boot project demonstrating **JWT-based authentication** using **access** and **refresh tokens**. It includes one protected API route to show how Spring Security can be effectively used to secure applications.

---

## 🔐 Why Spring Security?
Spring Security provides a comprehensive and customizable authentication and authorization framework. It’s not just about securing endpoints — it also integrates deeply with the Spring ecosystem, supports various security patterns, and gives you:
- Token validation out of the box
- Filter chains for pre/post-processing
- Easy integration with role-based access control
- Best practices baked in

If you're building anything beyond a demo, **it's worth using**.

---

## Features
- JWT access and refresh tokens
- Token-based login and token renewal endpoints
- One protected route (`/api/v1/protected`) to show authentication in action
- Custom filter for handling JWT validation
- Stateless security configuration (no sessions)

---

## Project Structure

*IMPORTANT YOU DO NEED AN ENV FILE PLACED AT THE ROOT OF YOUR PROJECT WITH THE FIELDS OF*

MY_SECRET_KEY=
JWT_ISSUER=
DB_URL="jdbc:postgresql://localhost:5432/mydb"
DB_USERNAME="myuser"
DB_PASSWORD="password"

Notes
Access Tokens are returned in Authorization Header after login with the format Bearer <Token>
Just Connect Postgres DB application has initializer
Refresh tokens are sent as HttpOnly cookies.
Tokens are stateless; no session is created.
You need to remove the token and set in header manually right now, this project was tested using POSTMAN
You can expand this to add role-based authorization or logout.
Docker Compose Currently Only Starts up Postgress just run the application normally from an IDE or compile then run or add to dockerFile
JwtServiceImpl handles all signing, validation, and token parsing.


## Endpoints

| Endpoint                  | Method | Auth Required | Description                          |
|---------------------------|-------|---------------|--------------------------------------|
| `/api/auth/v1/login`      | POST  | ❌ No          | User login (returns tokens)          |
| `/api/auth/v1/createUser` | POST  | ❌ No          | User Creation (returns created user with Id) 
| `/api/auth/v1/refresh`    | POST  | ❌ No          | Gets new access token                |
| `/api/v1/user/{id}`       | GET   | ❌ No          | Gets User by Id specified            |
| `/api/v1/protected`       | GET   | ✅ Yes         | Protected example route              |
---

## 🛠️ Running the Project

```bash
./mvnw spring-boot:run
OR USE YOUR IDE TO RUN authenticationApplication

Test It
Login to get tokens
Access protected route with access token

# Login
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "pass"}'

# Access protected route
curl http://localhost:8080/ \
  -H "Authorization: Bearer <access_token>"

Feel free to fork and improve on it