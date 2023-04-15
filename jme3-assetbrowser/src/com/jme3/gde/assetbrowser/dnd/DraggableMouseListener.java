/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.assetbrowser.dnd;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author rickard
 */
public class DraggableMouseListener extends MouseAdapter {

    @Override()
    public void mousePressed(MouseEvent e) {
        JComponent c = (JComponent) e.getSource();
        TransferHandler handler = c.getTransferHandler();
        handler.exportAsDrag(c, e, TransferHandler.COPY);
    }
}
