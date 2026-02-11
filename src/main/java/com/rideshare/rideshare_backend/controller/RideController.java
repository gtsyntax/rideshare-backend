package com.rideshare.rideshare_backend.controller;

import com.rideshare.rideshare_backend.model.DriverWithDistance;
import com.rideshare.rideshare_backend.service.DriverMatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rides")
public class RideController {
    private final DriverMatchingService matchingService;

    public RideController(DriverMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestRide(@RequestBody RideRequest request) {
        try {
            // Validate request
            if (request.pickupLatitude < -90 || request.pickupLatitude > 90) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Invalid latitude. Must be between -90 and 90"
                ));
            }

            if (request.pickupLongitude < -180 || request.pickupLongitude > 180) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "Invalid longitude. Must be between -180 and 180"
                ));
            }

            int maxDrivers = request.maxDrivers != null ? request.maxDrivers : 5;
            if (maxDrivers < 1 || maxDrivers > 20) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "success", false,
                        "message", "maxDrivers must be between 1 and 20"
                ));
            }

            // Find closest drivers using our algorithm
            List<DriverWithDistance> closestDrivers = matchingService.findClosestDrivers(
                    request.pickupLatitude,
                    request.pickupLongitude,
                    maxDrivers
            );

            if (closestDrivers.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "No drivers available in your area",
                        "driversFound", 0,
                        "nearestDrivers", List.of()
                ));
            }

            // Format response
            List<Map<String, Object>> driverInfo = closestDrivers.stream()
                    .map(dwd -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("driverId", dwd.getDriver().getId());
                        info.put("driverName", dwd.getDriver().getName());
                        info.put("latitude", dwd.getDriver().getLatitude());
                        info.put("longitude", dwd.getDriver().getLongitude());
                        info.put("distanceKm", Math.round(dwd.getDistanceKm() * 100.0) / 100.0);
                        info.put("distanceFormatted", dwd.getFormattedDistance());
                        info.put("estimatedArrivalMinutes", Math.round(dwd.getEstimatedArrivalMinutes()));
                        info.put("estimatedArrivalFormatted", dwd.getFormattedArrivalTime());
                        return info;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Found " + closestDrivers.size() + " nearby driver(s)",
                    "riderId", request.riderId,
                    "pickupLocation", Map.of(
                            "latitude", request.pickupLatitude,
                            "longitude", request.pickupLongitude
                    ),
                    "driversFound", closestDrivers.size(),
                    "nearestDrivers", driverInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Error processing ride request: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/nearby-drivers")
    public ResponseEntity<?> findNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "5") int maxDrivers) {

        try {
            List<DriverWithDistance> nearbyDrivers = matchingService.findClosestDrivers(
                    latitude,
                    longitude,
                    maxDrivers
            );

            List<Map<String, Object>> driverInfo = nearbyDrivers.stream()
                    .map(dwd -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("driverId", dwd.getDriver().getId());
                        info.put("driverName", dwd.getDriver().getName());
                        info.put("distanceKm", Math.round(dwd.getDistanceKm() * 100.0) / 100.0);
                        info.put("distanceFormatted", dwd.getFormattedDistance());
                        info.put("estimatedArrival", dwd.getFormattedArrivalTime());
                        return info;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", nearbyDrivers.size(),
                    "location", Map.of(
                            "latitude", latitude,
                            "longitude", longitude
                    ),
                    "drivers", driverInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/nearby-drivers/radius")
    public ResponseEntity<?> findDriversWithinRadius(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double maxDistanceKm,
            @RequestParam(required = false, defaultValue = "10") int maxDrivers) {

        try {
            List<DriverWithDistance> driversInRadius = matchingService.findDriversWithinRadius(
                    latitude,
                    longitude,
                    maxDistanceKm,
                    maxDrivers
            );

            List<Map<String, Object>> driverInfo = driversInRadius.stream()
                    .map(dwd -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("driverId", dwd.getDriver().getId());
                        info.put("driverName", dwd.getDriver().getName());
                        info.put("distanceKm", Math.round(dwd.getDistanceKm() * 100.0) / 100.0);
                        info.put("estimatedArrival", dwd.getFormattedArrivalTime());
                        return info;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", driversInRadius.size(),
                    "searchRadius", maxDistanceKm + " km",
                    "drivers", driverInfo
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        try {
            DriverMatchingService.DriverAvailabilityStats stats =
                    matchingService.getAvailabilityStats(latitude, longitude);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "location", Map.of(
                            "latitude", latitude,
                            "longitude", longitude
                    ),
                    "stats", Map.of(
                            "availableDrivers", stats.getAvailableDrivers(),
                            "unavailableDrivers", stats.getUnavailableDrivers(),
                            "totalDrivers", stats.getTotalDrivers(),
                            "averageDistanceKm", Math.round(stats.getAverageDistanceKm() * 100.0) / 100.0
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    public static class RideRequest {
        public String riderId;
        public Double pickupLatitude;
        public Double pickupLongitude;
        public Integer maxDrivers;
    }
}
