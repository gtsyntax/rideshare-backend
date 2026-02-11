package com.rideshare.rideshare_backend.model;

import org.jspecify.annotations.NonNull;

public class DriverWithDistance implements Comparable<DriverWithDistance>{
    private Driver driver;
    private double distanceKm;
    private double estimatedArrivalMinutes;

    public DriverWithDistance(Driver driver, double distanceKm) {
        this.driver = driver;
        this.distanceKm = distanceKm;
        this.estimatedArrivalMinutes = (distanceKm / 40.0) * 60;
    }

    public DriverWithDistance(Driver driver, double distanceKm, double estimatedArrivalMinutes) {
        this.driver = driver;
        this.distanceKm = distanceKm;
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    @Override
    public int compareTo(DriverWithDistance other) {
        return Double.compare(this.distanceKm, other.distanceKm);
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getEstimatedArrivalMinutes() {
        return estimatedArrivalMinutes;
    }

    public void setEstimatedArrivalMinutes(double estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }

    public String getFormattedDistance() {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.2f km", distanceKm);
        }
    }

    public String getFormattedArrivalTime() {
        if (estimatedArrivalMinutes < 1.0) {
            return "< 1 min";
        } else if (estimatedArrivalMinutes < 60) {
            return String.format("%.0f mins", estimatedArrivalMinutes);
        } else {
            int hours = (int) (estimatedArrivalMinutes / 60);
            int mins = (int) (estimatedArrivalMinutes % 60);
            return String.format("%d hr %d mins", hours, mins);
        }
    }

    @Override
    public String toString() {
        return "DriverWithDistance{" +
                "driver=" + driver.getName() +
                ", distanceKm=" + String.format("%.2f", distanceKm) +
                ", estimatedArrival=" + getFormattedArrivalTime() +
                '}';
    }
}
