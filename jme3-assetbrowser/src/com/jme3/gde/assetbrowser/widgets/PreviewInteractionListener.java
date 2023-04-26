/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.assetbrowser.widgets;

/**
 *
 * @author rickard
 */
public interface PreviewInteractionListener {
    
    void openAsset(AssetPreviewWidget widget);
    
    void refreshPreview(AssetPreviewWidget widget);
    
    void deleteAsset(AssetPreviewWidget widget);
}
