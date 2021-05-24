package org.vivecraft.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.vivecraft.provider.MCOpenVR;
import org.vivecraft.settings.AutoCalibration;

public class SneakTracker extends Tracker {
    public boolean sneakOverride = false;
    public int sneakCounter = 0;

    public SneakTracker(Minecraft mc) {
        super(mc);
    }

    public boolean isActive(ClientPlayerEntity p) {
        if (Minecraft.getInstance().vrSettings.seated)
            return false;
        if (!Minecraft.getInstance().vrPlayer.getFreeMove() && !Minecraft.getInstance().vrSettings.simulateFalling)
            return false;
        if (!Minecraft.getInstance().vrSettings.realisticSneakEnabled)
            return false;
        if (mc.playerController == null) return false;
        if (p == null || !p.isAlive() || !p.isOnGround())
            return false;
        return !p.isPassenger();
    }

    @Override
    public void reset(ClientPlayerEntity player) {
        sneakOverride = false;
    }

    public void doProcess(ClientPlayerEntity player) {

        if (!mc.isGamePaused()) {
            if (mc.sneakTracker.sneakCounter > 0)
                mc.sneakTracker.sneakCounter--;
        }

        sneakOverride = (AutoCalibration.getPlayerHeight() - MCOpenVR.hmdPivotHistory.latest().y) > mc.vrSettings.sneakThreshold;
    }

}
