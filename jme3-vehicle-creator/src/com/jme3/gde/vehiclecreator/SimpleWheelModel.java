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

import com.jayfella.jme.vehicle.WheelModel;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;

/**
 * A MaVehicles WheelModel based on a plain cylinder, or on a user-supplied
 * spatial normalized to the WheelModel convention (radius=0.5 model units,
 * local +X pointing inward along the axle).
 *
 * @author jMonkeyEngine
 */
public class SimpleWheelModel extends WheelModel {

    /**
     * Create a wheel model from a generated cylinder.
     *
     * @param diameter the desired wheel diameter (in local units, &gt;0)
     * @param assetManager for loading the material (not null)
     */
    public SimpleWheelModel(float diameter, AssetManager assetManager) {
        super(diameter);
        Cylinder cylinder = new Cylinder(6, 16, 0.5f, 0.3f, true);
        Geometry geometry = new Geometry("editor wheel", cylinder);
        geometry.rotate(0f, 0f, FastMath.HALF_PI); // align the axle with the X axis
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.DarkGray);
        geometry.setMaterial(material);
        setSpatial(geometry);
    }

    /**
     * Create a wheel model from an existing spatial, scaled so that its radius
     * matches the 0.5 model-unit convention.
     *
     * @param diameter the desired wheel diameter (in local units, &gt;0)
     * @param wheelSpatial the spatial to use as the wheel visual (not null)
     */
    public SimpleWheelModel(float diameter, Spatial wheelSpatial) {
        super(diameter);
        if (wheelSpatial.getWorldBound() instanceof BoundingBox) {
            BoundingBox bound = (BoundingBox) wheelSpatial.getWorldBound();
            float boundRadius = Math.max(bound.getYExtent(), bound.getZExtent());
            if (boundRadius > 0f) {
                wheelSpatial.scale(0.5f / boundRadius);
            }
        }
        setSpatial(wheelSpatial);
    }

    @Override
    public void load(AssetManager assetManager) {
        // the model is built programmatically, nothing to load
    }
}
