Welcome to the URL Shortener Service! This is a simple, high-performance application built to create short, memorable links. It leverages Redis for blazing-fast lookups and atomic click counting, ensuring efficiency and scalability.

✨ Features
URL Shortening: Easily convert long URLs into short keys.

High-Performance Caching: Uses Redis to store recent URLs, drastically reducing database load.

Atomic Click Tracking: Leverages Redis's atomic operations to accurately count clicks without race conditions.

Scheduled Synchronization: Periodically syncs click counts from Redis to the database for long-term persistence.

Automated Cleanup: A scheduled task automatically removes inactive or expired URLs.

💻 Tech Stack
Backend Framework: Spring Boot

Caching & Atomic Counters: Redis

Database: Spring Data JPA (e.g., PostgreSQL, MySQL)

Build Tool: Maven

🚀 How It Works
The service uses a two-tiered approach to maximize performance and data integrity:

URL Creation:

New URL? 🤔 The service first checks if the original URL is already in the Redis cache.

Found it! 🎉 If so, it immediately returns the existing short key, saving a trip to the database.

Not found! 📝 A new unique key is generated, and the URL is saved to both the database (for persistence) and Redis (for fast lookups).

Click Tracking:

When a short URL is clicked, the service looks it up in Redis.

It then uses the atomic INCREMENT command to safely and instantly add to the click count in Redis. This prevents race conditions, ensuring every click is counted accurately. 📈

The user is then redirected to the original URL.

Scheduled Sync:

A nightly or hourly scheduled task ⏰ runs in the background.

It fetches all the click counts from Redis.

It updates the corresponding records in the database, ensuring the click data is permanently stored.

The Redis counters are then reset to zero, ready to track new clicks.

🛠️ Getting Started
Prerequisites
Java 17 or higher

Maven

Running instances of Redis and a database (e.g., PostgreSQL)

Setup and Run
Clone the repository:

Bash

git clone <repository_url>
cd url-shortener-service
Configure your application.properties file with your database and Redis connection details.

Properties

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
Build the project:

Bash

mvn clean install
Run the application:

Bash

mvn spring-boot:run
📚 API Endpoints
Create a Short URL
Endpoint: POST /api/v1/shorten

Request Body:

JSON

{
"originalUrl": "https://www.example.com/this-is-a-very-long-url"
}
Response:

JSON

{
"shortKey": "abc1234",
"originalUrl": "https://www.example.com/this-is-a-very-long-url",
"shortUrl": "http://your-app-url/abc1234"
}
Redirect to Original URL
Endpoint: GET /{shortKey}

Example: http://your-app-url/abc1234

Behavior: Redirects the user to the originalUrl and increments the click count.