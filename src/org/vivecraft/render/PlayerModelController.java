package org.vivecraft.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.vivecraft.api.VRData;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Hendrik on 07-Aug-16.
 */
public class PlayerModelController {
    static PlayerModelController instance;
    private final Minecraft mc;
    public boolean debug = false;
    private Map<UUID, RotInfo> vivePlayers = new HashMap<UUID, RotInfo>();
    private Map<UUID, RotInfo> vivePlayersLast = new HashMap<UUID, RotInfo>();
    private Map<UUID, RotInfo> vivePlayersReceived = Collections.synchronizedMap(new HashMap<UUID, RotInfo>());
    private Map<UUID, Integer> donors = new HashMap<UUID, Integer>();
    private Random rand = new Random();

    private PlayerModelController() {
        this.mc = Minecraft.getInstance();
    }

    public static PlayerModelController getInstance() {
        if (instance == null)
            instance = new PlayerModelController();
        return instance;
    }

    public static RotInfo getMainPlayerRotInfo(VRData data) {
        RotInfo rotInfo = new RotInfo();

        Quaternion quatLeft = new Quaternion(data.getController(1).getMatrix());
        Quaternion quatRight = new Quaternion(data.getController(0).getMatrix());
        Quaternion quatHmd = new Quaternion(data.hmd.getMatrix());

        rotInfo.headQuat = quatHmd;
        rotInfo.leftArmQuat = quatLeft;
        rotInfo.rightArmQuat = quatRight;
        rotInfo.seated = Minecraft.getInstance().vrSettings.seated;

        rotInfo.leftArmPos = data.getController(1).getPosition();
        rotInfo.rightArmPos = data.getController(0).getPosition();
        rotInfo.Headpos = data.hmd.getPosition();

        return rotInfo;
    }

    /**
     * @return the yaw of the direction the head is oriented in, no matter their pitch
     * Is not the same as the hmd yaw. Creates better results at extreme pitches
     * Simplified: Takes hmd-forward when looking at horizon, takes hmd-up when looking at ground.
     */
    public static float getFacingYaw(RotInfo rotInfo) {
        Vector3d facingVec = getOrientVec(rotInfo.headQuat);
        float yaw = (float) Math.toDegrees(Math.atan2(facingVec.x, facingVec.z));
        return yaw;
    }

    public static Vector3d getOrientVec(Quaternion quat) {
        Vector3d facingPlaneNormal = quat.multiply(new Vector3d(0, 0, -1))
                .crossProduct(quat.multiply(new Vector3d(0, 1, 0))).normalize();
        return new Vector3d(0, 1, 0).crossProduct(facingPlaneNormal).normalize();
    }

    public void Update(UUID uuid, byte[] hmddata, byte[] c0data, byte[] c1data, float worldscale, float heightscale, boolean localPlayer) {
        if (!localPlayer && mc.player.getUniqueID().equals(uuid))
            return; // Don't update local player from server packet

        Vector3d hmdpos = null, c0pos = null, c1pos = null;
        Quaternion hmdq = null, c0q = null, c1q = null;
        boolean seated = false, reverse = false;
        for (int i = 0; i <= 2; i++) {
            try {
                byte[] arr = null;
                switch (i) {
                    case 0:
                        arr = hmddata;
                        break;
                    case 1:
                        arr = c0data;
                        break;
                    case 2:
                        arr = c1data;
                        break;
                }

                ByteArrayInputStream by = new ByteArrayInputStream(arr);
                DataInputStream da = new DataInputStream(by);

                boolean bool = false;
                if (arr.length >= 29)
                    bool = da.readBoolean();

                float posx = da.readFloat();
                float posy = da.readFloat();
                float posz = da.readFloat();
                float rotw = da.readFloat();
                float rotx = da.readFloat();
                float roty = da.readFloat();
                float rotz = da.readFloat();

                da.close();

                switch (i) {
                    case 0:
                        if (bool) { //seated
                            seated = true;
                        }
                        hmdpos = new Vector3d(posx, posy, posz);
                        hmdq = new Quaternion(rotw, rotx, roty, rotz);
                        break;
                    case 1:
                        if (bool) { //reverse
                            reverse = true;
                        }
                        c0pos = new Vector3d(posx, posy, posz);
                        c0q = new Quaternion(rotw, rotx, roty, rotz);
                        break;
                    case 2:
                        if (bool) { //reverse
                            reverse = true;
                        }
                        c1pos = new Vector3d(posx, posy, posz);
                        c1q = new Quaternion(rotw, rotx, roty, rotz);
                        break;
                }

            } catch (IOException e) {

            }
        }

        Vector3 shoulderR = new Vector3(0, -0.0f, 0);
        Vector3 forward = new Vector3(0, 0, -1);
        Vector3 dir = hmdq.multiply(forward);
        Vector3 dir2 = c0q.multiply(forward);
        Vector3 dir3 = c1q.multiply(forward);

        RotInfo out = new RotInfo();
        out.reverse = reverse;
        out.seated = seated;
        if (donors.containsKey(uuid)) out.hmd = donors.get(uuid);
        out.leftArmRot = new Vector3d(dir3.getX(), dir3.getY(), dir3.getZ());
        out.rightArmRot = new Vector3d(dir2.getX(), dir2.getY(), dir2.getZ());
        out.headRot = new Vector3d(dir.getX(), dir.getY(), dir.getZ());
        out.Headpos = hmdpos;
        out.leftArmPos = c1pos;
        out.rightArmPos = c0pos;
        out.leftArmQuat = c1q;
        out.rightArmQuat = c0q;
        out.headQuat = hmdq;
        out.worldScale = worldscale;

        if (heightscale < 0.5f)
            heightscale = 0.5f;
        if (heightscale > 1.5f)
            heightscale = 1.5f;

        out.heightScale = heightscale;

        if (out.seated)
            out.heightScale = 1;

        vivePlayersReceived.put(uuid, out);

    }

    public void Update(UUID uuid, byte[] hmddata, byte[] c0data, byte[] c1data, float worldscale, float heightscale) {
        Update(uuid, hmddata, c0data, c1data, worldscale, heightscale, false);
    }

    public void tick() {
        for (Map.Entry<UUID, RotInfo> entry : vivePlayers.entrySet()) {
            vivePlayersLast.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<UUID, RotInfo> entry : vivePlayersReceived.entrySet()) {
            vivePlayers.put(entry.getKey(), entry.getValue());
        }

        World world = Minecraft.getInstance().world;
        if (world != null) {
            for (Iterator<UUID> it = vivePlayers.keySet().iterator(); it.hasNext(); ) {
                UUID uuid = it.next();
                if (world.getPlayerByUuid(uuid) == null) {
                    it.remove();
                    vivePlayersLast.remove(uuid);
                    vivePlayersReceived.remove(uuid);
                }
            }

            if (!mc.isGamePaused()) {
                for (PlayerEntity player : world.getPlayers()) {
                    if (donors.getOrDefault(player.getUniqueID(), 0) > 3) {
                        if (rand.nextInt(10) < 4) {
                            RotInfo rotInfo = vivePlayers.get(player.getUniqueID());
                            Vector3d derp = player.getLookVec();
                            if (rotInfo != null) {
                                derp = rotInfo.leftArmPos.subtract(rotInfo.rightArmPos).rotateYaw((float) -Math.PI / 2);
                                if (rotInfo.reverse)
                                    derp = derp.scale(-1);
                                else if (rotInfo.seated)
                                    derp = rotInfo.rightArmRot;

                                // Hands are at origin or something
                                if (derp.length() < 0.0001f)
                                    derp = rotInfo.headRot;
                            }
                            derp = derp.scale(0.1f);

                            // Use hmd pos for self so we don't have butt sparkles in face
                            Vector3d pos = rotInfo != null && player == mc.player ? rotInfo.Headpos : player.getEyePosition(1);
                            Particle particle = mc.particles.addParticle(ParticleTypes.FIREWORK,
                                    pos.x + (player.isSneaking() ? -derp.x * 3 : 0) + ((double) this.rand.nextFloat() - 0.5D) * .02f,
                                    pos.y - (player.isSneaking() ? 1.0f : 0.8f) + ((double) this.rand.nextFloat() - 0.5D) * .02f,
                                    pos.z + (player.isSneaking() ? -derp.z * 3 : 0) + ((double) this.rand.nextFloat() - 0.5D) * .02f,
                                    -derp.x + ((double) this.rand.nextFloat() - 0.5D) * .01f, ((double) this.rand.nextFloat() - .05f) * .05f, -derp.z + ((double) this.rand.nextFloat() - 0.5D) * .01f
                            );
                            if (particle != null)
                                particle.setColor(0.5F + rand.nextFloat() / 2, 0.5F + rand.nextFloat() / 2, 0.5F + rand.nextFloat() / 2);
                        }
                    }
                }
            }
        }
    }

//	/**
//	 * gets the {@link RotInfo} for both SinglePlayer and Multiplayer {@link PlayerEntity}s
//	 * */
//	public RotInfo getRotationFromEntity(PlayerEntity player){
//		UUID playerId = player.getUniqueID();
//		if (mc.player.getUniqueID().equals(playerId)) {
//			VRData data=Minecraft.getInstance().vrPlayer.vrdata_world_render;
//			return getMainPlayerRotInfo(data);		
//		} else {
//			return PlayerModelController.getInstance().getRotationsForPlayer(playerId);
//		}
//	}

    public void setHMD(UUID uuid, int level) {
        donors.put(uuid, level);
    }

    public boolean HMDCHecked(UUID uuid) {
        return donors.containsKey(uuid);
    }

    public RotInfo getRotationsForPlayer(UUID uuid) {
        if (debug) uuid = mc.player.getUniqueID();
        RotInfo rot = vivePlayers.get(uuid);
        if (rot != null && vivePlayersLast.containsKey(uuid)) {
            RotInfo rotLast = vivePlayersLast.get(uuid);
            RotInfo rotLerp = new RotInfo();
            float pt = Minecraft.getInstance().getRenderPartialTicks();
            rotLerp.reverse = rot.reverse;
            rotLerp.seated = rot.seated;
            rotLerp.hmd = rot.hmd;
            rotLerp.leftArmPos = Utils.vecLerp(rotLast.leftArmPos, rot.leftArmPos, pt);
            rotLerp.rightArmPos = Utils.vecLerp(rotLast.rightArmPos, rot.rightArmPos, pt);
            rotLerp.Headpos = Utils.vecLerp(rotLast.Headpos, rot.Headpos, pt);
            rotLerp.leftArmQuat = rot.leftArmQuat;//Utils.slerp(rotLast.leftArmQuat, rot.leftArmQuat, pt);
            rotLerp.rightArmQuat = rot.rightArmQuat;//Utils.slerp(rotLast.rightArmQuat, rot.rightArmQuat, pt);
            rotLerp.headQuat = rot.headQuat;//Utils.slerp(rotLast.headQuat, rot.headQuat, pt);
            Vector3 forward = new Vector3(0, 0, -1);
            rotLerp.leftArmRot = Utils.vecLerp(rotLast.leftArmRot, Utils.convertToVector3d(rotLerp.leftArmQuat.multiply(forward)), pt);
            rotLerp.rightArmRot = Utils.vecLerp(rotLast.rightArmRot, Utils.convertToVector3d(rotLerp.rightArmQuat.multiply(forward)), pt);
            rotLerp.headRot = Utils.vecLerp(rotLast.headRot, Utils.convertToVector3d(rotLerp.headQuat.multiply(forward)), pt);
            rotLerp.heightScale = rot.heightScale;
            rotLerp.worldScale = rot.worldScale;
            return rotLerp;
        }
        return rot;
    }

    public boolean isTracked(UUID uuid) {
        debug = false;
        if (debug) return true;
        return vivePlayers.containsKey(uuid);
    }

    public static class RotInfo {
        public boolean seated, reverse;
        public int hmd = 0;
        public Quaternion leftArmQuat, rightArmQuat, headQuat;
        public Vector3d leftArmRot, rightArmRot, headRot;
        public Vector3d leftArmPos, rightArmPos, Headpos;
        public float worldScale;
        public float heightScale;
        public RotInfo() {

        }

        public double getBodyYawRadians() {
            Vector3d diff = leftArmPos.subtract(rightArmPos).rotateYaw((float) -Math.PI / 2);
            if (reverse) diff = diff.scale(-1);
            if (seated) diff = rightArmRot;
            Vector3d avg = Utils.vecLerp(diff, headRot, 0.5);
            double ltor = Math.atan2(-avg.x, avg.z);
            return ltor;
        }
    }
}
