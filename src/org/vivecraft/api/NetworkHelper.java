package org.vivecraft.api;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.gameplay.OpenVRPlayer;
import org.vivecraft.render.PlayerModelController;
import org.vivecraft.settings.AutoCalibration;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.lwjgl.Matrix4f;
import org.vivecraft.utils.math.Quaternion;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkHelper {

    public final static ResourceLocation channel = new ResourceLocation("vivecraft:data");
    public static Map<UUID, ServerVivePlayer> vivePlayers = new HashMap<UUID, ServerVivePlayer>();
    public static boolean displayedChatMessage = false;
    public static boolean serverWantsData = false;
    public static boolean serverAllowsClimbey = false;
    public static boolean serverSupportsDirectTeleport = false;
    public static boolean serverAllowsCrawling = false;
    private static float worldScallast = 0;
    private static float heightlast = 0;
    private static float capturedYaw, capturedPitch;
    private static boolean overrideActive;

    public static CCustomPayloadPacket getVivecraftClientPacket(PacketDiscriminators command, byte[] payload) {
        PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
        pb.writeByte(command.ordinal());
        pb.writeBytes(payload);
        return (new CCustomPayloadPacket(channel, pb));
    }

    public static SCustomPayloadPlayPacket getVivecraftServerPacket(PacketDiscriminators command, byte[] payload) {
        PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
        pb.writeByte(command.ordinal());
        pb.writeBytes(payload);
        return (new SCustomPayloadPlayPacket(channel, pb));
    }

    public static SCustomPayloadPlayPacket getVivecraftServerPacket(PacketDiscriminators command, String payload) {
        PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
        pb.writeByte(command.ordinal());
        pb.writeString(payload);
        return (new SCustomPayloadPlayPacket(channel, pb));
    }

    public static void resetServerSettings() {
        worldScallast = 0;
        heightlast = 0;
        serverAllowsClimbey = false;
        serverWantsData = false;
        serverSupportsDirectTeleport = false;
        serverAllowsCrawling = false;
        Minecraft.getInstance().vrSettings.overrides.resetAll();
    }

    public static void sendVersionInfo() {
        byte[] version = Minecraft.getInstance().minecriftVerString.getBytes(Charsets.UTF_8);
        String s = NetworkHelper.channel.toString();
        PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
        pb.writeBytes(s.getBytes());
        Minecraft.getInstance().getConnection().sendPacket(new CCustomPayloadPacket(new ResourceLocation("minecraft:register"), pb));
        Minecraft.getInstance().getConnection().sendPacket(NetworkHelper.getVivecraftClientPacket(PacketDiscriminators.VERSION, version));
        Minecraft.getInstance().vrPlayer.teleportWarningTimer = 20 * 10;
    }

    public static void sendVRPlayerPositions(OpenVRPlayer player) {
        if (!serverWantsData) return;
        if (Minecraft.getInstance().getConnection() == null) return;
        float worldScale = Minecraft.getInstance().vrPlayer.vrdata_world_post.worldScale;

        if (worldScale != worldScallast) {
            ByteBuf payload = Unpooled.buffer();
            payload.writeFloat(worldScale);
            byte[] out = new byte[payload.readableBytes()];
            payload.readBytes(out);
            CCustomPayloadPacket pack = getVivecraftClientPacket(PacketDiscriminators.WORLDSCALE, out);
            Minecraft.getInstance().getConnection().sendPacket(pack);

            worldScallast = worldScale;
        }

        float userheight = AutoCalibration.getPlayerHeight();

        if (userheight != heightlast) {
            ByteBuf payload = Unpooled.buffer();
            payload.writeFloat(userheight / AutoCalibration.defaultHeight);
            byte[] out = new byte[payload.readableBytes()];
            payload.readBytes(out);
            CCustomPayloadPacket pack = getVivecraftClientPacket(PacketDiscriminators.HEIGHT, out);
            Minecraft.getInstance().getConnection().sendPacket(pack);

            heightlast = userheight;
        }

        byte[] a = null, b = null, c = null;
        {
            FloatBuffer buffer = player.vrdata_world_post.hmd.getMatrix().toFloatBuffer();
            buffer.rewind();
            Matrix4f matrix = new Matrix4f();
            matrix.load(buffer);

            Vector3d headPosition = player.vrdata_world_post.getHeadPivot().subtract(Minecraft.getInstance().player.getPositionVec());
            Quaternion headRotation = new Quaternion(matrix);

            ByteBuf payload = Unpooled.buffer();
            payload.writeBoolean(Minecraft.getInstance().vrSettings.seated);
            payload.writeFloat((float) headPosition.x);
            payload.writeFloat((float) headPosition.y);
            payload.writeFloat((float) headPosition.z);
            payload.writeFloat(headRotation.w);
            payload.writeFloat(headRotation.x);
            payload.writeFloat(headRotation.y);
            payload.writeFloat(headRotation.z);
            byte[] out = new byte[payload.readableBytes()];
            payload.readBytes(out);
            a = out;
            CCustomPayloadPacket pack = getVivecraftClientPacket(PacketDiscriminators.HEADDATA, out);
            Minecraft.getInstance().getConnection().sendPacket(pack);

        }

        for (int i = 0; i < 2; i++) {
            Vector3d controllerPosition = player.vrdata_world_post.getController(i).getPosition().subtract(Minecraft.getInstance().player.getPositionVec());
            FloatBuffer buffer = player.vrdata_world_post.getController(i).getMatrix().toFloatBuffer();
            buffer.rewind();
            Matrix4f matrix = new Matrix4f();
            matrix.load(buffer);
            Quaternion controllerRotation = new Quaternion(matrix);
            ByteBuf payload = Unpooled.buffer();
            payload.writeBoolean(Minecraft.getInstance().vrSettings.vrReverseHands);
            payload.writeFloat((float) controllerPosition.x);
            payload.writeFloat((float) controllerPosition.y);
            payload.writeFloat((float) controllerPosition.z);
            payload.writeFloat(controllerRotation.w);
            payload.writeFloat(controllerRotation.x);
            payload.writeFloat(controllerRotation.y);
            payload.writeFloat(controllerRotation.z);
            byte[] out = new byte[payload.readableBytes()];
            if (i == 0) b = out;
            else c = out;
            payload.readBytes(out);
            CCustomPayloadPacket pack = getVivecraftClientPacket(i == 0 ? PacketDiscriminators.CONTROLLER0DATA : PacketDiscriminators.CONTROLLER1DATA, out);
            Minecraft.getInstance().getConnection().sendPacket(pack);
        }

        PlayerModelController.getInstance().Update(Minecraft.getInstance().player.getGameProfile().getId(), a, b, c, worldScale, userheight / AutoCalibration.defaultHeight, true);

    }

    public static boolean isVive(ServerPlayerEntity p) {
        if (p == null) return false;
        if (vivePlayers.containsKey(p.getGameProfile().getId())) {
            return vivePlayers.get(p.getGameProfile().getId()).isVR();
        }
        return false;
    }

    public static void sendPosData(ServerPlayerEntity from) {

        ServerVivePlayer v = vivePlayers.get(from.getUniqueID());
        if (v == null) return;
        if (v.player == null || v.player.hasDisconnected()) {
            vivePlayers.remove(from.getUniqueID());
            return;
        }
        if (v.isVR() == false) return;

        for (ServerVivePlayer sendTo : vivePlayers.values()) {

            if (sendTo == null || sendTo.player == null || sendTo.player.hasDisconnected())
                continue; // dunno y but just in case.

            if (v == sendTo || v.player.getEntityWorld() != sendTo.player.getEntityWorld() || v.hmdData == null || v.controller0data == null || v.controller1data == null) {
                continue;
            }

            double d = sendTo.player.getPositionVec().squareDistanceTo(v.player.getPositionVec());

            if (d < 256 * 256) {
                SCustomPayloadPlayPacket pack = getVivecraftServerPacket(PacketDiscriminators.UBERPACKET, v.getUberPacket());
                sendTo.player.connection.sendPacket(pack);
            }
        }
    }

    public static boolean isLimitedSurvivalTeleport() {
        return Minecraft.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.LIMIT_TELEPORT).getBoolean();
    }

    public static int getTeleportUpLimit() {
        return Minecraft.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_UP_LIMIT).getInt();
    }

    public static int getTeleportDownLimit() {
        return Minecraft.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_DOWN_LIMIT).getInt();
    }

    public static int getTeleportHorizLimit() {
        return Minecraft.getInstance().vrSettings.overrides.getSetting(VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT).getInt();
    }

    public static void sendActiveHand(byte c) {
        if (!serverWantsData) return;
        CCustomPayloadPacket pack = NetworkHelper.getVivecraftClientPacket(PacketDiscriminators.ACTIVEHAND, new byte[]{c});
        if (Minecraft.getInstance().getConnection() != null)
            Minecraft.getInstance().getConnection().sendPacket(pack);
    }

    public static void overridePose(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            ServerVivePlayer vp = vivePlayers.get(player.getGameProfile().getId());
            if (vp != null && vp.isVR() && vp.crawling)
                player.setPose(Pose.SWIMMING);
        } else if (player instanceof ClientPlayerEntity) {
            if (Minecraft.getInstance().crawlTracker.crawling)
                player.setPose(Pose.SWIMMING);
        }
    }

    public static void overrideLook(PlayerEntity player, Vector3d view) {
        if (serverWantsData) return; // shouldn't be needed, don't tease the anti-cheat.
        capturedPitch = player.rotationPitch;
        capturedYaw = player.rotationYaw;
        float pitch = (float) Math.toDegrees(Math.asin(-view.y / view.length()));
        float yaw = (float) Math.toDegrees(Math.atan2(-view.x, view.z));
        ((ClientPlayerEntity) player).connection.sendPacket(new CPlayerPacket.RotationPacket(yaw, pitch, player.isOnGround()));
        overrideActive = true;
    }

    public static void restoreLook(PlayerEntity player) {
        if (serverWantsData) return; // shouldn't be needed, don't tease the anti-cheat.
        if (!overrideActive) return; //wat
        ((ClientPlayerEntity) player).connection.sendPacket(new CPlayerPacket.RotationPacket(capturedYaw, capturedPitch, player.isOnGround()));
        overrideActive = false;
    }

    public enum PacketDiscriminators {
        VERSION,
        REQUESTDATA,
        HEADDATA,
        CONTROLLER0DATA,
        CONTROLLER1DATA,
        WORLDSCALE,
        DRAW,
        MOVEMODE,
        UBERPACKET,
        TELEPORT,
        CLIMBING,
        SETTING_OVERRIDE,
        HEIGHT,
        ACTIVEHAND,
        CRAWL
    }
}
