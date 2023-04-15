/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.jme3.gde.assetbrowser;

import com.jme3.gde.materials.MaterialPreviewOpenSupport;
import com.jme3.gde.assetbrowser.widgets.AssetPreviewWidget;
import com.jme3.gde.assetbrowser.widgets.MaterialPreview;
import com.jme3.gde.assetbrowser.widgets.ModelPreview;
import com.jme3.gde.assetbrowser.widgets.PreviewInteractionListener;
import com.jme3.gde.assetbrowser.widgets.TexturePreview;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.util.ProjectSelection;
import com.jme3.gde.materials.JMEMaterialDataObject;
import com.jme3.gde.textureeditor.JmeTextureDataObject;
import com.jme3.gde.textureeditor.OpenTexture;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author rickard
 */
public class AssetBrowser extends javax.swing.JPanel implements PreviewInteractionListener{
    
private ProjectAssetManager assetManager;
    private final Preferences prefs;
    private final PreviewUtil previewUtil;
    
    private int lastGridColumns = 0;
    
    /**
     * Creates new form AssetBrowser
     */
    public AssetBrowser() {
//        this.editor = editor;
        prefs = Preferences.userNodeForPackage(this.getClass());
        assetManager = ProjectSelection.getProjectAssetManager("Select project");
        previewUtil = new PreviewUtil(assetManager);
        
        initComponents();
//        jPanel1.setLayout(new GridLayout(2, 0, 3, 3));
        loadAssets();
        ComponentListener componentListener = new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = e.getComponent().getSize();
//                System.out.println("component resized " + size);
                setSize(getParent().getSize());
                setPreferredSize(getParent().getSize());
                
//                jScrollPane1.setMaximumSize(size);
//                jPanel1.setMaximumSize(size);
                updateGrid(getParent().getSize());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        };
        
        addComponentListener(componentListener);
    }

//    @Override
//    public Dimension getMaximumSize() {
//        return getParent().getSize(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//    }
    
    
    
    private void loadAssets() {
        createAssetBrowserFolder(assetManager);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        // use while loop to make you easer to switch to your result set.
        

//        updateGrid(getSize());
        addAssets(assetManager.getMaterials(), "Materials");
        addAssets(assetManager.getTextures(), "Textures");
        addAssets(assetManager.getModels(), "Models");
        
    }
    
    private void updateGrid(Dimension size) {
//        GridBagConstraints constraints = new GridBagConstraints();
//        constraints.gridheight = size.height / 150;
//        constraints.gridwidth = size.width / 150;
        int rows = size.height / 180;
        int columns = size.width / 180;
        if(rows != lastGridColumns) {
            jPanel1.setLayout(new GridLayout(rows, 0, 3, 3));
            lastGridColumns = rows;
        }
        
    }
    
    private void addAssets(String[] items, String name) {
        List<String> leavesList = Arrays.asList(items);
        Collections.sort(leavesList);
        leavesList.forEach(item -> {
            AssetPreviewWidget preview = null;
            if(name.startsWith("Textures")) {
                
                final var icon = previewUtil.getOrCreateTexturePreview(item);
                if(icon != null) {
                    preview = new TexturePreview(this);
                    preview.setPreviewImage(icon);
                }
            } else if(name.startsWith("Materials")) {
                preview = new MaterialPreview(this);
                preview.setPreviewImage( previewUtil.getOrCreateMaterialPreview(item, preview) );
            } else if(name.startsWith("Models")) {
                preview = new ModelPreview(this);
                preview.setPreviewImage( previewUtil.getOrCreateModelPreview(item, preview) );
            }
            preview.setPreviewName(item);
            jPanel1.add(preview);
        });
        
    }
    
    public static void createAssetBrowserFolder(ProjectAssetManager assetManager) {
        FileObject fileObject = assetManager.getProject().getProjectDirectory();
        File file = new File(fileObject.getPath(), ".assetBrowser/");
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 300));

        jPanel1.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanel1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onDoubleClick(AssetPreviewWidget widget) {
        FileObject pf = assetManager.getAssetFileObject(widget.getPreviewName());
        if(widget instanceof MaterialPreview) {
            try {
                JMEMaterialDataObject matObject = (JMEMaterialDataObject) DataObject.find(pf);
                new MaterialPreviewOpenSupport(matObject.getPrimaryEntry()).open();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if(widget instanceof TexturePreview) {
            try {
                JmeTextureDataObject textureObject = (JmeTextureDataObject) DataObject.find(pf);
                OpenTexture openTexture = new OpenTexture(textureObject);
                openTexture.actionPerformed(null);
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        } else {
            JOptionPane.showMessageDialog(null, "Not yet supported");
        }
    }
}
