package com.rideshare.rideshare_backend.service;

import com.rideshare.rideshare_backend.datastructure.GeohashTrie;
import com.rideshare.rideshare_backend.model.Driver;
import com.rideshare.rideshare_backend.util.GeohashUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DriverService {
    private final GeohashTrie geohashTrie;
    private final Map<String, Driver> driverById;
    private static final int DEFAULT_SEARCH_PRECISION = 5;

    public DriverService() {
        this.geohashTrie = new GeohashTrie();
        this.driverById = new HashMap<>();
    }

    public Driver registerDriver(Driver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }

        if (driverById.containsKey(driver.getId())) {
            throw new IllegalArgumentException("Driver with ID " + driver.getId() + " already exists");
        }

        String geohash = GeohashUtil.encode(driver.getLatitude(), driver.getLongitude());
        driver.setGeohash(geohash);

        geohashTrie.insert(geohash, driver);

        driverById.put(driver.getId(), driver);

        return driver;
    }

    public Driver updateDriverLocation(String driverId, double newLatitude, double newLongitude) {
        Driver driver = driverById.get(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }

        String oldGeohash = driver.getGeohash();
        String newGeohash = GeohashUtil.encode(newLatitude, newLongitude);

        if (!oldGeohash.equals(newGeohash)) {
            geohashTrie.updateLocation(oldGeohash, newGeohash, driver);
            driver.setGeohash(newGeohash);
        }

        driver.updateLocation(newLatitude, newLongitude);

        return driver;
    }

    public List<Driver> findNearbyDrivers(double latitude, double longitude, int precision) {
        String searchGeohash = GeohashUtil.encode(latitude, longitude, precision);

        List<Driver> nearbyDrivers = geohashTrie.searchByPrefix(searchGeohash);

        return nearbyDrivers.stream()
                .filter(Driver::isAvailable)
                .toList();
    }

    public List<Driver> findNearbyDrivers(double latitude, double longitude) {
        return findNearbyDrivers(latitude, longitude, DEFAULT_SEARCH_PRECISION);
    }

    public Optional<Driver> getDriverById(String driverId) {
        return Optional.ofNullable(driverById.get(driverId));
    }

    public Driver setDriverAvailability(String driverId, boolean available) {
        Driver driver = driverById.get(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }

        driver.setAvailable(available);
        return driver;
    }

    public boolean removeDriver(String driverId) {
        Driver driver = driverById.get(driverId);

        if (driver == null) {
            return false;
        }

        geohashTrie.delete(driver.getGeohash(), driver);

        driverById.remove(driverId);

        return true;
    }

    public List<Driver> getAllDrivers() {
        return driverById.values().stream().toList();
    }

    public int getTotalDrivers() {
        return driverById.size();
    }

    public GeohashTrie.TrieStats getTrieStats() {
        return geohashTrie.getStats();
    }

    public void clearAll() {
        geohashTrie.clear();
        driverById.clear();
    }
}
