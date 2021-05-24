package org.vivecraft.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.gameplay.OpenVRPlayer;
import org.vivecraft.provider.MCOpenVR;


public class BackpackTracker extends Tracker {
    public boolean[] wasIn = new boolean[2];

    public int previousSlot = 0;
    private Vector3d down = new Vector3d(0, -1, 0);

    public BackpackTracker(Minecraft mc) {
        super(mc);
    }

    public boolean isActive(ClientPlayerEntity p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.vrSettings.seated) return false;
        if (!mc.vrSettings.backpackSwitching) return false;
        if (p == null) return false;
        if (mc.playerController == null) return false;
        if (!p.isAlive()) return false;
        if (p.isSleeping()) return false;
        return !mc.bowTracker.isDrawing;
    }

    public void doProcess(ClientPlayerEntity player) {
        OpenVRPlayer provider = mc.vrPlayer;

        Vector3d hmdPos = provider.vrdata_room_pre.getHeadRear();

        for (int c = 0; c < 2; c++) {
            Vector3d controllerPos = provider.vrdata_room_pre.getController(c).getPosition();//.add(provider.getCustomControllerVector(c, new Vec3(0, 0, -0.1)));
            Vector3d controllerDir = provider.vrdata_room_pre.getHand(c).getDirection();
            Vector3d hmddir = provider.vrdata_room_pre.hmd.getDirection();
            Vector3d delta = hmdPos.subtract(controllerPos);
            double dot = controllerDir.dotProduct(down);
            double dotDelta = delta.dotProduct(hmddir);

            boolean below = ((Math.abs(hmdPos.y - controllerPos.y)) < 0.25);
            boolean behind = (dotDelta > 0) && delta.length() > 0.05;
            boolean aimdown = (dot > .6);

            boolean infront = (dotDelta < 0) && delta.length() > 0.25;
            boolean aimup = (dot < 0);

            boolean zone = below && behind && aimdown;

            Minecraft mc = Minecraft.getInstance();
            if (zone) {
                if (!wasIn[c]) {
                    if (c == 0) { //mainhand
                        if ((mc.climbTracker.isGrabbingLadder() &&
                                mc.climbTracker.isClaws(mc.player.getHeldItemMainhand()))) {
                        } else {
                            if (player.inventory.currentItem != 0) {
                                previousSlot = player.inventory.currentItem;
                                player.inventory.currentItem = 0;
                            } else {
                                player.inventory.currentItem = previousSlot;
                                previousSlot = 0;
                            }
                        }
                    } else { //offhand
                        if ((mc.climbTracker.isGrabbingLadder() &&
                                mc.climbTracker.isClaws(mc.player.getHeldItemOffhand()))) {
                        } else {
                            if (mc.vrSettings.physicalGuiEnabled) {
                                mc.physicalGuiManager.toggleInventoryBag();
                            } else
                                player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                        }
                    }
                    MCOpenVR.triggerHapticPulse(c, 1500);
                    wasIn[c] = true;
                }
            } else {
                if (!infront && !aimup) {
                    //noop
                } else {
                    wasIn[c] = false;
                }
            }
        }
    }

}
