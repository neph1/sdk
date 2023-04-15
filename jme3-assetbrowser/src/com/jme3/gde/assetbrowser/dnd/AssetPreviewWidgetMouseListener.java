/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.assetbrowser.dnd;

import com.jme3.gde.assetbrowser.widgets.AssetPreviewWidget;
import com.jme3.gde.assetbrowser.widgets.PreviewInteractionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * For handling drag and drop of assets
 * @author rickard
 */
public class AssetPreviewWidgetMouseListener extends MouseAdapter {

    private final AssetPreviewWidget previewWidget;
    private final PreviewInteractionListener listener;
    private boolean pressed, moved;

    public AssetPreviewWidgetMouseListener(AssetPreviewWidget previewWidget, PreviewInteractionListener listener) {
        this.previewWidget = previewWidget;
        this.listener = listener;
    }
    
    

    
    public void mouseClicked(java.awt.event.MouseEvent evt) {                                   
        if (evt.getClickCount() == 2) {
            evt.consume();
            System.out.println("double click");
            listener.onDoubleClick(previewWidget);
        }
    }                                  

    public void mousePressed(java.awt.event.MouseEvent evt) {                                  
        System.out.println("mouse pressed");
        pressed = true;
        
    }                                 

    public void mouseReleased(java.awt.event.MouseEvent evt) {                                   
        
        pressed = false;
        moved = false;
    }                                  

    public void mouseMoved(java.awt.event.MouseEvent evt) {                                
        
    }                               

    public void mouseDragged(java.awt.event.MouseEvent evt) {                                  
        System.out.println("mouse drasgged");
        if(pressed) {
            moved = true;
            TransferHandler handler = previewWidget.getTransferHandler();
            handler.exportAsDrag(previewWidget, evt, TransferHandler.COPY);
        }
    }                                 
}
