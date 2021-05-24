package org.vivecraft.gui.settings;

import net.minecraft.client.gui.screen.Screen;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionEntry;
import org.vivecraft.settings.VRSettings;

public class GuiStandingSettings extends GuiVROptionsBase {
    private VROptionEntry[] locomotionSettings = new VROptionEntry[]
            {
                    new VROptionEntry(VRSettings.VrOptions.WALK_UP_BLOCKS),
                    new VROptionEntry(VRSettings.VrOptions.VEHICLE_ROTATION),
                    new VROptionEntry(VRSettings.VrOptions.WALK_MULTIPLIER),
                    new VROptionEntry(VRSettings.VrOptions.WORLD_ROTATION_INCREMENT),
                    new VROptionEntry(VRSettings.VrOptions.BCB_ON),
                    new VROptionEntry(VRSettings.VrOptions.ALLOW_STANDING_ORIGIN_OFFSET),
                    new VROptionEntry(VRSettings.VrOptions.FORCE_STANDING_FREE_MOVE, true),
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

    public GuiStandingSettings(Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        vrTitle = "vivecraft.options.screen.standing";

        super.init(locomotionSettings, true);

        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        VRSettings vr = minecraft.vrSettings;
        vr.vrShowBlueCircleBuddy = true;
        vr.walkMultiplier = 1;
        vr.vehicleRotation = true;
        vr.walkUpBlocks = true;
        vr.vrWorldRotationIncrement = 45f;
        vr.allowStandingOriginOffset = false;
        vr.forceStandingFreeMove = false;
    }
}
