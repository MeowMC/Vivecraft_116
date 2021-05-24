package org.vivecraft.gui.settings;

import net.minecraft.client.gui.screen.Screen;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionEntry;
import org.vivecraft.settings.VRSettings;

public class GuiMenuWorldSettings extends GuiVROptionsBase {
    private VROptionEntry[] miscSettings = new VROptionEntry[]
            {
                    new VROptionEntry(VRSettings.VrOptions.MENU_WORLD_SELECTION),
                    new VROptionEntry("vivecraft.gui.menuworld.refresh", (button, mousePos) -> {
                        if (minecraft.menuWorldRenderer.getWorld() != null) {
                            try {
                                minecraft.menuWorldRenderer.destroy();
                                minecraft.menuWorldRenderer.prepare();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }),
                    new VROptionEntry(VRSettings.VrOptions.DUMMY),
                    new VROptionEntry("vivecraft.gui.menuworld.loadnew", (button, mousePos) -> {
                        try {
                            if (minecraft.menuWorldRenderer.isReady())
                                minecraft.menuWorldRenderer.destroy();
                            minecraft.menuWorldRenderer.init();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }),
            };

    public GuiMenuWorldSettings(Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        vrTitle = "vivecraft.options.screen.menuworld";

        super.init(miscSettings, true);

        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        VRSettings vr = minecraft.vrSettings;
        vr.menuWorldSelection = VRSettings.MENU_WORLD_BOTH;
    }
}
