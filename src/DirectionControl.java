/*
 * Copyright (C) 2025 Shivaji Patil, College of the North Atlantic
 * All rights reserved.
 *
 * Aircraft Simulation Project
 */

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Import statements that are necessary for the functionality of the listeners below.
 */

/**
 * Direction control system for an aircraft axis. Manages a current value and a
 * target value, and adjusts the current value over time toward the target
 * using a physics-based movement (inertia, dampening, tolerance, max step).
 */
public class DirectionControl {
    private String name;
    private double currentValue;
    private double targetValue;
    private double velocity;
    private double min;
    private double max;
    private double inertia;
    private double dampening;
    private double tolerance;
    private double maxStep;

    // Statistics tracking
    private double totalDeviation = 0;
    private double maxDeviation = 0;
    private int sampleCount = 0;
    private boolean trackStatistics = true;

    // Getters for correction mechanism display
    public String getName() { return name; }
    public double getInertia() { return inertia; }
    public double getDampening() { return dampening; }
    public double getTolerance() { return tolerance; }
    public double getVelocity() { return velocity; }

    // Protected setters so subclasses can override physics parameters
    protected void setInertia(double inertia) { this.inertia = inertia; }
    protected void setDampening(double dampening) { this.dampening = dampening; }
    protected void setTolerance(double tolerance) { this.tolerance = tolerance; }

    public DirectionControl(String name, double min, double max, ConfigLoader config) {
        this.name = name;
        this.min = min;
        this.max = max;

        // Read configuration values with defaults if not specified
        this.inertia = config.getDouble(name.toLowerCase() + ".inertia", 1.0);
        this.dampening = config.getDouble(name.toLowerCase() + ".dampening", 0.95);
        this.tolerance = config.getDouble(name.toLowerCase() + ".tolerance", 2.0);
        this.maxStep = config.getDouble(name.toLowerCase() + ".maxStep", 3.0);

        this.currentValue = 0;
        this.targetValue = 0;
        this.velocity = 0;
    }

    /**
     * Added Listeners for adding, removing, notifying changes in current value.
     */

    private final List<DirectionControlListener> listeners = new CopyOnWriteArrayList<>();

    public interface DirectionControlListener {
        void currentValueChanged(DirectionControl source, double newValue);
    }

    public void addListener(DirectionControlListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(DirectionControlListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(double newValue) {
        for (DirectionControlListener listener : listeners) {
            listener.currentValueChanged(this, newValue);
        }
    }

    /**
     * Update the current value based on the physics model and target.
     */
    public void update() {
        double oldValue;
        double newValue;

        /**
         * Changed the update function to work with notifyListeners when the value changes.
         */

        synchronized (this) {
            double deviation = targetValue - currentValue;
            oldValue = currentValue;

            if (trackStatistics) {
                totalDeviation += Math.abs(deviation);
                maxDeviation = Math.max(maxDeviation, Math.abs(deviation));
                sampleCount++;
            }

            Main.logToCSV(name, targetValue, currentValue, velocity);

            if (Math.abs(deviation) < tolerance && Math.abs(velocity) < 0.1) {
                velocity = 0;
                return;
            }

            velocity += deviation / inertia;
            velocity *= dampening;

            if (velocity > maxStep) velocity = maxStep;
            if (velocity < -maxStep) velocity = -maxStep;

            currentValue += velocity;

            if (currentValue < min) {
                currentValue = min;
                velocity = 0;
            } else if (currentValue > max) {
                currentValue = max;
                velocity = 0;
            }

            newValue = currentValue;
        }

        if (Math.abs(newValue - oldValue) > 1e-6) {
            notifyListeners(newValue);
        }
    }

    public Map<String, Double> getStatistics() {
        Map<String, Double> stats = new HashMap<>();
        stats.put("sampleCount", (double) sampleCount);
        stats.put("averageDeviation", sampleCount > 0 ? totalDeviation / sampleCount : 0);
        stats.put("maxDeviation", maxDeviation);
        return stats;
    }

    public synchronized double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double value) {
        double oldValue;
        double newValue;
        synchronized (this) {
            oldValue = currentValue;
            currentValue = Math.max(min, Math.min(max, value));
            newValue = currentValue;
        }
        if (Math.abs(newValue - oldValue) > 1e-6) {
            notifyListeners(newValue);
        }
    }

    public synchronized double getTargetValue() { return targetValue; }
    public synchronized void setTargetValue(double value) {
        if (value < min) value = min;
        if (value > max) value = max;
        this.targetValue = value;
    }
}
