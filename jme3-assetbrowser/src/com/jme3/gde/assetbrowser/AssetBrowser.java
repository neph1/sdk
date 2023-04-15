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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
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
//    private final Preferences prefs;
    private PreviewUtil previewUtil;

    private int lastGridColumns = 0;
    private int lastGridRows = 0;
    private String lastFilter;

    /**
     * Creates new form AssetBrowser
     */
    public AssetBrowser() {
//        this.editor = editor;
//        prefs = Preferences.userNodeForPackage(this.getClass());



        initComponents();
//        jPanel1.setLayout(new GridLayout(2, 0, 3, 3));

        ComponentListener componentListener = new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = e.getComponent().getSize();
                System.out.println("component resized " + size);
                setSize(getParent().getSize());
                setPreferredSize(getParent().getSize());

                //jScrollPane1.setMaximumSize(getParent().getSize());
                //jPanel1.setMaximumSize(getParent().getSize());
//                updateGrid(size.getSize());
                java.awt.EventQueue.invokeLater(() -> {
                    loadAssets(filterField.getText().toLowerCase());
                });
//                jScrollPane1.setVerticalScrollBarPolicy();
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
    
    private void onProjectSelected() {
    }

//    @Override
//    public Dimension getMaximumSize() {
//        return getParent().getSize(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
//    }
    private void loadAssets(String filter) {
        if (assetManager == null) {
            return;
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        // use while loop to make you easer to switch to your result set.

        GridBagConstraints c = new GridBagConstraints();
        
        Dimension size = getSize();
        int rows = Math.max(size.height / 180, 1);
        int numAssets = Math.max(assetManager.getTextures().length, 1);
        int columns = numAssets / rows;
//        c.gridheight = rows;
//        c.gridwidth = columns;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1f / columns;
        c.weighty = 1f / rows;
        c.gridx = 180;
        c.gridy = 180;
        if (columns != lastGridColumns || rows != lastGridRows || !lastFilter.equals(filter)) {
            jPanel1.removeAll();
            jPanel1.setSize(new Dimension(columns * 180, size.height));
            jPanel1.setPreferredSize(new Dimension(columns * 180, size.height));
            jPanel1.setMinimumSize(new Dimension(columns * 180, size.height));
            
            jPanel1.setLayout(new GridBagLayout());
            int index = addAssets(assetManager.getTextures(), "Textures", c, columns, rows, 0, filter);
            index = addAssets(assetManager.getMaterials(), "Materials", c, columns, rows, index, filter);
            index = addAssets(assetManager.getModels(), "Models", c, columns, rows, index, filter);
            lastGridColumns = columns;
            lastGridRows = rows;
            lastFilter = filter;
        }

    }

    private void updateGrid(Dimension size) {
//        GridBagConstraints constraints = new GridBagConstraints();
//        constraints.gridheight = size.height / 150;
//        constraints.gridwidth = size.width / 150;
        int rows = size.height / 180;
        int columns = size.width / 180;
//        loadAssets();
//        GridBagConstraints c = new GridBagConstraints();
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.weightx = 0.5;
//        
//        if(rows != lastGridColumns) {
//            jPanel1.setLayout(new GridLayout(rows, 0, 3, 3));
//            lastGridColumns = rows;
//        }

    }

    private int addAssets(String[] items, String name, GridBagConstraints c, int columns, int rows, int startIndex, String filter) {
        List<String> leavesList = Arrays.asList(items);
        Collections.sort(leavesList);
        int x = 0;
        int y = 0;
        int index = startIndex;
        for (String item : leavesList) {
            if(!filter.isEmpty() && !item.toLowerCase().contains(filter)){
                continue;
            }
            AssetPreviewWidget preview = null;
            c.gridx = index % columns;
            c.gridy = index / columns;
            if (name.startsWith("Textures")) {
                final var icon = previewUtil.getOrCreateTexturePreview(item);
                if (icon != null) {
                    preview = new TexturePreview(this);
                    preview.setPreviewImage(icon);
                }
            } else if (name.startsWith("Materials")) {
                preview = new MaterialPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateMaterialPreview(item, preview));
            } else if (name.startsWith("Models")) {
                preview = new ModelPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateModelPreview(item, preview));
            }
            preview.setPreviewName(item);
            jPanel1.add(preview, c);
            x++;
            if (x == columns) {
                x = 0;
                y++;
            }
            index++;
        }
        return index;
    }

    private void addAssets(String[] items, String name) {
        List<String> leavesList = Arrays.asList(items);
        Collections.sort(leavesList);
        leavesList.forEach(item -> {
            AssetPreviewWidget preview = null;
            if (name.startsWith("Textures")) {

                final var icon = previewUtil.getOrCreateTexturePreview(item);
                if (icon != null) {
                    preview = new TexturePreview(this);
                    preview.setPreviewImage(icon);
                }
            } else if (name.startsWith("Materials")) {
                preview = new MaterialPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateMaterialPreview(item, preview));
            } else if (name.startsWith("Models")) {
                preview = new ModelPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateModelPreview(item, preview));
            }
            preview.setPreviewName(item);
            jPanel1.add(preview);
        });

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
        setPreferredSize(new java.awt.Dimension(200, 33));
        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.projectLabel.text")); // NOI18N
        projectLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                projectLabelMouseClicked(evt);
            }
        });
        add(projectLabel, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
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
        filterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterFieldKeyPressed(evt);
            }
        });
        add(filterField, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void projectLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_projectLabelMouseClicked
        assetManager = ProjectSelection.getProjectAssetManager("Select project");
        projectLabel.setText(assetManager.getProject().getProjectDirectory().getName());
        previewUtil = new PreviewUtil(assetManager);      
        createAssetBrowserFolder(assetManager);
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onDoubleClick(AssetPreviewWidget widget) {
        FileObject pf = assetManager.getAssetFileObject(widget.getPreviewName());
        if (widget instanceof MaterialPreview) {
            try {
                JMEMaterialDataObject matObject = (JMEMaterialDataObject) DataObject.find(pf);
                new MaterialPreviewOpenSupport(matObject.getPrimaryEntry()).open();
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
}
