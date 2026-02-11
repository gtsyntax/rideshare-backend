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

```

## Day 2: Min-Heap (Priority Queue) for Finding K Closest Drivers

### What we built

- **Haversine Distance Calculator** - Accurate distance calculation accounting for Earth's curvature
- **Min-Heap (Priority Queue)** - Custom implementation for efficient top-K selection
- **Driver Matching Service** - Combines Trie + Min-Heap for optimal driver matching
- **Ride Request API** - Complete endpoints for requesting rides and finding drivers


### Data Structures & Algorithms Used

#### 1. Min-Heap (Priority Queue)

- **Purpose**: Efficiently find K smallest/closest elements from N candidates
- **Structure**: Complete binary tree stored in array
- **Operations**:
  - Insert: O(log n) - bubble up
  - Extract Min: O(log n) - bubble down
  - Peek: O(1) - just view root
  - Build Heap: O(n) - bottom-up heapify
- **Why Better than Sorting**:
  - Full sort: O(n log n) to sort all N drivers
  - Min-Heap: O(n log k) where k = drivers to return
  - With 10,000 drivers, finding 5 closest: Heap is 5.7x faster!

#### 2. Haversine Formula

- **Purpose**: Calculate great-circle distance between two points on Earth
- **Formula**:
```
  a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
  c = 2 × atan2(√a, √(1-a))
  distance = R × c  (where R = Earth's radius = 6371 km)
```
- **Why**: Accounts for Earth's curvature, more accurate than Euclidean distance
- **Time Complexity**: O(1)

#### 3. Complete Driver Matching Algorithm

**Combined approach using multiple data structures:**

1. **Geohash Trie** (from Day 1): O(k + m)
  - Narrow search to nearby area
  - k = geohash prefix length (~5)
  - m = drivers in area

2. **Haversine Distance**: O(m)
  - Calculate exact distance to each nearby driver
  - More accurate than geohash approximation

3. **Min-Heap**: O(m log K)
  - Insert all m nearby drivers into heap
  - Extract K closest drivers
  - K is typically small (3-5 drivers)

**Total Time Complexity**: O(m log K) where m << total drivers
- Much better than O(n log n) for sorting all drivers!

### New API Endpoints
```
POST   /api/rides/request                    - Request ride, get K closest drivers
GET    /api/rides/nearby-drivers             - Find nearby drivers
GET    /api/rides/nearby-drivers/radius      - Find drivers within radius
GET    /api/rides/availability               - Check driver availability stats
```