package com.rideshare.rideshare_backend.service;

import com.rideshare.rideshare_backend.datastructure.MinHeap;
import com.rideshare.rideshare_backend.model.Driver;
import com.rideshare.rideshare_backend.model.DriverWithDistance;
import com.rideshare.rideshare_backend.util.HaversineDistanceCalculator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DriverMatchingService {
    private final DriverService driverService;
    private static final int DEFAULT_SEARCH_PRECISION = 5;
    private static final int FALLBACK_SEARCH_PRECISION = 4;
    private static final int MAX_SEARCH_PRECISION = 3;

    public DriverMatchingService(DriverService driverService) {
        this.driverService = driverService;
    }

    public List<DriverWithDistance> findClosestDrivers(
            double pickupLatitude,
            double pickupLongitude,
            int maxDrivers
    ) {
        List<Driver> nearbyDrivers = findNearbyDriversWithFallback(pickupLatitude, pickupLongitude);

        if (nearbyDrivers.isEmpty()) {
            return List.of();
        }

        MinHeap<DriverWithDistance> heap = new MinHeap<>();

        for (Driver driver : nearbyDrivers) {
            double distance = HaversineDistanceCalculator.calculateDistance(
                    pickupLatitude,
                    pickupLongitude,
                    driver.getLatitude(),
                    driver.getLongitude()
            );

            double arrivalTime = HaversineDistanceCalculator.estimateTravelTime(distance);

            DriverWithDistance driverWithDistance = new DriverWithDistance(driver, distance, arrivalTime);

            heap.insert(driverWithDistance);
        }

        int driversToReturn = Math.min(maxDrivers, heap.size());
        List<DriverWithDistance> closestDrivers = new ArrayList<>();

        for (int i = 0; i < driversToReturn; i++) {
            closestDrivers.add(heap.extractMin());
        }

        return closestDrivers;
    }

    public List<DriverWithDistance> findClosestDrivers(double pickupLatitude, double pickupLongitude) {
        return findClosestDrivers(pickupLatitude, pickupLongitude, 5);
    }

    private List<Driver> findNearbyDriversWithFallback(double latitude, double longitude) {
        List<Driver> drivers = driverService.findNearbyDrivers(latitude, longitude, DEFAULT_SEARCH_PRECISION);

        if (drivers.size() < 3) {
            drivers = driverService.findNearbyDrivers(
                    latitude,
                    longitude,
                    FALLBACK_SEARCH_PRECISION
            );
        }

        if (drivers.size() < 3) {
            drivers = driverService.findNearbyDrivers(
                    latitude,
                    longitude,
                    MAX_SEARCH_PRECISION
            );
        }

        return drivers;
    }

    public DriverWithDistance findClosestDriver(double pickupLatitude, double pickupLongitude) {
        List<DriverWithDistance> closest = findClosestDrivers(pickupLatitude, pickupLongitude, 1);
        return closest.isEmpty() ? null : closest.get(0);
    }

    public List<DriverWithDistance> findDriversWithinRadius(
            double pickupLatitude,
            double pickupLongitude,
            double maxDistanceKm,
            int maxDrivers
    ) {
        List<DriverWithDistance> closestDrivers = findClosestDrivers(
                pickupLatitude,
                pickupLongitude,
                maxDrivers * 2
        );

        return closestDrivers.stream()
                .filter(d -> d.getDistanceKm() <= maxDistanceKm)
                .limit(maxDrivers)
                .toList();
    }

    public DriverAvailabilityStats getAvailabilityStats(double latitude, double longitude) {
        List<Driver> nearby = findNearbyDriversWithFallback(latitude, longitude);

        long availableCount = nearby.stream().filter(Driver::isAvailable).count();
        long unavailableCount = nearby.size() - availableCount;

        // Calculate average distance to available drivers
        double avgDistance = 0.0;
        if (availableCount > 0) {
            List<DriverWithDistance> withDistances = findClosestDrivers(latitude, longitude, (int) availableCount);
            avgDistance = withDistances.stream()
                    .mapToDouble(DriverWithDistance::getDistanceKm)
                    .average()
                    .orElse(0.0);
        }

        return new DriverAvailabilityStats(
                (int) availableCount,
                (int) unavailableCount,
                avgDistance
        );
    }

    public static class DriverAvailabilityStats {
        private int availableDrivers;
        private int unavailableDrivers;
        private double averageDistanceKm;

        public DriverAvailabilityStats(int availableDrivers, int unavailableDrivers, double averageDistanceKm) {
            this.availableDrivers = availableDrivers;
            this.unavailableDrivers = unavailableDrivers;
            this.averageDistanceKm = averageDistanceKm;
        }

        public int getAvailableDrivers() {
            return availableDrivers;
        }

        public int getUnavailableDrivers() {
            return unavailableDrivers;
        }

        public double getAverageDistanceKm() {
            return averageDistanceKm;
        }

        public int getTotalDrivers() {
            return availableDrivers + unavailableDrivers;
        }
    }
}
