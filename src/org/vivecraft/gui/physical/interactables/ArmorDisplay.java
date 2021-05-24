package org.vivecraft.gui.physical.interactables;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.api.VRData;
import org.vivecraft.gui.physical.PhysicalInventory;
import org.vivecraft.gui.physical.PhysicalItemSlotGui;
import org.vivecraft.utils.math.Quaternion;

import java.util.ArrayList;

public class ArmorDisplay extends PhysicalItemSlotGui {
    //TODO better Model
    //TODO fix helmet in the way

    PhysicalInventory inventory;
    boolean armorMode;
    ArrayList<ArmorItemSlot> armorSlots = new ArrayList<>();

    public ArmorDisplay(PhysicalInventory inventory) {
        super(inventory.entity);
        this.inventory = inventory;
    }

    @Override
    public void open(Object payload) {
        this.container = inventory.container;
        loadSlots();
        isOpen = true;
    }

    @Override
    public void close() {
        isOpen = false;
        setArmorMode(false);
    }

    public void setArmorMode(boolean armorMode) {
        this.armorMode = armorMode;

        for (ArmorItemSlot slot : armorSlots) {
            slot.enabled = armorMode;
        }
        mc.vrSettings.tmpRenderSelf = armorMode;
    }

    @Override
    public void tryOpenWindow() {
    }

    @Override
    public void render(double partialTicks) {
        super.render(partialTicks);

		/*for(ArmorItemSlot slot: armorSlots){
			Debug debug=new Debug(slot.getAnchorPos(0),slot.getAnchorRotation(0));
			AxisAlignedBB bb=slot.getBoundingBox().offset(slot.position.x,slot.position.y,slot.position.z);
			debug.drawBoundingBox(bb, Color.blue);
		}*/
    }

    public void loadSlots() {
        armorSlots.clear();
        interactables.clear();

        if (!inventory.metaData.hasExtra || inventory.container == null)
            return;
        int offset = inventory.metaData.armorOffset;
        ArmorItemSlot helmet = new ArmorItemSlot(this, offset);
        helmet.feetBound = false;
        helmet.position = new Vector3d(0, 0, 0);
        helmet.slot = inventory.container.getSlot(helmet.slotId);
        armorSlots.add(helmet);

        ArmorItemSlot chest = new ArmorItemSlot(this, offset + 1);
        chest.position = new Vector3d(0, -0.4, 0);
        chest.feetBound = false;
        chest.slot = inventory.container.getSlot(chest.slotId);
        armorSlots.add(chest);

        ArmorItemSlot pants = new ArmorItemSlot(this, offset + 2);
        pants.slot = inventory.container.getSlot(pants.slotId);
        pants.feetBound = true;
        pants.position = new Vector3d(0, 0.7, 0);
        armorSlots.add(pants);

        ArmorItemSlot boots = new ArmorItemSlot(this, offset + 3);
        boots.position = new Vector3d(0, 0.1, 0);
        boots.feetBound = true;
        boots.slot = inventory.container.getSlot(boots.slotId);
        armorSlots.add(boots);

        for (ArmorItemSlot slot : armorSlots) {
            slot.enabled = armorMode;
        }
        interactables.addAll(armorSlots);
    }

    @Override
    public Vector3d getAnchorPos(double partialTicks) {
        return new Vector3d(mc.gameRenderer.rveX, mc.gameRenderer.rveY, mc.gameRenderer.rveZ);
    }

    @Override
    public Quaternion getAnchorRotation(double partialTicks) {
        return new Quaternion(0, -entity.rotationYaw, 0);
    }

    class ArmorItemSlot extends PhysicalItemSlot {
        public AxisAlignedBB boundingBox = super.getBoundingBox();
        public boolean feetBound;
        boolean placed;

        public ArmorItemSlot(PhysicalItemSlotGui gui, int slotId) {
            super(gui, slotId);
            preview = false;
        }

        @Override
        public ItemStack getDisplayedItem() {
            return ItemStack.EMPTY;
        }

        @Override
        public void touch() {
            if (!mc.physicalGuiManager.getVirtualHeldItem().isEmpty()) {
                super.click(0);
                placed = false;
            } else
                placed = true;
        }

        @Override
        public void untouch() {
            if (!placed && !slot.getStack().isEmpty())
                super.click(0);
        }

        @Override
        public void click(int button) {
            if (placed) {
                super.click(0);
            }
            placed = true;
        }

        @Override
        public boolean isEnabled() {
            if (!mc.physicalGuiManager.getVirtualHeldItem().isEmpty() &&
                    !slot.isItemValid(mc.physicalGuiManager.getVirtualHeldItem()))
                return false;
            return super.isEnabled();
        }

        @Override
        public AxisAlignedBB getBoundingBox() {
            return boundingBox;
        }

        @Override
        public Vector3d getAnchorPos(double partialTicks) {
            if (feetBound)
                return ArmorDisplay.this.getAnchorPos(partialTicks);
            else {
                VRData data = mc.vrPlayer.getVRDataWorld();
                return data.hmd.getPosition();
            }
        }
    }
}
