# Ride-Sharing Backend

A real-time ride-sharing backend service built with Java and Spring Boot,
designed to master Data Structures and Algorithms through practical implementation.

## Project Goal

Learn and master DSA by building a production-ready ride-sharing application similar
to Uber, where every feature requires understanding and implementing different data structures and algorithms.

## Day 1: Geohash + Trie for Driver Location Management

### What was built

- **Geohash encoding/decoding** - Convert lat/lon to geohash strings
- **Trie (Prefix Tree)** - Custom implementation for efficient spatial queries
- **Driver management system** - Register, update, and find drivers
- **REST API** - Complete CRUD operations

### Data structures & algorithms used

#### 1. Geohash (Space-filling Curve)

- **Purpose**: Convert 2D coordinates (lat, lon) into 1D string
- **Algorithm**: Recursive binary subdivision of space
- **Why**: Nearby locations share common prefixes
- **Time Complexity**: O(K) where k = precision

#### 2. Trie (Prefix Tree)

- **Purpose**: Efficient prefix-based searching
- **Structure**: Tree where each edge is a character
- **Operations**:
    - Insert: O(k) where k = geohash length
    - Search by prefix: O(k + m) where m = results;
    - Delete: O(k)
- **Why better than HashMap**:
    - HashMap iteration: O(n) for prefix search
    - Trie: O(k) where k is constant (6 characters)
    - With 10,000 drivers: Trie is ~1,600x faster!

#### 3. Depth-First Search (DFS)

- **Purpose**: Collect all drivers in a Trie subtree
- **Algorithm**: Recursive traversal
- **Time Complexity**: O(m) where m = number of results

### API Endpoints

```
POST        /api/drivers                    - Register new driver
GET         /api/drivers                    - Get all drivers
GET         /api/drivers/{id}               - Get specific driver 
GET         /api/drivers/nearby             - Find nearby drivers (uses Trie)
PUT         /api/drivers/{id}/location      - Update driver location 
PUT         /api/drivers/{id}/availability  - Set availability 
DELETE      /api/drivers/{id}               - Remove driver
GET         /api/drivers/stats              - Get Trie statistics

