/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.gde.textureeditor;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.OpenSupport;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableTopComponent;

@NbBundle.Messages("CTL_OpenTexture=Edit Texture")
@ActionID(id = "com.jme3.gde.textureeditor.OpenTexture", category = "Window")
@ActionReferences({
    @ActionReference(path = "Loaders/image/png/Actions", position = 0),
    @ActionReference(path = "Loaders/image/jpeg/Actions", position = 0),
    @ActionReference(path = "Loaders/image/image/x-jmetexture/Actions",     position = 0),
    @ActionReference(path = "Loaders/text/plain/Actions",       position = 0),
})
public final class OpenTexture extends OpenSupport implements OpenCookie, CloseCookie {

    public OpenTexture(JmeTextureDataObject.Entry entry) {
        super(entry);
    }
    
    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        DataObject dobj = entry.getDataObject();
        FileObject file = dobj.getPrimaryFile();
        ImageEditorTopComponent display = new ImageEditorTopComponent(file);
        return display;
    }

}
