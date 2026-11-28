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

import com.jayfella.jme.vehicle.GlobalAudio;
import com.jayfella.jme.vehicle.Steering;
import com.jayfella.jme.vehicle.TireSmokeEmitter;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.gde.core.assets.BinaryModelDataObject;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.scene.SceneApplication;
import com.jme3.gde.core.scene.controller.SceneToolController;
import com.jme3.gde.core.sceneexplorer.nodes.JmeSpatial;
import com.jme3.gde.core.undoredo.AbstractUndoableSceneEdit;
import com.jme3.gde.core.undoredo.SceneUndoRedoManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.KeyEvent;
import jme3utilities.DecalManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Edits a four-wheeled MaVehicles Vehicle in memory. The vehicle is built
 * around the scene's root node as chassis and can be test-driven in the
 * preview.
 *
 * @author normenhansen
 */
public class VehicleEditorController implements LookupListener, ActionListener {

    private static final Logger logger = Logger.getLogger(VehicleEditorController.class.getName());

    private final JmeSpatial jmeRootNode;
    private JmeSpatial selectedSpat;
    private final BinaryModelDataObject currentFileObject;
    private EditableVehicle vehicle;
    private Vector3f[] connectionLocations;
    private boolean bulletAttached;
    private final List<Geometry> list = new LinkedList<>();
    private SceneToolController toolController;
    private final Node toolsNode;
    private final BulletAppState bulletState;
    private final Result<JmeSpatial> result;
    private boolean testing = false;
    private float motorForce = 1f; // accelerate signal strength, 0..1
    private float brakeForce = 1f; // main brake signal strength, 0..1
    /**
     * vehicle pose (in world coordinates) captured when the last test drive
     * started, or null if none was captured
     */
    private Transform testStartWorld;
    /**
     * vehicle node's local transform when the last test drive started
     */
    private Transform testStartLocal;
    /**
     * wheel-node local transforms when the last test drive started
     */
    private Transform[] testStartWheelLocals;

    public VehicleEditorController(JmeSpatial jmeRootNode, BinaryModelDataObject currentFileObject) {
        this.jmeRootNode = jmeRootNode;
        this.currentFileObject = currentFileObject;
        toolsNode = new Node("ToolsNode");
        toolController = new SceneToolController(toolsNode, currentFileObject.getLookup().lookup(ProjectAssetManager.class));
        toolController.setShowSelection(true);
        result = Utilities.actionsGlobalContext().lookupResult(JmeSpatial.class);
        result.addLookupListener(this);
        toolsNode.addLight(new DirectionalLight());
        Node track = (Node) new DesktopAssetManager(true).loadModel("Models/Racetrack/Raceway.j3o");
        if (track.getChild("Grass").getControl(RigidBodyControl.class) == null) {
            track.getChild("Grass").addControl(new RigidBodyControl(0f));
        }
        if (track.getChild("Road").getControl(RigidBodyControl.class) == null) {
            track.getChild("Road").addControl(new RigidBodyControl(0f));
        }
        if (track.getChild("Wall").getControl(RigidBodyControl.class) == null) {
            track.getChild("Wall").addControl(new RigidBodyControl(0f));
        }
        if (track.getChild("Rumble-Strip").getControl(RigidBodyControl.class) == null) {
            track.getChild("Rumble-Strip").addControl(new RigidBodyControl(0f));
        }

        toolsNode.attachChild(track);
        bulletState = new BulletAppState();
    }

    public void prepareApplication() {
        SceneApplication.getApplication().getInputManager().addMapping("VehicleEditor_Left", new KeyTrigger(KeyInput.KEY_A));
        SceneApplication.getApplication().getInputManager().addMapping("VehicleEditor_Right", new KeyTrigger(KeyInput.KEY_D));
        SceneApplication.getApplication().getInputManager().addMapping("VehicleEditor_Up", new KeyTrigger(KeyInput.KEY_W));
        SceneApplication.getApplication().getInputManager().addMapping("VehicleEditor_Down", new KeyTrigger(KeyInput.KEY_S));
        SceneApplication.getApplication().getInputManager().addMapping("VehicleEditor_Space", new KeyTrigger(KeyInput.KEY_SPACE));
        SceneApplication.getApplication().getInputManager().addMapping("VehicleEditor_Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        SceneApplication.getApplication().getInputManager().addListener(this, "VehicleEditor_Left", "VehicleEditor_Right", "VehicleEditor_Up", "VehicleEditor_Down", "VehicleEditor_Space", "VehicleEditor_Reset");
    }

    public void cleanupApplication() {
        SceneApplication.getApplication().getInputManager().removeListener(this);
        if (bulletAttached) {
            SceneApplication.getApplication().getStateManager().detach(getBulletState());
            bulletAttached = false;
        }
    }

    public JmeSpatial getJmeRootNode() {
        return jmeRootNode;
    }

    public JmeSpatial getSelectedSpat() {
        return selectedSpat;
    }

    public void setSelectedSpat(JmeSpatial selectedSpat) {
        this.selectedSpat = selectedSpat;
    }

    public BinaryModelDataObject getCurrentFileObject() {
        return currentFileObject;
    }

    /**
     * @return the MaVehicles Vehicle being edited, or null if none was created
     */
    public EditableVehicle getVehicle() {
        return vehicle;
    }

    public void awtCall() {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(() -> {
                    doAwtCall(node);
                    return null;
                }).get();
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doAwtCall(Spatial selected) {
    }

    public void testVehicle() {
        if (jmeRootNode == null) {
            return;
        }
        final Node node = jmeRootNode.getLookup().lookup(Node.class);
        if (node != null) {
            SceneApplication.getApplication().enqueue(() -> {
                doTestVehicle(node);
                return null;
            });
        }
    }

    public void doTestVehicle(Node vehicleNode) {
        testing = true;
        if (!bulletAttached) {
            SceneApplication.getApplication().getStateManager().attach(getBulletState());
            bulletAttached = true;
        }
        bulletState.getPhysicsSpace().addAll(toolsNode);
        if (vehicle != null) {
            // the test drive starts wherever the vehicle currently sits in
            // the scene; capture that pose so the drive can be reset to it
            // (see restoreTestStart and the reset key)
            captureTestStart();
            GlobalAudio globalAudio = () -> 1f;
            vehicle.addToWorld(new EditorWorld(vehicleNode, testStartWorld), globalAudio);
            // addToWorld warped the vehicle to the drop location; MaVehicles
            // derives it with a downward ray test that moves the vehicle
            // far away when nothing is below the drop point (off the
            // track), so place it directly at the captured pose instead
            //vehicle.warpAllBodies(testStartWorld.getTranslation(),
            //        yawAngle(testStartWorld.getRotation()));
            vehicle.startEngine();
        }
    }

    /**
     * Record the vehicle's current pose, so that the test drive can start
     * from it and be reset to it.
     */
    private void captureTestStart() {
        Node node = vehicle.getNode();
        // refresh the cached world transform before reading it
        node.updateGeometricState();
        testStartWorld = node.getWorldTransform().clone();
        testStartLocal = node.getLocalTransform().clone();
        int numWheels = vehicle.countWheels();
        testStartWheelLocals = new Transform[numWheels];
        for (int i = 0; i < numWheels; ++i) {
            Spatial wheelSpatial = vehicle.getWheel(i).getVehicleWheel().getWheelSpatial();
            testStartWheelLocals[i] = wheelSpatial.getLocalTransform().clone();
        }
    }

    /**
     * Return the vehicle to the pose recorded by captureTestStart().
     */
    private void restoreTestStart() {
        if (testStartWorld == null) {
            return;
        }
        // move the physics body and zero its velocities, so neither the next
        // test drive nor a saved scene inherits the driven pose
        vehicle.warpAllBodies(testStartWorld.getTranslation(),
                yawAngle(testStartWorld.getRotation()));
        // restore the visuals; the physics control leaves them alone while
        // the body isn't simulated
        vehicle.getNode().setLocalTransform(testStartLocal);
        int numWheels = vehicle.countWheels();
        for (int i = 0; i < numWheels; ++i) {
            vehicle.getWheel(i).getVehicleWheel().getWheelSpatial()
                    .setLocalTransform(testStartWheelLocals[i]);
        }
    }

    /**
     * Extract the yaw angle (in radians) from a rotation.
     *
     * @param rotation the rotation to convert (not null, unaffected)
     * @return the rotation about the world Y axis
     */
    private static float yawAngle(Quaternion rotation) {
        float[] angles = rotation.toAngles(null);
        return angles[1];
    }

    public void stopVehicle() {
        try {
            SceneApplication.getApplication().enqueue(() -> {
                doStopVehicle();
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doStopVehicle() {
        testing = false;
        if (vehicle != null && vehicle.getWorld() != null) {
            vehicle.removeFromWorld();
            // reset the vehicle to the pose it had when testing started
            restoreTestStart();
            // keep the vehicle visible in the scene, physics is now off
            final Node rootNode = jmeRootNode.getLookup().lookup(Node.class);
            if (rootNode != null) {
                rootNode.attachChild(vehicle.getNode());
            }
        }
        bulletState.getPhysicsSpace().removeAll(toolsNode);
        if (bulletAttached) {
            SceneApplication.getApplication().getStateManager().detach(getBulletState());
            bulletAttached = false;
        }
    }

    public void centerSelected() {
        if (selectedSpat == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(() -> {
                    doCenterSelected(node);
                    return null;
                }).get();
                currentFileObject.setModified(true);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCenterSelected(final Spatial selected) {
        final Vector3f location = new Vector3f(selected.getLocalTranslation());
        selected.center();
        Lookup.getDefault().lookup(SceneUndoRedoManager.class).addEdit(this, new AbstractUndoableSceneEdit() {

            @Override
            public void sceneUndo() {
                //undo stuff here
                selected.setLocalTranslation(location);
            }

            @Override
            public void sceneRedo() {
                //redo stuff here
                selected.center();
            }

            @Override
            public void awtRedo() {
            }

            @Override
            public void awtUndo() {
            }
        });
    }

    public void addWheel(final SuspensionSettings settings) {
        if (selectedSpat == null || selectedSpat == jmeRootNode) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            final Node rootNode = jmeRootNode.getLookup().lookup(Node.class);
            if (node != null) {
                SceneApplication.getApplication().enqueue(() -> {
                    doAddWheel(node, rootNode, settings);
                    return null;
                }).get();
                currentFileObject.setModified(true);
                refreshSelectedParent();
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Reconfigure the wheel nearest to the selected spatial: its radius is
     * derived from the spatial's bounding box (or taken from the settings),
     * and the suspension/wheel settings are applied to it.
     */
    public void doAddWheel(Spatial selected, Spatial vehicleSpatial, SuspensionSettings settings) {
        if (vehicle == null || connectionLocations == null) {
            return;
        }

        //compute radius from bounding volume with scale if set
        if (settings.getBoundingScale() > 0) {
            BoundingVolume worldBound = selected.getWorldBound();
            if (worldBound instanceof BoundingBox) {
                settings.setRadius(((BoundingBox) worldBound).getYExtent() * settings.getBoundingScale());
            } else {
                logger.log(Level.WARNING, "Cannot get bounding box!");
            }
        }

        Vector3f local = vehicleSpatial.worldToLocal(selected.getWorldBound().getCenter(), new Vector3f());
        int nearest = 0;
        float nearestDistance = Float.MAX_VALUE;
        for (int i = 0; i < connectionLocations.length; ++i) {
            float distance = connectionLocations[i].distance(local);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = i;
            }
        }

        Wheel wheel = vehicle.getWheel(nearest);
        wheel.setSteering(settings.isFrontWheel() ? Steering.DIRECT : Steering.UNUSED);
        settings.applyToWheel(wheel);
    }

    public void checkVehicle() {
        if (jmeRootNode == null) {
            return;
        }
        try {
            final Node node = jmeRootNode.getLookup().lookup(Node.class);
            if (node != null) {
                if (SceneApplication.getApplication().enqueue(() -> doCheckVehicle(node)).get()) {
                    currentFileObject.setModified(true);
                    refreshRoot();
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Create the editable four-wheeled Vehicle around the scene's root node,
     * if none exists yet.
     *
     * @return true if a new Vehicle was created
     */
    public boolean doCheckVehicle(Node rootNode) {
        if (vehicle != null) {
            return false;
        }

        Vector3f halfExtents = new Vector3f(Vector3f.UNIT_XYZ);
        BoundingVolume worldBound = rootNode.getWorldBound();
        if (worldBound instanceof BoundingBox) {
            halfExtents = ((BoundingBox) worldBound).getExtent(new Vector3f());
        }
        BoxCollisionShape shape = new BoxCollisionShape(halfExtents);

        // suspension connection locations at the four corners, in chassis
        // coordinates; the first two (front, +Z) are the steered wheels.
        // The connections must sit far enough down the bounding box that the
        // wheels reach BELOW the chassis collision box, otherwise the
        // vehicle settles on the box and the wheels spin without traction.
        // Wheel reach below the connection = radius + rest length, where the
        // rest length must match MaVehicles' Vehicle.addWheel (0.2).
        Vector3f center = worldBound.getCenter(new Vector3f());
        float wheelRadius = SimpleWheelModel.WHEEL_DIAMETER / 2f;
        float restLength = 0.2f; // must match Vehicle.addWheel()
        float connectionY = center.y - halfExtents.y + wheelRadius + restLength - 0.05f;
        connectionLocations = new Vector3f[EditableVehicle.NUM_WHEELS];
        int i = 0;
        for (float x : new float[]{-1f, 1f}) {
            for (float z : new float[]{1f, -1f}) {
                Vector3f world = new Vector3f(
                        center.x + x * halfExtents.x * 0.9f,
                        connectionY,
                        center.z + z * halfExtents.z * 0.9f);
                connectionLocations[i++] = rootNode.worldToLocal(world, new Vector3f());
            }
        }

        AssetManager assetManager = SceneApplication.getApplication().getAssetManager();
        WheelModel[] wheelModels = new WheelModel[EditableVehicle.NUM_WHEELS];
        for (int j = 0; j < wheelModels.length; ++j) {
            wheelModels[j] = new SimpleWheelModel(SimpleWheelModel.WHEEL_DIAMETER, assetManager);
        }

        // the vehicle (chassis + wheels) is attached to the scene node right
        // away so it is visible; physics stays off until "test vehicle"
        Node chassis = new Node(rootNode.getName() + "-chassis");
        Geometry chassi = new Geometry("Chassi", new Box(0.5f, 0.5f, 1f));
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        chassi.setMaterial(m);
        chassis.attachChild(chassi);
        vehicle = new EditableVehicle("editor vehicle", chassis, shape, 200f, 0.1f,
                wheelModels, connectionLocations);
        rootNode.attachChild(vehicle.getNode());
        return true;
    }

    public void createHullShapeFromSelected() {
        if (selectedSpat == null || vehicle == null) {
            return;
        }
        try {
            final Spatial node = selectedSpat.getLookup().lookup(Spatial.class);
            logger.log(Level.INFO, "Creating hull shape");
            final EditableVehicle editedVehicle = vehicle;
            SceneApplication.getApplication().enqueue(() -> {
                doCreateHullShapeFromSelected(editedVehicle, node);
                return null;
            }).get();
            currentFileObject.setModified(true);
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doCreateHullShapeFromSelected(EditableVehicle editedVehicle, Spatial spat) {
        editedVehicle.getVehicleControl().setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(spat));
        refreshSelected();
    }

    /**
     * Apply the settings to the vehicle's wheels.
     *
     * @param wheels 0=all wheels, 1=front (steered) wheels, 2=rear wheels
     * @param settings the settings to apply
     */
    public void applyWheelData(final int wheels, final SuspensionSettings settings) {
        try {
            final EditableVehicle editedVehicle = this.vehicle;
            if (editedVehicle == null) {
                return;
            }
            SceneApplication.getApplication().enqueue(() -> {
                doApplyWheelData(editedVehicle, wheels, settings);
                return null;
            }).get();
            currentFileObject.setModified(true);
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void doApplyWheelData(EditableVehicle editedVehicle, int wheels, SuspensionSettings settings) {
        for (Wheel wheel : editedVehicle.listWheels()) {
            switch (wheels) {
                case 1:
                    if (!wheel.isSteering()) {
                        continue;
                    }
                    break;
                case 2:
                    if (wheel.isSteering()) {
                        continue;
                    }
                    break;
                default:
                    break;
            }
            settings.applyToWheel(wheel);
        }
        if (wheels == 0) {
            settings.applyToVehicle(editedVehicle);
        }
    }

    private void refreshSelected() {
        java.awt.EventQueue.invokeLater(() -> {
            if (getSelectedSpat() != null) {
                getSelectedSpat().refresh(false);
            }
        });

    }

    private void refreshSelectedParent() {
        java.awt.EventQueue.invokeLater(() -> {
            if (getSelectedSpat() != null) {
                ((JmeSpatial) getSelectedSpat().getParentNode()).refresh(false);
            }
        });

    }

    private void refreshRoot() {
        java.awt.EventQueue.invokeLater(() -> {
            if (getJmeRootNode() != null) {
                getJmeRootNode().refresh(true);
            }
        });

    }

    public void cleanup() {
        result.removeLookupListener(this);
        toolController.cleanup();
        if (vehicle != null && vehicle.getWorld() != null) {
            SceneApplication.getApplication().enqueue(() -> {
                doStopVehicle();
                return null;
            });
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        boolean cleared = false;
        for (JmeSpatial jmeSpatial : result.allInstances()) {
            selectedSpat = jmeSpatial;
            Spatial spat = jmeSpatial.getLookup().lookup(Spatial.class);
            toolController.updateSelection(spat);
            Geometry geom = jmeSpatial.getLookup().lookup(Geometry.class);
            if (geom != null) {
                if (!cleared) {
                    list.clear();
                    cleared = true;
                }
                logger.log(Level.INFO, "adding:{0}", jmeSpatial.getName());
                list.add(geom);
            }
        }
    }

    /**
     * @return the toolsNode
     */
    public Node getToolsNode() {
        return toolsNode;
    }

    /**
     * @return the bulletState
     */
    public BulletAppState getBulletState() {
        return bulletState;
    }

    float steeringValue = 0;

    /**
     * Forward an AWT key press/release to the test drive. The embedded
     * preview canvas is not focusable, so jME's InputManager never sees
     * keyboard events; the editor forwards them itself (see
     * VehicleCreatorTopComponent and VehicleCreatorCameraController).
     *
     * @param awtKeyCode a java.awt.event.KeyEvent key code
     * @param pressed true while the key is down, false on release
     * @return true if the key drives the test vehicle, otherwise false
     */
    public boolean forwardAwtTestKey(int awtKeyCode, boolean pressed) {
        String binding;
        switch (awtKeyCode) {
            case KeyEvent.VK_A:
                binding = "VehicleEditor_Left";
                break;
            case KeyEvent.VK_D:
                binding = "VehicleEditor_Right";
                break;
            case KeyEvent.VK_W:
                binding = "VehicleEditor_Up";
                break;
            case KeyEvent.VK_S:
                binding = "VehicleEditor_Down";
                break;
            case KeyEvent.VK_SPACE:
                binding = "VehicleEditor_Space";
                break;
            case KeyEvent.VK_ENTER:
                binding = "VehicleEditor_Reset";
                break;
            default:
                binding = null;
                break;
        }
        return forwardBinding(binding, pressed);
    }

    /**
     * Forward a jME key press/release to the test drive, like
     * forwardAwtTestKey but for com.jme3.input event codes.
     *
     * @param jmeKeyCode a com.jme3.input.KeyInput key code
     * @param pressed true while the key is down, false on release
     * @return true if the key drives the test vehicle, otherwise false
     */
    public boolean forwardJmeTestKey(int jmeKeyCode, boolean pressed) {
        String binding;
        switch (jmeKeyCode) {
            case KeyInput.KEY_A:
                binding = "VehicleEditor_Left";
                break;
            case KeyInput.KEY_D:
                binding = "VehicleEditor_Right";
                break;
            case KeyInput.KEY_W:
                binding = "VehicleEditor_Up";
                break;
            case KeyInput.KEY_S:
                binding = "VehicleEditor_Down";
                break;
            case KeyInput.KEY_SPACE:
                binding = "VehicleEditor_Space";
                break;
            case KeyInput.KEY_RETURN:
                binding = "VehicleEditor_Reset";
                break;
            default:
                binding = null;
                break;
        }
        return forwardBinding(binding, pressed);
    }

    /**
     * Enqueue a test-drive action, like the InputManager would deliver it.
     */
    private boolean forwardBinding(String binding, boolean pressed) {
        if (binding == null) {
            return false;
        }
        SceneApplication.getApplication().enqueue(() -> {
            onAction(binding, pressed, 0f);
            return null;
        });
        return true;
    }

    @Override
    public void onAction(String binding, boolean value, float f) {
        if (!testing || vehicle == null || vehicle.getWorld() == null) {
            return;
        }
        switch (binding) {
            case "VehicleEditor_Left":
                if (value) {
                    steeringValue += .5f;
                } else {
                    steeringValue += -.5f;
                }
                vehicle.steer(FastMath.clamp(steeringValue, -1f, 1f));
                break;
            case "VehicleEditor_Right":
                if (value) {
                    steeringValue += -.5f;
                } else {
                    steeringValue += .5f;
                }
                vehicle.steer(FastMath.clamp(steeringValue, -1f, 1f));
                break;
            case "VehicleEditor_Up":
                vehicle.setAccelerateSignal(value ? motorForce : 0f);
                break;
            case "VehicleEditor_Down":
            case "VehicleEditor_Space":
                vehicle.setBrakeSignals(value ? brakeForce : 0f, 0f);
                break;
            case "VehicleEditor_Reset":
                if (value && testStartWorld != null) {
                    // warpToStart() would repeat the drop-point ray test;
                    // reset directly to the captured starting pose instead
                    vehicle.warpAllBodies(testStartWorld.getTranslation(),
                            yawAngle(testStartWorld.getRotation()));
                }
                break;
            default:
                break;
        }
    }

    /**
     * @return the accelerate signal strength used while testing (0..1)
     */
    public float getMotorForce() {
        return motorForce;
    }

    /**
     * @param motorForce the accelerate signal strength (clamped to 0..1)
     */
    public void setMotorForce(float motorForce) {
        this.motorForce = FastMath.clamp(motorForce, 0f, 1f);
    }

    /**
     * @return the main brake signal strength used while testing (0..1)
     */
    public float getBrakeForce() {
        return brakeForce;
    }

    /**
     * @param brakeForce the main brake signal strength (clamped to 0..1)
     */
    public void setBrakeForce(float brakeForce) {
        this.brakeForce = FastMath.clamp(brakeForce, 0f, 1f);
    }

    /**
     * Minimal VehicleWorld backed by the SDK preview, used to test-drive the
     * edited Vehicle.
     */
    private class EditorWorld implements VehicleWorld {

        private final Node parentNode;
        private final AppStateManager stateManager;
        /**
         * vehicle pose (in world coordinates) captured when testing started;
         * doubles as the drop configuration should MaVehicles ever warp
         */
        private final Transform startTransform;

        EditorWorld(Node parentNode, Transform startTransform) {
            this.parentNode = parentNode;
            this.startTransform = startTransform;
            this.stateManager = new EditorAppStateManager(
                    SceneApplication.getApplication().getStateManager());
        }

        @Override
        public float dropYRotation() {
            return yawAngle(startTransform.getRotation());
        }

        @Override
        public AssetManager getAssetManager() {
            return SceneApplication.getApplication().getAssetManager();
        }

        @Override
        public DecalManager getDecalManager() {
            return null;
        }

        @Override
        public Node getParentNode() {
            return parentNode;
        }

        @Override
        public PhysicsSpace getPhysicsSpace() {
            return bulletState.getPhysicsSpace();
        }

        @Override
        public AppStateManager getStateManager() {
            return stateManager;
        }

        @Override
        public void locateDrop(Vector3f storeResult) {
            storeResult.set(startTransform.getTranslation());
        }
    }

    /**
     * AppStateManager that delegates to the preview's real state manager but
     * never attaches the cosmetic vehicle states that are incompatible with
     * the SDK's SceneApplication: TireSmokeEmitter casts the app to
     * SimpleApplication, and SkidMarksState needs a DecalManager plus the
     * SkidMark material, none of which the preview provides.
     */
    private static class EditorAppStateManager extends AppStateManager {

        private final AppStateManager delegate;

        EditorAppStateManager(AppStateManager delegate) {
            super(SceneApplication.getApplication());
            this.delegate = delegate;
        }

        @Override
        public boolean attach(AppState state) {
            if (state instanceof TireSmokeEmitter || state instanceof SkidMarksState) {
                return false;
            }
            return delegate.attach(state);
        }

        @Override
        public boolean detach(AppState state) {
            if (state instanceof TireSmokeEmitter || state instanceof SkidMarksState) {
                return false;
            }
            return delegate.detach(state);
        }
    }
}
