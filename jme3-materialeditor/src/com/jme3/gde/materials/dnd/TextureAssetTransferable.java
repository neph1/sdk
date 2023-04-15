/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.materials.dnd;

import com.jme3.gde.core.dnd.AssetNameHolder;
import com.jme3.gde.core.dnd.StringDataFlavor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JPanel;

/**
 *
 * @author rickard
 */
public class TextureAssetTransferable implements Transferable {

    private DataFlavor[] flavors = new DataFlavor[]{StringDataFlavor.SHARED_INSTANCE};
    private AssetNameHolder string;

    public TextureAssetTransferable(AssetNameHolder name) {
        this.string = name;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor mine : getTransferDataFlavors()) {
            if (mine.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    public AssetNameHolder getString() {
        return string;

    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            System.out.println("returning " + getString());
            return getString();
        } else {
            throw new UnsupportedFlavorException(flavor);
        }

    }

}
