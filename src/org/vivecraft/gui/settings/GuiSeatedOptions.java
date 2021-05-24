package org.vivecraft.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionEntry;
import org.vivecraft.settings.VRSettings;

public class GuiSeatedOptions extends GuiVROptionsBase {
    private VROptionEntry[] seatedOptions = new VROptionEntry[]{
            new VROptionEntry(VRSettings.VrOptions.X_SENSITIVITY),
            new VROptionEntry(VRSettings.VrOptions.Y_SENSITIVITY),
            new VROptionEntry(VRSettings.VrOptions.KEYHOLE),
            new VROptionEntry(VRSettings.VrOptions.SEATED_HUD_XHAIR),
            new VROptionEntry(VRSettings.VrOptions.WALK_UP_BLOCKS),
            new VROptionEntry(VRSettings.VrOptions.WORLD_ROTATION_INCREMENT),
            new VROptionEntry(VRSettings.VrOptions.VEHICLE_ROTATION),
            new VROptionEntry(VRSettings.VrOptions.DUMMY),
            new VROptionEntry(VRSettings.VrOptions.SEATED_FREE_MOVE, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry("vivecraft.options.screen.teleport.button", (button, mousePos) -> {
                minecraft.displayGuiScreen(new GuiTeleportSettings(this));
                return true;
            }),
            new VROptionEntry("vivecraft.options.screen.freemove.button", (button, mousePos) -> {
                minecraft.displayGuiScreen(new GuiFreeMoveSettings(this));
                return true;
            })
    };

    public GuiSeatedOptions(Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        vrTitle = "vivecraft.options.screen.seated";


        super.init(seatedOptions, true);


        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        VRSettings vrSettings = Minecraft.getInstance().vrSettings;
        vrSettings.keyholeX = 15;
        vrSettings.xSensitivity = 1;
        vrSettings.ySensitivity = 1;
        vrSettings.seatedHudAltMode = false;
        vrSettings.vrWorldRotationIncrement = 45f;
        vrSettings.seatedFreeMove = false;
    }


}
