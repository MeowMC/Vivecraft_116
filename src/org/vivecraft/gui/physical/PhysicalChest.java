package org.vivecraft.gui.physical;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.gui.physical.interactables.Interactable;
import org.vivecraft.gui.physical.interactables.PhysicalItemSlot;
import org.vivecraft.utils.math.Quaternion;
import org.vivecraft.utils.math.Vector3;

public class PhysicalChest extends PhysicalItemSlotGui {
    boolean wasOpen = false;
    boolean loadedDouble;
    double lidAngle = 0;
    double lastLidAngle = 0;
    int handOnLid = -1;
    int layer = 2;
    public PhysicalChest(BlockPos pos) {
        super(pos);
        openDistance = 0;
    }

    public boolean isDouble() {
        Vector3d posVec = getAnchorPos(0);
        Vector3d right = new Vector3d(-1, 0, 0);
        Quaternion rot = getAnchorRotation();
        BlockPos neighborPos = new BlockPos(posVec.add(rot.multiply(right)));

        BlockState neighborBlock = mc.world.getBlockState(neighborPos);
        return (neighborBlock.getBlock().equals(Blocks.CHEST) && blockState.getBlock().equals(Blocks.CHEST)) ||
                (neighborBlock.getBlock().equals(Blocks.TRAPPED_CHEST) && blockState.getBlock().equals(Blocks.TRAPPED_CHEST));
    }

    public double getLidHoldAngle(BlockPos pos, double partialTicks) {
        BlockState block = mc.world.getBlockState(pos);

        boolean isNeighbor = false;
        if (this.blockState.getBlock().equals(Blocks.CHEST) && block.getBlock().equals(Blocks.CHEST) ||
                this.blockState.getBlock().equals(Blocks.TRAPPED_CHEST) && block.getBlock().equals(Blocks.TRAPPED_CHEST)) {
//			Vector3d offset = new Vector3d(pos).subtract(new Vector3d(this.blockPos));
//			int x = Math.abs((int) Math.round(getAnchorRotation(partialTicks).inverse().multiply(offset).x));
//			isNeighbor = (x == 1);
        }

        if (isFullyClosed() || (!pos.equals(this.blockPos) && !isNeighbor))
            return -1;
        return lastLidAngle + (lidAngle - lastLidAngle) * partialTicks;
    }

    @Override
    public boolean isAlive() {
        if (!mc.world.getBlockState(blockPos).getBlock().equals(blockState.getBlock()))
            return false;
        return loadedDouble == isDouble();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        //reloadSlots();

        double[] lidDist = new double[2];

        Vector3d basePos = getAnchorPos();
        Quaternion rot = getAnchorRotation().multiply(new Quaternion(0, 180, 0)); // rotation facing you

        basePos = basePos.add(rot.multiply(new Vector3d(0, 0.1, -0.4)));


        if (isDouble()) {
            basePos = basePos.add(rot.multiply(new Vector3d(0.5, 0, 0)));
        }


        Quaternion lidRot = new Quaternion((float) (-lidAngle), 0, 0);

        Vector3d lidMiddle = basePos.add(rot.multiply(lidRot.multiply(new Vector3d(0, 0, 0.4))));

        for (int i = 0; i < 2; i++) {
            Vector3d handPos = mc.vrPlayer.vrdata_world_pre.getController(i).getPosition();

            //get the position relative to the lid
            Vector3d offsetLid = lidRot.inverse().multiply(rot.inverse().multiply(handPos.subtract(lidMiddle)));

            double xRange = isDouble() ? 1.0 : 0.5;
            double zRange = 0.5;

            if (Math.abs(offsetLid.x) < xRange && Math.abs(offsetLid.z) < zRange
                    && (Math.abs(offsetLid.y - 0.05) < 0.1 || (handOnLid == i && Math.abs(offsetLid.y) < 0.4))) {
                lidDist[i] = -offsetLid.y;
            } else {
                lidDist[i] = Double.MAX_VALUE;
            }
        }

        int handOnLid = lidDist[0] < lidDist[1] ? 0 : 1;

        if (lidDist[handOnLid] < 0.5) {


            if (!isOpen && !mc.physicalGuiManager.isIntercepting())
                tryOpenWindow();
            this.handOnLid = handOnLid;
            Vector3d handPos = mc.vrPlayer.vrdata_world_pre.getController(handOnLid).getPosition();
            Vector3d dir = handPos.subtract(basePos).normalize();

            dir = rot.inverse().multiply(dir);
            dir = new Vector3d(0, dir.y, dir.z);


            double newAngle = Quaternion.createFromToVector(new Vector3(0, 0, -1), new Vector3(dir)).toEuler().getPitch();

            newAngle = Math.max(Math.min(newAngle, 90), 0);
            lastLidAngle = lidAngle;
            lidAngle = newAngle;
        } else {
            this.handOnLid = -1;
            double velocity = lidAngle - lastLidAngle;
            double force = 1.0 * Math.abs((lidAngle - 45) / 45.0) + 0.5;

            if (isOpen && lidAngle > 45) {
                velocity += force;
            } else {
                velocity -= force;
            }
            lastLidAngle = lidAngle;
            lidAngle = Math.min(Math.max(0, lidAngle + velocity), 90);

            if (isOpen && lidAngle == 0) {
                close();
            }
        }


        if (isOpen) {
            if (isInRange()) {
                int mainhand = (mc.gameSettings.mainHand == HandSide.RIGHT) ? 0 : 1;
                Vector3d handPos = mc.vrPlayer.vrdata_world_pre.getController(mainhand).getPosition();
                Vector3d offset = getAnchorRotation().inverse().multiply(handPos.subtract(getAnchorPos()));
                double height = offset.y + 0.3;
                int layer = (int) (height / 0.4f * 2);
                layer = Math.min(Math.max(0, layer), 2);
                switchLayer(layer);
            } else {
                switchLayer(2);
            }
        }
    }

    @Override
    boolean isInRange() {
        for (int i = 0; i < 2; i++) {
            Vector3d handPos = mc.vrPlayer.vrdata_world_pre.getController(i).getPosition();
            Vector3d offset = getAnchorRotation().inverse().multiply(handPos.subtract(getAnchorPos()));
            if (isDouble()) {
                offset = offset.add(new Vector3d(0.5, 0, 0));
            }

            double rangeX = isDouble() ? 1 : 0.5;
            double rangeZ = 0.5;

            if (Math.abs(offset.x) < rangeX && Math.abs(offset.z) < rangeZ) {
                return true;
            }
        }
        return false;

    }

    int getSlotCount() {
        if (container == null)
            if (isDouble())
                return 54;
            else
                return 27;
        else
            return container.inventorySlots.get(0).inventory.getSizeInventory();
    }


    @Override
    public void render(double partialTicks) {
        getBlockOrientation(blockPos);
        super.render(partialTicks);
    }

    @Override
    void loadSlots() {
        loadedDouble = isDouble();
        boolean dummy = container == null;
        int slotCount = getSlotCount();

        int rows = slotCount / 9;

        this.interactables.clear();


        Vector3d anchor = new Vector3d(0.2, -0.375, 0.2);
        double spanX = isDouble() ? 0.8 : 0.6;
        double spanY = 0.6;
        double spanZ = 0.6;

        for (int row = 0; row < rows; row++) {
            int level = row / (rows / 3);
            int pack = row % (rows / 3);
            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 3; x++) {
                    double spacingX = spanX / 3;
                    double spacingY = spanY / 3;
                    double spacingZ = spanZ / 3;

                    int slotId = row * 9 + y * 3 + x;
                    PhysicalItemSlot slot = new PhysicalItemSlot(this, slotId);
                    if (!dummy)
                        slot.slot = container.inventorySlots.get(slotId);

                    slot.position = anchor.add(new Vector3d(-spacingX * x, spacingY * level, -spacingZ * y))
                            .add(new Vector3d(-pack * (spanX + 0.07), 0, 0));
                    slot.rotation = new Quaternion(90, 0, 0);
                    slot.fullBlockScaleMult = 1.9;
                    slot.scale = 0.19;
                    slot.opacity = 1;
                    interactables.add(slot);
                }
            }
        }
    }

    void switchLayer(int layer) {
        this.layer = layer;

        int slotCount = getSlotCount();

        for (Interactable inter : interactables) {
            if (inter instanceof PhysicalItemSlot) {
                PhysicalItemSlot slot = (PhysicalItemSlot) inter;
                int j = slot.slotId / 9 / (slotCount / 9 / 3);
                if (j > layer) {
                    slot.opacity = 0.1;
                } else {
                    slot.opacity = 1;
                }
            }
        }
    }

    @Override
    public boolean isFullyClosed() {
//		if(isOpen)
//			return false;
//
//		if(!wasOpen)
//			return true;
//
//		if(!(lidAngle==0 && lastLidAngle==0))
//			return false;
//
//		TileEntity te=mc.world.getTileEntity(blockPos);
//		if(te!=null && te instanceof ChestTileEntity){
//			ChestTileEntity teChest=(ChestTileEntity) te;
//			if(teChest.prevLidAngle == 0 && teChest.lidAngle == 0){
//				wasOpen=false;
//				return true;
//			}else
//				return false;
//		}
//		if(te!=null && te instanceof EnderChestTileEntity){
//			EnderChestTileEntity teChest=(EnderChestTileEntity) te;
//			if( teChest.prevLidAngle==0 && teChest.lidAngle==0){
//				wasOpen=false;
//				return true;
//			}else
//				return false;
//		}
        return super.isFullyClosed();
    }

    @Override
    public void open(Object payload) {
        if (payload instanceof IInventory) {
            IInventory inventory = (IInventory) payload;
//			container = new ChestContainer(mc.player.inventory, inventory, mc.player);		
            wasOpen = true;
            super.open(null);
        }

    }
}
