/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.core.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

/**
 * Based on:
 * https://stackoverflow.com/questions/23225958/dragging-between-two-components-in-swing
 *
 * @author rickard
 * @param <T>
 */
public class AssetGrabHandler<T extends DataFlavor> extends TransferHandler {

    //private T t;
    private static final long serialVersionUID = 1L;
    private DataFlavor stringFlavor;
    // We'll be moving the strings of this list
//        private AssetPreviewWidget preview;
    private AssetNameHolder origin;
    private String content;

    public AssetGrabHandler(AssetNameHolder origin, T flavor) {
        this.content = origin.getAssetName();
        this.origin = origin;
        stringFlavor = flavor;
    }
    
    // Clients should use a static factory method to instantiate the handler
        private AssetGrabHandler() {}

    public AssetGrabHandler(String name) {
        this.content = name;
    }

        public static AssetGrabHandler createFor(AssetNameHolder origin, Class c) {
            AssetGrabHandler handler = new AssetGrabHandler(origin.getAssetName());
            handler.origin = origin;
            return handler;
        }
        
    @Override
    public boolean canImport(TransferSupport info) {
        return info.isDataFlavorSupported(stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport transferSupport) {
        Transferable t = transferSupport.getTransferable();
        System.out.println("importData 1 ");
        boolean success = false;
        try {
            String importedData = (String) t.getTransferData(stringFlavor);

            System.out.println("importData " + importedData);
//                addToListModel(importedData);
            success = true;
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    @Override
    public Transferable createTransferable(JComponent source) {
        System.out.println("createTransferable " + content + " t " + stringFlavor);
        // We need the values from the list as an object array, otherwise the data flavor won't match in importData
        return new AssetTransferable(origin);
    }

    public void setContent(String name) {
        this.content = name;
    }
    
}
