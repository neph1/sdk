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
        
        
        
        AssetNameHolder transferableObj = null;
        Transferable transferable = null;
        DataFlavor flavor = null;
        
        try {
            // Grab expected flavor
            
            transferable = dtde.getTransferable();
            DataFlavor[] flavors = transferable.getTransferDataFlavors();
            
            flavor = flavors[0];
            // What does the Transferable support
            if (transferable.isDataFlavorSupported(flavor)) {
                transferableObj = (AssetNameHolder) dtde.getTransferable().getTransferData(flavor);
            }
            
        } catch (UnsupportedFlavorException | IOException ex) { ex.printStackTrace(); }
        
        // If didn't find an item, bail
        if (transferable == null || transferableObj == null) {
            System.out.println("transferableObj == null");
            return;
        }
        
        final int dropYLoc = dtde.getLocation().y;
        final int dropXLoc = dtde.getLocation().x;
        
        // ray cast and drop model
        if (flavor instanceof SpatialDataFlavor) {
            rootPanel.addModel(transferableObj.getAssetName(), new Vector2f(dropXLoc, dropYLoc));
        } else if(flavor instanceof MaterialDataFlavor) {
            rootPanel.applyMaterial(transferableObj.getAssetName(), new Vector2f(dropXLoc, dropYLoc));
        }
        
    }
    
}
