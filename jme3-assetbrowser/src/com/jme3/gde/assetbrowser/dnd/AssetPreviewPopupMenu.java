/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jme3.gde.assetbrowser.dnd;

import java.awt.event.ActionListener;
import javax.help.plaf.basic.BasicFavoritesNavigatorUI;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuListener;
import org.openide.actions.DeleteAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author rickard
 */
public class AssetPreviewPopupMenu extends JPopupMenu{
    
    public AssetPreviewPopupMenu(ActionListener listener){
        add("Refresh").addActionListener(listener);
        add(SystemAction.get(DeleteAction.class));
    }
}
