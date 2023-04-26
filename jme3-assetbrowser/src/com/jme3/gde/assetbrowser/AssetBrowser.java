/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.jme3.gde.assetbrowser;

import com.jme3.gde.assetbrowser.widgets.AssetPreviewWidget;
import com.jme3.gde.assetbrowser.widgets.MaterialPreview;
import com.jme3.gde.assetbrowser.widgets.ModelPreview;
import com.jme3.gde.assetbrowser.widgets.PreviewInteractionListener;
import com.jme3.gde.assetbrowser.widgets.TexturePreview;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.util.ProjectSelection;
import com.jme3.gde.materials.JMEMaterialDataObject;
import com.jme3.gde.materials.multiview.MaterialOpenSupport;
import com.jme3.gde.textureeditor.JmeTextureDataObject;
import com.jme3.gde.textureeditor.OpenTexture;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
public class AssetBrowser extends javax.swing.JPanel implements PreviewInteractionListener {

    private ProjectAssetManager assetManager;
    private PreviewUtil previewUtil;
    private String projectName;

    private int lastGridColumns = 0;
    private int lastGridRows = 0;
    private String lastFilter;
    
    private int sizeX = 170;
    private int sizeY = 180;

    /**
     * Creates new form AssetBrowser
     */
    public AssetBrowser() {
        
        initComponents();
        
        ComponentListener componentListener = new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
//                Dimension size = e.getComponent().getSize();
//                System.out.println("component resized " + size);
                setSize(getParent().getSize());
                setPreferredSize(getParent().getSize());
                java.awt.EventQueue.invokeLater(() -> {
                    loadAssets(filterField.getText().toLowerCase());
                });
            }

            @Override
            public void componentMoved(ComponentEvent e) {
//                setSize(new Dimension(0,0));
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
    
    private void loadAssets(String filter) {
        if (assetManager == null) {
            return;
        }
        GridBagConstraints constraints = new GridBagConstraints();
        Dimension size = jPanel1.getSize();
        
        
        int rows = Math.max(size.height / sizeY, 1);
        
        final var textures = Arrays.stream(assetManager.getTextures()).filter(s -> filter.isEmpty() || s.contains(filter)).collect(Collectors.toList());
        final var materials = Arrays.stream(assetManager.getMaterials()).filter(s -> filter.isEmpty() || s.contains(filter)).collect(Collectors.toList());
        final var models = Arrays.stream(assetManager.getModels()).filter(s -> filter.isEmpty() || s.contains(filter)).collect(Collectors.toList());
        int numAssets = textures.size() + materials.size() + models.size();
        int columns = numAssets / rows;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = sizeX;
        constraints.gridy = sizeY;
        
        
        
        Dimension newSize = new Dimension(columns * sizeX, rows * sizeY);
        System.out.println("old size " + size + " new size " + newSize);
        if (columns != lastGridColumns || rows != lastGridRows || !lastFilter.equals(filter)) {
            jPanel1.removeAll();
            jPanel1.setSize(newSize);
            jPanel1.setPreferredSize(newSize);
            
            jPanel1.setLayout(new GridBagLayout());
            
            int index = addAssets(textures, "Textures", constraints, columns, rows, 0);
            index = addAssets(materials, "Materials", constraints, columns, rows, index);
            index = addAssets(models, "Models", constraints, columns, rows, index);
            lastGridColumns = columns;
            lastGridRows = rows;
            lastFilter = filter;
        }
    }

    private int addAssets(List<String> items, String name, GridBagConstraints c, int columns, int rows, int startIndex) {
        Collections.sort(items);
        int index = startIndex;
        for (String item : items) {
            AssetPreviewWidget preview = null;
            c.gridx = index % columns;
            c.gridy = (int) (((float)index) / columns);
            if (name.startsWith("Textures")) {
                preview = new TexturePreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateTexturePreview(item));
            } else if (name.startsWith("Materials")) {
                preview = new MaterialPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateMaterialPreview(item, preview));
            } else if (name.startsWith("Models")) {
                preview = new ModelPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateModelPreview(item, preview));
            }
            if(preview == null) {
                continue;
            }
            if(assetManager.getAbsoluteAssetPath(item) != null) {
                preview.setEditable(true);
            }
            preview.setPreviewName(item);
            jPanel1.add(preview, c);
            index++;
        }
        return index;
    }

    private void createAssetBrowserFolder(ProjectAssetManager assetManager) {
        FileObject fileObject = assetManager.getProject().getProjectDirectory();
        File file = new File(fileObject.getPath(), ".assetBrowser/");
        if (!file.exists()) {
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

        projectLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        filterField = new javax.swing.JTextField();

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.projectLabel.text")); // NOI18N
        projectLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                projectLabelMouseClicked(evt);
            }
        });
        add(projectLabel, java.awt.BorderLayout.NORTH);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 300));

        jPanel1.setPreferredSize(new java.awt.Dimension(200, 300));
        jPanel1.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanel1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        filterField.setText(org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.filterField.text")); // NOI18N
        filterField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                filterFieldFocusLost(evt);
            }
        });
        filterField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                filterFieldMouseClicked(evt);
            }
        });
        filterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterFieldKeyPressed(evt);
            }
        });
        add(filterField, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void projectLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_projectLabelMouseClicked
        assetManager = ProjectSelection.getProjectAssetManager("Select project");
        projectName = assetManager.getProject().getProjectDirectory().getName();
        projectLabel.setText(projectName);
        previewUtil = new PreviewUtil(assetManager);      
        createAssetBrowserFolder(assetManager);
        
        
        System.out.println("loaded project " + projectName);

        loadAssets("");
    }//GEN-LAST:event_projectLabelMouseClicked

    private void filterFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_filterFieldFocusLost
        
    }//GEN-LAST:event_filterFieldFocusLost

    private void filterFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFieldKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_TAB || evt.getKeyCode() == KeyEvent.VK_ENTER) {
            jPanel1.requestFocusInWindow();
            loadAssets(filterField.getText().toLowerCase());
        }
    }//GEN-LAST:event_filterFieldKeyPressed

    private void filterFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterFieldMouseClicked
        //filterField.setSelectionStart(0);
        //filterField.setSelectionEnd(filterField.getSelectedText().length());
    }//GEN-LAST:event_filterFieldMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void openAsset(AssetPreviewWidget widget) {
        FileObject pf = assetManager.getAssetFileObject(widget.getPreviewName());
        if (widget instanceof MaterialPreview) {
            try {
                JMEMaterialDataObject matObject = (JMEMaterialDataObject) DataObject.find(pf);
                new MaterialOpenSupport(matObject.getPrimaryEntry()).open();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (widget instanceof TexturePreview) {
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

    @Override
    public void refreshPreview(AssetPreviewWidget widget) {
        
    }

    @Override
    public void deleteAsset(AssetPreviewWidget widget) {
        int result = JOptionPane.showConfirmDialog(null, "Delete asset? " + widget.getAssetName());
        if(result == JOptionPane.OK_OPTION) {
            
        }
    }

}
