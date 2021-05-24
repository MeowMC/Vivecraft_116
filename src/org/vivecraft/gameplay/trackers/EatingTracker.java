package org.vivecraft.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.gameplay.OpenVRPlayer;
import org.vivecraft.provider.MCOpenVR;

import java.util.Random;

/**
 * Created by Hendrik on 02-Aug-16.
 */
public class EatingTracker extends Tracker {
    public boolean[] eating = new boolean[2];
    float mouthtoEyeDistance = 0.0f;
    float threshold = 0.25f;
    int eattime = 2100;
    long eatStart;
    private Random r = new Random();

    public EatingTracker(Minecraft mc) {
        super(mc);
    }

    public boolean isEating() {
        return eating[0] || eating[1];
    }

    public boolean isActive(ClientPlayerEntity p) {
        if (Minecraft.getInstance().vrSettings.seated)
            return false;
        if (p == null) return false;
        if (mc.playerController == null) return false;
        if (!p.isAlive()) return false;
        if (p.isSleeping()) return false;
        if (p.getHeldItemMainhand() != null) {
            UseAction action = p.getHeldItemMainhand().getUseAction();
            if (action == UseAction.EAT || action == UseAction.DRINK) return true;
        }
        if (p.getHeldItemOffhand() != null) {
            UseAction action = p.getHeldItemOffhand().getUseAction();
            return action == UseAction.EAT || action == UseAction.DRINK;
        }
        return false;
    }

    @Override
    public void reset(ClientPlayerEntity player) {
        eating[0] = false;
        eating[1] = false;
    }

    public void doProcess(ClientPlayerEntity player) {

        OpenVRPlayer provider = mc.vrPlayer;

        Vector3d hmdPos = provider.vrdata_room_pre.hmd.getPosition();
        Vector3d mouthPos = provider.vrdata_room_pre.getController(0).getCustomVector(new Vector3d(0, -mouthtoEyeDistance, 0)).add(hmdPos);

        for (int c = 0; c < 2; c++) {

            Vector3d controllerPos = MCOpenVR.controllerHistory[c].averagePosition(0.333).add(provider.vrdata_room_pre.getController(c).getCustomVector(new Vector3d(0, 0, -0.1)));
            controllerPos = controllerPos.add(mc.vrPlayer.vrdata_room_pre.getController(c).getDirection().scale(0.1));

            if (mouthPos.distanceTo(controllerPos) < threshold) {
                ItemStack is = c == 0 ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
                if (is == ItemStack.EMPTY) continue;

                int crunchiness = 0;
                if (is.getUseAction() == UseAction.DRINK) { //thats how liquid works.
                    if (provider.vrdata_room_pre.getController(c).getCustomVector(new Vector3d(0, 1, 0)).y > 0)
                        continue;
                }
                if (is.getUseAction() == UseAction.EAT) {
                    crunchiness = 2;
                } else {
                    continue;
                }

                if (!eating[c]) {
                    Minecraft.getInstance().physicalGuiManager.preClickAction();
                    if (mc.playerController.processRightClick(player, player.world, c == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND).isSuccessOrConsume()) {
                        mc.gameRenderer.itemRenderer.resetEquippedProgress(c == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
                        eating[c] = true;
                        eatStart = Util.milliTime();
                    }
                }

                if (eating[c]) {
                    long t = player.getItemInUseCount();
                    if (t > 0)
                        if (t % 5 <= crunchiness)
                            MCOpenVR.triggerHapticPulse(c, 700);
                }

                if (Util.milliTime() - eatStart > eattime)
                    eating[c] = false;

            } else {
                eating[c] = false;
            }
        }
    }
}
