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

import com.jayfella.jme.vehicle.Steering;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * A concrete MaVehicles Vehicle with exactly four wheels (two steered front
 * wheels, two rear wheels), an engine and a gear box, as edited by the
 * vehicle creator. The first two wheels in the connection-location array are
 * the steered front wheels.
 *
 * @author jMonkeyEngine
 */
public class EditableVehicle extends Vehicle {

    public static final int NUM_WHEELS = 4;

    /**
     * Instantiate a four-wheeled Vehicle around the specified chassis spatial.
     *
     * @param name the desired name (not null)
     * @param chassis the C-G model to visualize the chassis (not null)
     * @param shape the collision shape for the chassis (not null)
     * @param mass the mass of the chassis (in kilos, &gt;0)
     * @param damping air-resistance drag on the chassis (&ge;0, &lt;1)
     * @param wheelModels one WheelModel per wheel, 4 instances (not null)
     * @param connectionLocations the suspension connection locations in
     * chassis coordinates, 4 instances (not null, unaffected)
     */
    public EditableVehicle(String name, Spatial chassis, CollisionShape shape,
            float mass, float damping, WheelModel[] wheelModels,
            Vector3f[] connectionLocations) {
        super(name);
        setChassis(chassis, shape, mass, damping);

        setEngine(new SimpleEngine("editor engine", 80_000f, 800f, 6000f));

        GearBox gearBox = new GearBox(4, 1);
        gearBox.getGear(1).setMinMaxRedKph(0f, 30f, 40f);
        gearBox.getGear(2).setMinMaxRedKph(25f, 60f, 80f);
        gearBox.getGear(3).setMinMaxRedKph(55f, 100f, 130f);
        gearBox.getGear(4).setMinMaxRedKph(95f, 150f, 190f);
        gearBox.getGear(-1).setMinMaxRedKph(-25f, -20f, -30f);
        setGearBox(gearBox);

        for (int wheelIndex = 0; wheelIndex < NUM_WHEELS; ++wheelIndex) {
            Steering steering = (wheelIndex < 2) ? Steering.DIRECT : Steering.UNUSED;
            Wheel wheel = addWheel(wheelModels[wheelIndex], connectionLocations[wheelIndex],
                    steering, 4_000f, 2_000f, 0.1f);
            wheel.setMaxSteerAngle(0.5f);
            wheel.setPowerFraction(0.25f);
        }
        build();
    }

    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.5f, -2.5f);
    }

    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.5f, -1f);
    }
}
