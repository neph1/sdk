/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.core.dnd;

import com.jme3.gde.core.sceneviewer.SceneViewerTopComponent;
import com.jme3.math.Vector2f;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

/**
 *
 * @author rickard
 */
public class SceneViewerDropTargetListener implements DropTargetListener{

    private static final Cursor droppableCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor notDroppableCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    
    private final SceneViewerTopComponent rootPanel;

    public SceneViewerDropTargetListener(SceneViewerTopComponent rootPanel) {
        this.rootPanel = rootPanel;
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }
    
    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        if (!this.rootPanel.getCursor().equals(droppableCursor)) {
            this.rootPanel.setCursor(droppableCursor);
        }
    }
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }
    
    @Override
    public void dragExit(DropTargetEvent dte) {
        this.rootPanel.setCursor(notDroppableCursor);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        this.rootPanel.setCursor(Cursor.getDefaultCursor());
        
        
        
        Object transferableObj = null;
        Transferable transferable = null;
        
        
        try {
            // Grab expected flavor
            
            transferable = dtde.getTransferable();
            
            // What does the Transferable support
            if (transferable.isDataFlavorSupported(SpatialDataFlavor.instance)) {
                transferableObj = dtde.getTransferable().getTransferData(SpatialDataFlavor.instance);
            }
            if(transferableObj == null) {
                if (transferable.isDataFlavorSupported(MaterialDataFlavor.instance)) {
                    transferableObj = dtde.getTransferable().getTransferData(MaterialDataFlavor.instance);
                }
            }
            
        } catch (UnsupportedFlavorException | IOException ex) { ex.printStackTrace(); }
        
        // If didn't find an item, bail
        if (transferable == null || transferableObj == null) {
            System.out.println("transferableObj == null");
            return;
        }
        
        
        AssetNameHolder assetNameHolder = (AssetNameHolder) transferableObj;
        
        final int dropYLoc = dtde.getLocation().y;
        final int dropXLoc = dtde.getLocation().x;
        
        // ray cast and drop model
        if (transferable.isDataFlavorSupported(SpatialDataFlavor.instance)) {
            rootPanel.addModel(assetNameHolder.getAssetName(), new Vector2f(dropXLoc, dropYLoc));
        } else if(transferable.isDataFlavorSupported(MaterialDataFlavor.instance)) {
            rootPanel.applyMaterial(assetNameHolder.getAssetName(), new Vector2f(dropXLoc, dropYLoc));
        }
        
    }
    
}
