/*
 *  Copyright (c) 2009-2024 jMonkeyEngine
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
package com.jme3.gde.core.appstates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class NewAppStateVisualPanel1 extends JPanel {

    private Project proj;

    /**
     * Creates new form NewCustomControlVisualPanel1
     */
    public NewAppStateVisualPanel1() {
        initComponents();
    }

    @Override
    public String getName() {
        return "Select AppState";
    }

    public String getClassName() {
        return jTextField1.getText();
    }

    private void scanControls() {
        List<String> sources = getSources();
        jList1.setListData(sources.toArray());
    }

    private List<String> getSources() {
        Project root = ProjectUtils.rootOf(proj);
        Set<Project> containedProjects = ProjectUtils.getContainedProjects(root, true);
        List<Project> projects = new ArrayList<>();
        projects.add(root);
        if (containedProjects != null) {
            projects.addAll(containedProjects);
        }
        if (projects.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        for (Project project : projects) {
            Sources sources = ProjectUtils.getSources(project);
            if (sources == null) {
                continue;
            }

            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if (groups == null) {
                continue;
            }

            for (SourceGroup sourceGroup : groups) {
                FileObject rootFolder = sourceGroup.getRootFolder();
                ClasspathInfo cpInfo = ClasspathInfo.create(
                        ClassPath.getClassPath(rootFolder, ClassPath.BOOT),
                        ClassPath.getClassPath(rootFolder, ClassPath.COMPILE),
                        ClassPath.getClassPath(rootFolder, ClassPath.SOURCE)
                );

                Set<SearchScope> set = EnumSet.of(ClassIndex.SearchScope.SOURCE);
                Set<ElementHandle<TypeElement>> types = cpInfo.getClassIndex().getDeclaredTypes("", NameKind.PREFIX, set);
                for (ElementHandle<TypeElement> elementHandle : types) {
                    JavaSource js = JavaSource.create(cpInfo);
                    try {
                        js.runUserActionTask((CompilationController control) -> {
                            control.toPhase(JavaSource.Phase.RESOLVED);
                            TypeElement elem = elementHandle.resolve(control);
                            if (elem != null && doesInheritFromAppState(elem, control.getTypes())) {
                                list.add(elem.getQualifiedName().toString());
                            }
                        }, false);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }

        return list;
    }

    /**
     * Checks recursively if a type inherits from or implements any
     * AppState-related class/interface.
     */
    private boolean doesInheritFromAppState(TypeElement type, Types typeUtils) {
        if (type == null) {
            return false;
        }

        // Check interfaces
        for (TypeMirror iface : type.getInterfaces()) {
            if (isAppStateType(iface)) {
                return true;
            }
            if (doesInheritFromAppState((TypeElement) typeUtils.asElement(iface), typeUtils)) {
                return true;
            }
        }

        // Check superclass
        TypeMirror superClass = type.getSuperclass();
        if (superClass != null && superClass.getKind() != TypeKind.NONE) {
            if (isAppStateType(superClass)) {
                return true;
            }
            return doesInheritFromAppState((TypeElement) typeUtils.asElement(superClass), typeUtils);
        }

        return false;
    }

    /**
     * Determines if a TypeMirror corresponds to an AppState-related type.
     */
    private boolean isAppStateType(TypeMirror typeMirror) {
        if (typeMirror == null) {
            return false;
        }
        String className = typeMirror.toString();
        return "com.jme3.app.state.AppState".equals(className)
                || "com.jme3.app.state.AbstractAppState".equals(className)
                || "com.jme3.app.state.BaseAppState".equals(className);
    }

    public void load(Project proj) {
        this.proj = proj;
        scanControls();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                updateClassName(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewAppStateVisualPanel1.class, "NewAppStateVisualPanel1.jLabel1.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(NewAppStateVisualPanel1.class, "NewAppStateVisualPanel1.jTextField1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                    .addComponent(jTextField1)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateClassName(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_updateClassName
        Object obj = jList1.getSelectedValue();
        if (obj != null) {
            jTextField1.setText(obj + "");
        }
    }//GEN-LAST:event_updateClassName

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
