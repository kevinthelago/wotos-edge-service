# WoToS Edge Service

API gateway for the [WoToS](https://github.com/users/kevinthelago/projects/2) system. All requests from the React frontend arrive here; this service fans them out to the player, statistics, and vehicle microservices using Feign clients, aggregates the results, and returns them to the caller.

The React UI is hardcoded to call this service at `http://localhost:8081/api`.

## Prerequisites

- Java 8 (Temurin recommended)
- Maven or the included `./mvnw` wrapper
- All downstream services running:
  - `wotos-player-service`
  - `wotos-statistics-service`
  - `wotos-vehicle-service`
- `wotos-eureka-server` running (service registry)
- `wotos-config-server` running at `localhost:4040`

## Running Locally

### Command Line

```bash
./mvnw spring-boot:run
```

### IntelliJ

1. Open the project root in IntelliJ IDEA.
2. Run `WotosEdgeServiceApplication` — no additional environment variables required.

## Building

```bash
./mvnw clean package        # build JAR, skip tests
./mvnw clean install        # build JAR + run all tests
```

## API Endpoints

All endpoints are prefixed `/api` and are CORS-open for local development.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/players` | Get full player details (player data + statistics) for given account IDs |
| `GET` | `/api/players/list` | Search for players by nickname |
| `GET` | `/api/vehicles` | Get vehicle data from Tankopedia |

### `/api/players/list` query parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `nicknames` | `String[]` | Nicknames to search |
| `searchType` | `String` | WoT search type (`startswith` or `exact`) |
| `language` | `String` | Response language (default: `en`) |
| `limit` | `Integer` | Max results (max: 100) |
