/*
 * Copyright (c) 2026 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.vehiclecreator;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jayfella.jme.vehicle.tire.TireSettings;

/**
 * Tuning parameters of a MaVehicles-based vehicle, edited in the vehicle
 * creator. Maps to the Suspension/Wheel/Engine/GearBox/TireSettings objects of
 * a MaVehicles Vehicle.
 *
 * @author jMonkeyEngine
 */
public class SuspensionSettings {

    // wheel placement, used when a wheel is reconfigured from a selected spatial
    private float boundingScale = 0; // >0: derive the wheel radius from the selected spatial's bounding box
    private float radius = 0.35f; // fixed wheel radius (in meters)
    private boolean frontWheel = false; // true for a steered wheel

    // suspension / wheel parameters
    private float restLength = 0.3f; // suspension rest length (in meters)
    private float maxForce = 10_000f; // maximum suspension force (in Newtons)
    private float stiffness = 50f; // 10=buggy, 50=sports car, 200=F1
    private float maxTravelCm = 20f; // maximum suspension travel (in centimeters)
    private float compression = 0.4f; // compression damping ratio (0=undamped, 1=critical)
    private float release = 0.6f; // relaxation damping ratio (0=undamped, 1=critical)
    private float rollInfluence = 0.1f; // influence of the wheels on chassis roll
    private float friction = 10.5f; // tire-road friction (frictionSlip)
    private float maxSteerAngle = 0.5f; // maximum steering angle (in radians)
    private float powerFraction = 0.25f; // fraction of engine power sent to the wheel

    // Pacejka tire-model coefficients, applied to all tire components
    private float tireCoeffC = 1.3f;
    private float tireCoeffB = 15.2f;
    private float tireCoeffE = -1.6f;
    private float tireLoadCoeffKA = 2.0f;
    private float tireLoadCoeffKB = 0.000055f;

    // engine / gear box parameters
    private float engineMaxWatts = 80_000f; // maximum engine power output
    private float[] gearMaxKph = {30f, 60f, 100f, 150f}; // upshift speed per forward gear
    private float reverseMaxKph = -20f; // upshift speed of the reverse gear

    public SuspensionSettings() {
    }

    /**
     * Read the parameters from a MaVehicles Wheel.
     *
     * @param wheel the wheel to read (not null)
     */
    public SuspensionSettings(Wheel wheel) {
        loadFromWheel(wheel);
    }

    public void loadFromWheel(Wheel wheel) {
        Suspension suspension = wheel.getSuspension();
        restLength = suspension.getRestLength();
        maxForce = suspension.getMaxForce();
        stiffness = suspension.getStiffness();
        compression = suspension.getCompressDamping();
        release = suspension.getRelaxDamping();
        radius = wheel.getDiameter() / 2f;
        friction = wheel.getVehicleWheel().getFrictionSlip();
        rollInfluence = wheel.getVehicleWheel().getRollInfluence();
        maxSteerAngle = wheel.getMaxSteerAngle();
        powerFraction = wheel.getPowerFraction();
    }

    public void loadFromVehicle(Vehicle vehicle) {
        engineMaxWatts = vehicle.getEngine().getMaxOutputWatts();
        GearBox gearBox = vehicle.getGearBox();
        int numGears = Math.min(gearBox.countForwardGears(), gearMaxKph.length);
        for (int i = 0; i < numGears; ++i) {
            gearMaxKph[i] = gearBox.getGear(i + 1).getMaxKph();
        }
        if (gearBox.countReverseGears() > 0) {
            reverseMaxKph = gearBox.getGear(-1).getMaxKph();
        }
    }

    /**
     * Apply the suspension/wheel/tire parameters to a MaVehicles Wheel.
     *
     * @param wheel the wheel to alter (not null)
     */
    public void applyToWheel(Wheel wheel) {
        Suspension suspension = wheel.getSuspension();
        suspension.setRestLength(restLength);
        suspension.setMaxForce(maxForce);
        suspension.setStiffness(stiffness);
        suspension.setMaxTravelCm(maxTravelCm);
        suspension.setCompressDamping(compression);
        suspension.setRelaxDamping(release);

        wheel.getVehicleWheel().setRollInfluence(rollInfluence);
        wheel.setFriction(friction);
        wheel.setDiameter(radius * 2f);
        wheel.setMaxSteerAngle(maxSteerAngle);
        wheel.setPowerFraction(powerFraction);

        TireSettings tire = new TireSettings(
                tireCoeffC, tireCoeffB, tireCoeffE, tireLoadCoeffKA, tireLoadCoeffKB);
        wheel.setTireModel(new PacejkaTireModel("editor tire", tire, tire, tire, maxForce));
    }

    /**
     * Apply the engine/gear box parameters to a MaVehicles Vehicle.
     *
     * @param vehicle the vehicle to alter (not null)
     */
    public void applyToVehicle(Vehicle vehicle) {
        vehicle.getEngine().setMaxOutputWatts(engineMaxWatts);
        GearBox gearBox = vehicle.getGearBox();
        int numGears = Math.min(gearBox.countForwardGears(), gearMaxKph.length);
        for (int i = 0; i < numGears; ++i) {
            // upshift at gearMaxKph, redline somewhat above it, downshift at the previous upshift
            float downshift = (i == 0) ? 0f : gearMaxKph[i - 1];
            gearBox.getGear(i + 1).setMinMaxRedKph(downshift, gearMaxKph[i], gearMaxKph[i] * 1.25f);
        }
        if (gearBox.countReverseGears() > 0) {
            gearBox.getGear(-1).setMinMaxRedKph(reverseMaxKph * 1.25f, reverseMaxKph, reverseMaxKph * 0.75f);
        }
    }

    public float getBoundingScale() {
        return boundingScale;
    }

    public void setBoundingScale(float boundingScale) {
        this.boundingScale = boundingScale;
    }

    public boolean isFrontWheel() {
        return frontWheel;
    }

    public void setFrontWheel(boolean frontWheel) {
        this.frontWheel = frontWheel;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRestLength() {
        return restLength;
    }

    public void setRestLength(float restLength) {
        this.restLength = restLength;
    }

    public float getMaxForce() {
        return maxForce;
    }

    public void setMaxForce(float maxForce) {
        this.maxForce = maxForce;
    }

    public float getStiffness() {
        return stiffness;
    }

    public void setStiffness(float stiffness) {
        this.stiffness = stiffness;
    }

    public float getMaxTravelCm() {
        return maxTravelCm;
    }

    public void setMaxTravelCm(float maxTravelCm) {
        this.maxTravelCm = maxTravelCm;
    }

    public float getCompression() {
        return compression;
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    public float getRelease() {
        return release;
    }

    public void setRelease(float release) {
        this.release = release;
    }

    public float getRollInfluence() {
        return rollInfluence;
    }

    public void setRollInfluence(float rollInfluence) {
        this.rollInfluence = rollInfluence;
    }

    public float getFriction() {
        return friction;
    }

    public void setFriction(float friction) {
        this.friction = friction;
    }

    public float getMaxSteerAngle() {
        return maxSteerAngle;
    }

    public void setMaxSteerAngle(float maxSteerAngle) {
        this.maxSteerAngle = maxSteerAngle;
    }

    public float getPowerFraction() {
        return powerFraction;
    }

    public void setPowerFraction(float powerFraction) {
        this.powerFraction = powerFraction;
    }

    public float getTireCoeffC() {
        return tireCoeffC;
    }

    public void setTireCoeffC(float tireCoeffC) {
        this.tireCoeffC = tireCoeffC;
    }

    public float getTireCoeffB() {
        return tireCoeffB;
    }

    public void setTireCoeffB(float tireCoeffB) {
        this.tireCoeffB = tireCoeffB;
    }

    public float getTireCoeffE() {
        return tireCoeffE;
    }

    public void setTireCoeffE(float tireCoeffE) {
        this.tireCoeffE = tireCoeffE;
    }

    public float getTireLoadCoeffKA() {
        return tireLoadCoeffKA;
    }

    public void setTireLoadCoeffKA(float tireLoadCoeffKA) {
        this.tireLoadCoeffKA = tireLoadCoeffKA;
    }

    public float getTireLoadCoeffKB() {
        return tireLoadCoeffKB;
    }

    public void setTireLoadCoeffKB(float tireLoadCoeffKB) {
        this.tireLoadCoeffKB = tireLoadCoeffKB;
    }

    public float getEngineMaxWatts() {
        return engineMaxWatts;
    }

    public void setEngineMaxWatts(float engineMaxWatts) {
        this.engineMaxWatts = engineMaxWatts;
    }

    public float[] getGearMaxKph() {
        return gearMaxKph;
    }

    public void setGearMaxKph(float[] gearMaxKph) {
        this.gearMaxKph = gearMaxKph;
    }

    public float getReverseMaxKph() {
        return reverseMaxKph;
    }

    public void setReverseMaxKph(float reverseMaxKph) {
        this.reverseMaxKph = reverseMaxKph;
    }
}
