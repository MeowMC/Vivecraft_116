package org.vivecraft.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Pose;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.provider.MCOpenVR;

public class CrawlTracker extends Tracker {
    public boolean crawling;
    public boolean crawlsteresis;
    private boolean wasCrawling;

    public CrawlTracker(Minecraft mc) {
        super(mc);
    }

    @Override
    public boolean isActive(ClientPlayerEntity player) {
        if (mc.vrSettings.seated) return false;
        if (!mc.vrSettings.vrAllowCrawling) return false;
        if (!NetworkHelper.serverAllowsCrawling) return false;
        if (!player.isAlive()) return false;
        if (player.isSpectator()) return false;
        if (player.isSleeping()) return false;
        return !player.isPassenger();
    }

    @Override
    public void reset(ClientPlayerEntity player) {
        crawling = false;
        crawlsteresis = false;
        updateState(player);
    }

    @Override
    public void doProcess(ClientPlayerEntity player) {
        crawling = MCOpenVR.hmdPivotHistory.averagePosition(0.2f).y * mc.vrPlayer.worldScale + 0.1f < mc.vrSettings.crawlThreshold;
        updateState(player);
    }

    private void updateState(ClientPlayerEntity player) {
        if (crawling != wasCrawling) {
            if (crawling) {
                player.setPose(Pose.SWIMMING);
                crawlsteresis = true;
            }

            if (NetworkHelper.serverAllowsCrawling) {
                CCustomPayloadPacket pack = NetworkHelper.getVivecraftClientPacket(NetworkHelper.PacketDiscriminators.CRAWL, new byte[]{crawling ? (byte) 1 : (byte) 0});
                if (mc.getConnection() != null)
                    mc.getConnection().sendPacket(pack);
            }

            wasCrawling = crawling;
        }

        if (!crawling && player.getPose() != Pose.SWIMMING)
            crawlsteresis = false;
    }
}
