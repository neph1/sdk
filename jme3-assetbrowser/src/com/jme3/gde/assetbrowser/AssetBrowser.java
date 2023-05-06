/*
 *  Copyright (c) 2009-2023 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.assetbrowser;

import com.jme3.gde.assetbrowser.icons.Icons;
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
    private int imageSize = 150;
    private int oldSliderValue = 2;

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
//                setMinimumSize(getParent().getSize());
                getLayout().layoutContainer(AssetBrowser.this);
                //repaint();

             //revalidate();
//jScrollPane1.setPreferredSize(getParent().getSize());
                java.awt.EventQueue.invokeLater(() -> {
                    
                    loadAssets(lastFilter);
                    
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
    /**
     * Will recalculate grid, and remove all previews and regenerate if rows or
     * columns or filter has changed
     * 
     * @param filter only show previews containing filter
     */
    private void loadAssets(String filter) {
        if (assetManager == null) {
            return;
        }
        GridBagConstraints constraints = new GridBagConstraints();
        previewsPanel.setLayout(new GridBagLayout());
        Dimension size = previewsPanel.getSize();
        
        
        int rows = Math.max(size.height / sizeY, 1);
        
        final var textures = Arrays.stream(assetManager.getTextures()).filter(s -> filter.isEmpty() || s.toLowerCase().contains(filter)).collect(Collectors.toList());
        final var materials = Arrays.stream(assetManager.getMaterials()).filter(s -> filter.isEmpty() || s.toLowerCase().contains(filter)).collect(Collectors.toList());
        final var models = Arrays.stream(assetManager.getModels()).filter(s -> filter.isEmpty() || s.toLowerCase().contains(filter)).collect(Collectors.toList());
        int numAssets = textures.size() + materials.size() + models.size();
        int columns = Math.max(numAssets / rows, 1);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = sizeX;
        constraints.gridy = sizeY;
        
        
        
        Dimension newSize = new Dimension(columns * sizeX, rows * sizeY);
        System.out.println("old size " + size + " new size " + newSize + " " + columns + " " + rows);
        if (columns != lastGridColumns || rows != lastGridRows || !lastFilter.equals(filter)) {
            previewsPanel.removeAll();
            previewsPanel.setSize(newSize);
            previewsPanel.setPreferredSize(newSize);
            
            previewsPanel.setLayout(new GridBagLayout());
            
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
                preview.setPreviewImage(previewUtil.getOrCreateTexturePreview(item, imageSize));
            } else if (name.startsWith("Materials")) {
                preview = new MaterialPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateMaterialPreview(item, preview, imageSize));
            } else if (name.startsWith("Models")) {
                preview = new ModelPreview(this);
                preview.setPreviewImage(previewUtil.getOrCreateModelPreview(item, preview, imageSize));
            }
            if(preview == null) {
                continue;
            }
            preview.setMinimumSize(new Dimension(sizeX, sizeY));
            preview.setPreferredSize(new Dimension(sizeX, sizeY));
            if(assetManager.getAbsoluteAssetPath(item) != null) {
                preview.setEditable(true);
            }
            preview.setPreviewName(item);
            previewsPanel.add(preview, c);
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
        previewsPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        filterField = new javax.swing.JTextField();
        clearFilterButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        sizeSlider = new javax.swing.JSlider();

        setAlignmentX(0.0F);
        setAlignmentY(0.0F);
        setPreferredSize(new java.awt.Dimension(2000, 2000));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        projectLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.projectLabel.text")); // NOI18N
        projectLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        projectLabel.setAlignmentX(0.5F);
        projectLabel.setAlignmentY(0.0F);
        projectLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        projectLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                projectLabelMouseClicked(evt);
            }
        });
        add(projectLabel);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(150, 150));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(2000, 3000));

        previewsPanel.setPreferredSize(new java.awt.Dimension(200, 300));
        previewsPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(previewsPanel);

        add(jScrollPane1);

        jPanel2.setMaximumSize(new java.awt.Dimension(2147483647, 23));

        filterField.setText(org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.filterField.text")); // NOI18N
        filterField.setMinimumSize(new java.awt.Dimension(100, 23));
        filterField.setPreferredSize(new java.awt.Dimension(250, 23));
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
        filterField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterFieldActionPerformed(evt);
            }
        });
        filterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterFieldKeyPressed(evt);
            }
        });
        jPanel2.add(filterField);

        clearFilterButton.setIcon(Icons.clearFilter);
        org.openide.awt.Mnemonics.setLocalizedText(clearFilterButton, org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.clearFilterButton.text")); // NOI18N
        clearFilterButton.setMaximumSize(new java.awt.Dimension(23, 23));
        clearFilterButton.setMinimumSize(new java.awt.Dimension(23, 23));
        clearFilterButton.setPreferredSize(new java.awt.Dimension(23, 23));
        clearFilterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearFilterButtonMouseClicked(evt);
            }
        });
        jPanel2.add(clearFilterButton);
        jPanel2.add(filler1);

        sizeSlider.setMaximum(2);
        sizeSlider.setToolTipText(org.openide.util.NbBundle.getMessage(AssetBrowser.class, "AssetBrowser.sizeSlider.toolTipText")); // NOI18N
        sizeSlider.setName("sizeSlider"); // NOI18N
        sizeSlider.setPreferredSize(new java.awt.Dimension(100, 20));
        sizeSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizeSliderStateChanged(evt);
            }
        });
        jPanel2.add(sizeSlider);

        add(jPanel2);
    }// </editor-fold>//GEN-END:initComponents

    private void projectLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_projectLabelMouseClicked
        assetManager = ProjectSelection.getProjectAssetManager("Select project");
        projectName = assetManager.getProject().getProjectDirectory().getName();
        projectLabel.setText(projectName);
        previewUtil = new PreviewUtil(assetManager);      
        createAssetBrowserFolder(assetManager);
        loadAssets("");
    }//GEN-LAST:event_projectLabelMouseClicked

    private void filterFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_filterFieldFocusLost
        
    }//GEN-LAST:event_filterFieldFocusLost

    private void filterFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFieldKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_TAB || evt.getKeyCode() == KeyEvent.VK_ENTER) {
            previewsPanel.requestFocusInWindow();
            loadAssets(filterField.getText().toLowerCase());
        }
    }//GEN-LAST:event_filterFieldKeyPressed

    private void filterFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_filterFieldMouseClicked
        //filterField.setSelectionStart(0);
        //filterField.setSelectionEnd(filterField.getSelectedText().length());
    }//GEN-LAST:event_filterFieldMouseClicked

    private void filterFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_filterFieldActionPerformed

    private void sizeSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sizeSliderStateChanged
        final var value = sizeSlider.getValue(); 
        switch(value) {
            case 0:
                sizeX = (int) (170 * 0.5f);
                sizeY = (int) (180 * 0.5f);
                imageSize = (int) (150 * 0.5f); 
                break;
            case 1:
                sizeX = (int) (170 * 0.75f);
                sizeY = (int) (180 * 0.75f);
                imageSize = (int) (150 * 0.75f); 
                break;
            case 2:
                sizeX = 170;
                sizeY = 180;
                imageSize = 150;
                break;
        }
        if(value != oldSliderValue) {
            loadAssets(lastFilter);
            oldSliderValue = value;
        }
    }//GEN-LAST:event_sizeSliderStateChanged

    private void clearFilterButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearFilterButtonMouseClicked
        filterField.setText("");
        lastFilter = "";
        loadAssets("");
    }//GEN-LAST:event_clearFilterButtonMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JTextField filterField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel previewsPanel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JSlider sizeSlider;
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
