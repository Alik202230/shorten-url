# Shortest-URL

Shortest-URL is a URL shortening service that allows users to create short, manageable links for long URLs. It supports user authentication, caching for fast access, and bulk URL processing.

## Features

- **Create Short URLs**: Generate short URLs from long original URLs.
- **Read/Fetch URLs**: Retrieve the original URL using the short key.
- **User Authentication**: Secure login and registration with JWT-based access and refresh tokens.
- **Bulk URL Processing**: Efficiently create multiple short URLs at once.
- **Caching**: Frequently accessed URLs are cached using Redis for fast retrieval.
- **Compromised Password Check**: Prevents the use of compromised passwords during login and registration.

## Tech Stack

- **Backend**: Java, Spring Boot
- **Database**: PostgreSQL (or any relational DB)
- **Caching**: Redis
- **Security**: Spring Security, JWT (Access & Refresh Tokens)
- **Testing**: JUnit 5, Mockito
- **Build Tool**: Maven
- **Optional**: Docker for containerized deployment

## Project Structure

- `User` management with roles and authentication.
- `ShortUrl` service for creating and managing shortened URLs.
- `Redis` caching layer for both short keys and original URLs.
- `JWT` tokens for secure API access.
- `Bulk processing` support for handling multiple URLs efficiently.

## Installation

1. Clone the repository:

```bash
git clone https://github.com/Alik202230/shorten-url
cd shortest-url
```
2. Configure your database and Redis in application.yml.
3. Bulid the project:

```bash
mvn clean install
```
4. Run the application:

```bash
mvn spring-boot:run
```

## API Endpoints

- POST /auth/register: Register a new user.

- POST /auth/login: Authenticate a user and obtain JWT tokens.

- POST /shorten: Create a new short URL.

- GET /{shortKey}: Redirect to the original URL.

## Notes

- Redis caching improves performance for frequently accessed URLs.

- Compromised passwords are automatically rejected during login and registration.

- JWT tokens are used for secure API communication.
