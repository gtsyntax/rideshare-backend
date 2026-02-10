package com.rideshare.rideshare_backend.controller;

import com.rideshare.rideshare_backend.datastructure.GeohashTrie;
import com.rideshare.rideshare_backend.model.Driver;
import com.rideshare.rideshare_backend.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<?> registerDriver(@RequestBody DriverRegistrationRequest request) {
        try {
            Driver driver = new Driver(
                    request.id,
                    request.name,
                    request.latitude,
                    request.longitude
            );

            Driver registered = driverService.registerDriver(driver);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Driver registered successfully",
                    "driver", registered
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateLocation(
            @PathVariable String id,
            @RequestBody LocationUpdateRequest request) {
        try {
            Driver updated = driverService.updateDriverLocation(
                    id,
                    request.latitude,
                    request.longitude
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Location updated successfully",
                    "driver", updated
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> findNearbyDrivers(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(required = false, defaultValue = "5") int precision) {

        if (precision < 1 || precision > 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Precision must be between 1 and 8"
            ));
        }

        List<Driver> nearbyDrivers = driverService.findNearbyDrivers(latitude, longitude, precision);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", nearbyDrivers.size(),
                "searchLocation", Map.of(
                        "latitude", latitude,
                        "longitude", longitude,
                        "precision", precision
                ),
                "drivers", nearbyDrivers
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDriver(@PathVariable String id) {
        return driverService.getDriverById(id)
                .map(driver -> ResponseEntity.ok(Map.of(
                        "success", true,
                        "driver", driver
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Driver not found: " + id
                )));
    }

    @GetMapping
    public ResponseEntity<?> getAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", drivers.size(),
                "drivers", drivers
        ));
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<?> setAvailability(
            @PathVariable String id,
            @RequestBody AvailabilityRequest request) {
        try {
            Driver updated = driverService.setDriverAvailability(id, request.available);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Availability updated successfully",
                    "driver", updated
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeDriver(@PathVariable String id) {
        boolean removed = driverService.removeDriver(id);

        if (removed) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Driver removed successfully"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Driver not found: " + id
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        GeohashTrie.TrieStats stats = driverService.getTrieStats();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats,
                "totalDrivers", driverService.getTotalDrivers()
        ));
    }


    public static class DriverRegistrationRequest {
        public String id;
        public String name;
        public double latitude;
        public double longitude;
    }

    public static class LocationUpdateRequest {
        public double latitude;
        public double longitude;
    }

    public static class AvailabilityRequest {
        public boolean available;
    }
}
