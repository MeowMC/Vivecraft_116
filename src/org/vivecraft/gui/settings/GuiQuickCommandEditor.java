/**
 * Copyright 2013 Mark Browning, StellaArtois
 * Licensed under the LGPL 3.0 or later (See LICENSE.md for details)
 */
package org.vivecraft.gui.settings;

import net.minecraft.client.gui.screen.Screen;
import org.vivecraft.gui.framework.GuiVROptionsBase;


public class GuiQuickCommandEditor extends GuiVROptionsBase {

    private GuiQuickCommandsList guiList;

    public GuiQuickCommandEditor(Screen par1Screen) {
        super(par1Screen);
    }

    @Override
    public void init() {
        vrTitle = "vivecraft.options.screen.quickcommands";
        this.guiList = new GuiQuickCommandsList(this, minecraft);
        super.init();
        super.addDefaultButtons();
        this.visibleList = guiList;
    }

    @Override
    protected void loadDefaults() {
        minecraft.vrSettings.vrQuickCommands = minecraft.vrSettings.getQuickCommandsDefaults();
    }

    @Override
    protected boolean onDoneClicked() {
        for (int i = 0; i < 12; i++) {
            String c = this.guiList.getEventListeners().get(i).txt.getText();
            minecraft.vrSettings.vrQuickCommands[i] = c;
        }
        return super.onDoneClicked();
    }

}
