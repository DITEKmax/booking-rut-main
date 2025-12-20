# ElasticSearch Integration Guide for RUT Booking System

## Overview
The RUT Booking system uses ElasticSearch for advanced full-text search capabilities across room data, providing fuzzy matching, multi-field search, and Russian language support.

## Prerequisites
- ElasticSearch 8.x running on `http://localhost:9200`
- PostgreSQL database running on `localhost:5432`

## Setup Instructions

### 1. Install ElasticSearch

**On macOS (using Homebrew):**
```bash
brew tap elastic/tap
brew install elastic/tap/elasticsearch-full
brew services start elastic/elasticsearch-full
```

**On Linux (Ubuntu/Debian):**
```bash
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
echo "deb https://artifacts.elastic.co/packages/8.x/apt stable main" | sudo tee /etc/apt/sources.list.d/elastic-8.x.list
sudo apt-get update && sudo apt-get install elasticsearch
sudo systemctl start elasticsearch
```

**On Windows:**
Download from https://www.elastic.co/downloads/elasticsearch and follow installation instructions.

### 2. Verify ElasticSearch is Running
```bash
curl http://localhost:9200
```

Expected response:
```json
{
  "name" : "node-1",
  "cluster_name" : "elasticsearch",
  "version" : { ... },
  ...
}
```

### 3. Configure Application
The application is already configured in `application.properties`:
```properties
spring.elasticsearch.uris=http://localhost:9200
spring.data.elasticsearch.repositories.enabled=true
```

## How ElasticSearch Works in RUT Booking

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Application Flow                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  User Search Input → RoomService.searchRooms()              │
│                              ↓                               │
│                   RoomSearchService.searchRooms()            │
│                              ↓                               │
│                    ElasticSearch Query                       │
│                              ↓                               │
│                   Fallback to DB if ES fails                 │
│                              ↓                               │
│                   Return Room IDs → Display                  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Indexed Fields

Each room is indexed with the following fields:

| Field | Boost | Description |
|-------|-------|-------------|
| `number` | 3x | Room number (e.g., "1125") |
| `building` | 2x | Building name (e.g., "ГУК-1") |
| `equipmentText` | 2x | Equipment keywords in Russian & English |
| `description` | 1x | Room description |
| `reviews` | 1x | All review comments concatenated |
| `roomTypeDisplayName` | 1x | Room type (e.g., "Лекционная аудитория") |

### Search Features

1. **Multi-Match Query**: Searches across multiple fields simultaneously
2. **Fuzzy Matching**: Handles typos with automatic fuzziness (AUTO)
3. **Field Boosting**: Prioritizes matches in room number and building
4. **Active Filter**: Only searches active rooms (isActive = true)
5. **Graceful Fallback**: Falls back to PostgreSQL if ElasticSearch is unavailable

## How to Use ElasticSearch

### 1. Automatic Indexing

When the application starts, all rooms are automatically indexed:

**File**: `src/main/java/com/rut/booking/search/RoomSearchService.java`

```java
@PostConstruct
public void indexAllRooms() {
    // Automatically called on application startup
    // Indexes all rooms from PostgreSQL to ElasticSearch
}
```

### 2. Real-time Re-indexing

When reviews are added, updated, or deleted, the room is automatically re-indexed:

**File**: `src/main/java/com/rut/booking/services/ReviewService.java`

```java
// After review creation
roomSearchService.reindexRoomAfterReview(roomId);
```

### 3. Searching Rooms

**On the Frontend**:
- Navigate to `http://localhost:8080/rooms`
- Enter search terms in the search bar
- Examples of searches:
  - `1125` - Find room by number
  - `ГУК` - Find rooms in ГУК building
  - `проектор` - Find rooms with projector
  - `компьютеры` - Find rooms with computers
  - `лекционная` - Find lecture halls

**Search Flow**:
```
User Input → /rooms?search=1125
              ↓
         RoomController.listRooms()
              ↓
         RoomService.searchRooms("1125")
              ↓
    Try ElasticSearch first
              ↓
    If ES fails → Fallback to DB query
              ↓
         Return matched rooms
```

### 4. Search Query Structure

**File**: `src/main/java/com/rut/booking/search/RoomSearchService.java`

```java
Query multiMatchQuery = MultiMatchQuery.of(m -> m
    .query(keyword)                              // User's search input
    .fields("number^3", "building^2", ...)       // Fields with boost
    .fuzziness("AUTO")                           // Handle typos
)._toQuery();

Query boolQuery = BoolQuery.of(b -> b
    .must(multiMatchQuery)                       // Match search query
    .filter(f -> f.term(t -> t                   // Only active rooms
        .field("isActive").value(true)))
)._toQuery();
```

## Equipment Search Keywords

The system indexes equipment with both Russian and English keywords:

| Equipment | Keywords Indexed |
|-----------|------------------|
| Projector | `проектор`, `projector` |
| Computers | `компьютеры`, `computers` |
| Whiteboard | `доска`, `whiteboard`, `маркеры`, `markers` |

This allows searching in either language:
- `projector` → Finds rooms with projectors
- `проектор` → Same result

## Monitoring & Debugging

### Check ElasticSearch Status
```bash
curl http://localhost:9200/_cluster/health?pretty
```

### View Indexed Rooms
```bash
curl http://localhost:9200/rooms/_search?pretty
```

### Check Room Count
```bash
curl http://localhost:9200/rooms/_count
```

### View Specific Room Document
```bash
curl http://localhost:9200/rooms/_doc/1?pretty
```

## Testing ElasticSearch

### Test 1: Direct ElasticSearch Query
```bash
curl -X GET "http://localhost:9200/rooms/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "multi_match": {
      "query": "проектор",
      "fields": ["equipmentText", "description"]
    }
  }
}
'
```

### Test 2: Application Search
1. Start the application: `mvn spring-boot:run`
2. Navigate to: `http://localhost:8080/rooms`
3. Search for: `проектор`
4. Check console logs for ElasticSearch activity

### Test 3: Verify Indexing
```bash
# Check application logs on startup
# Should see: "Indexed X rooms to Elasticsearch"
```

## Common Issues & Solutions

### Issue 1: ElasticSearch Not Running
**Symptom**: Search falls back to database query
**Solution**: Start ElasticSearch service
```bash
# macOS
brew services start elastic/elasticsearch-full

# Linux
sudo systemctl start elasticsearch
```

### Issue 2: Connection Refused
**Symptom**: `java.net.ConnectException: Connection refused`
**Solution**: Verify ElasticSearch is running on port 9200
```bash
netstat -an | grep 9200
```

### Issue 3: No Search Results
**Symptom**: Search returns empty even though rooms exist
**Solution**: Re-index all rooms
```bash
# Restart the application to trigger @PostConstruct
# Or manually call indexAllRooms() via debugger
```

### Issue 4: Outdated Index
**Symptom**: New rooms don't appear in search
**Solution**: The application auto-indexes on startup. Restart if needed.

## Performance Benefits

### Without ElasticSearch (Database Only)
```sql
SELECT * FROM rooms
WHERE number LIKE '%1125%'
   OR building LIKE '%1125%'
   OR description LIKE '%1125%'
-- Slow on large datasets, no fuzzy matching, no ranking
```

### With ElasticSearch
- **Fuzzy Matching**: Handles typos automatically
- **Multi-field Search**: Searches across all fields at once
- **Relevance Scoring**: Results ranked by relevance
- **Fast**: Sub-second search even with 10,000+ rooms
- **Language Support**: Russian morphology (проектор → проектора)

## API Endpoints Using ElasticSearch

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/rooms?search={keyword}` | GET | Search rooms by keyword |

## Development Tips

### Adding New Searchable Fields

1. **Update RoomDocument**:
```java
@Field(type = FieldType.Text, analyzer = "russian")
private String newField;
```

2. **Update indexRoom() method**:
```java
.newField(room.getNewField())
```

3. **Update search query**:
```java
.fields("number^3", "newField^2", ...)
```

### Custom Analyzers
To add custom analyzers for better Russian language support, create an index template:

```bash
curl -X PUT "http://localhost:9200/_index_template/rooms_template" -H 'Content-Type: application/json' -d'
{
  "index_patterns": ["rooms"],
  "template": {
    "settings": {
      "analysis": {
        "analyzer": {
          "custom_russian": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": ["lowercase", "russian_stop", "russian_stemmer"]
          }
        }
      }
    }
  }
}
'
```

## Conclusion

ElasticSearch is **already integrated and working** in the RUT Booking system. It provides:
- ✅ Automatic indexing on application startup
- ✅ Real-time re-indexing when reviews change
- ✅ Fuzzy search with typo tolerance
- ✅ Multi-language support (Russian & English)
- ✅ Graceful fallback to database if ElasticSearch is down
- ✅ Field boosting for better relevance

Just ensure ElasticSearch is running on `http://localhost:9200` before starting the application!
