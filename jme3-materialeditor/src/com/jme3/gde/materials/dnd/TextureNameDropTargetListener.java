/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.materials.dnd;

import com.jme3.gde.materials.multiview.widgets.TexturePanel;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import javax.swing.JComponent;

/**
 *
 * @author rickard
 */
public class TextureNameDropTargetListener implements DropTargetListener{

    private static final Cursor droppableCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor notDroppableCursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    
    private final TexturePanel rootPanel;

    public TextureNameDropTargetListener(TexturePanel rootPanel) {
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
        System.out.println("Step 5 of 7: The user dropped the panel. The drop(...) method will compare the drops location with other panels and reorder the panels accordingly.");
        
        this.rootPanel.setCursor(Cursor.getDefaultCursor());
        
        DataFlavor dragAndDropPanelFlavor = null;
        
        Object transferableObj = null;
        Transferable transferable = null;
        
        try {
            // Grab expected flavor
            dragAndDropPanelFlavor = StringDataFlavor.SHARED_INSTANCE;
            
            transferable = dtde.getTransferable();
            DropTargetContext c = dtde.getDropTargetContext();
            
            // What does the Transferable support
            if (transferable.isDataFlavorSupported(dragAndDropPanelFlavor)) {
                transferableObj = dtde.getTransferable().getTransferData(dragAndDropPanelFlavor);
            } 
            
        } catch (Exception ex) { ex.printStackTrace(); }
        
        // If didn't find an item, bail
        if (transferableObj == null) {
            System.out.println("transferableObj == null");
            return;
        }
        
        AssetNameHolder assetNameHolder = (AssetNameHolder) transferableObj;
        System.out.println("setting data");
        rootPanel.setAssetName("\""+assetNameHolder.getAssetName()+"\"");
    }
    
}
