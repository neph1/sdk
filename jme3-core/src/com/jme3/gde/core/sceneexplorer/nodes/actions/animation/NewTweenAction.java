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
package com.jme3.gde.core.sceneexplorer.nodes.actions.animation;

import com.jme3.anim.AnimClip;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.tween.Tweens;
import com.jme3.gde.core.sceneexplorer.nodes.animation.JmeAnimComposer;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author rickard
 */
public class NewTweenAction extends AbstractAction {

    private final JmeAnimComposer animComposer;

    public NewTweenAction(JmeAnimComposer animComposer) {
        super("Make Tween");
        this.animComposer = animComposer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Stream<AnimClip> clips = animComposer.getLookup().lookup(AnimComposer.class)
                .getAnimClips().stream();
        final String[] animNames = clips
                .map(AnimClip::getName)
                .toArray(String[]::new);
        JList list = new JList();
        list.setListData(animNames);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane jscrollpane = new JScrollPane();
        jscrollpane.setViewportView(list);
        JOptionPane.showMessageDialog(null, jscrollpane, "Select AnimClips", JOptionPane.PLAIN_MESSAGE);
        
        System.out.println("Selected " + list.getSelectedValuesList().size());
        final List<AnimClip> selected = clips.takeWhile((AnimClip clip) -> {
            for(Object name : list.getSelectedValuesList()) {
                if (clip.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }
    
}
