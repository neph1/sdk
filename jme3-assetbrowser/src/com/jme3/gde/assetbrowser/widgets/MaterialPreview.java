/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.assetbrowser.widgets;

import com.jme3.gde.core.dnd.MaterialDataFlavor;
import com.jme3.gde.core.dnd.AssetGrabHandler;

/**
 *
 * @author rickard
 */
public class MaterialPreview extends AssetPreviewWidget{
    
    public MaterialPreview(PreviewInteractionListener listener) {
        super(listener);
        setTransferHandler(new AssetGrabHandler(this, new MaterialDataFlavor()));
    }

}
