/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.assetbrowser.dnd;

import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;

/**
 * Pop up menu for actions on asset previews
 * 
 * @author rickard
 */
public class AssetPreviewPopupMenu extends JPopupMenu{
    
    public AssetPreviewPopupMenu(ActionListener listener){
        add("Refresh").addActionListener(listener);
        add("Delete").addActionListener(listener);
    }
}
