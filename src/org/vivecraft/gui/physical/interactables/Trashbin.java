package org.vivecraft.gui.physical.interactables;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.gui.physical.PhysicalInventory;
import org.vivecraft.gui.physical.PhysicalItemSlotGui;
import org.vivecraft.gui.physical.WindowCoordinator;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Quaternion;

public class Trashbin extends CreativeItemSlot {
    ModelResourceLocation binLoc;

    boolean charging = false;
    double charge = 0;
    double lastCharge = 0;
    double chargePerTick = 0.01;
    double chargeMultPerTick = 1.03;

    public Trashbin(PhysicalItemSlotGui gui) {
        super(gui, ItemStack.EMPTY, -1);
        binLoc = new ModelResourceLocation("vivecraft:trashbin");
    }

    @Override
    public void render(double partialTicks, int renderLayer) {

        GlStateManager.pushMatrix();
        double binScale = 0.4;


        Vector3d offset = new Vector3d(-0.21, -0.03, -0.21);
        double spin = ((lastCharge + (charge - lastCharge) * partialTicks) * 5 * 360) % 360;

        Utils.glRotate(new Quaternion(-90, 0, 0));
        Utils.glRotate(new Quaternion(0, (float) spin, 0));
        GlStateManager.translated(offset.x, offset.y, offset.z);
        GlStateManager.scalef((float) binScale, (float) binScale, (float) binScale);

        PhysicalInventory.renderCustomModel(binLoc);

        GlStateManager.popMatrix();
        super.render(partialTicks, renderLayer);
    }

    @Override
    public void update() {
        lastCharge = charge;
        if (charging) {
            charge += chargePerTick;
            charge *= chargeMultPerTick;
            if (charge >= 1) {
                charging = false;
                Vector3d offset = new Vector3d(-0.3, -0.3, 0);
                Vector3d bagCenter = getAnchorPos(0).add(getAnchorRotation(0).multiply(offset));

                Utils.spawnParticles(ParticleTypes.EXPLOSION, 100, bagCenter, new Vector3d(0.1, 0.1, 0.1), 0);
                mc.physicalGuiManager.windowCoordinator.enqueueOperation(new WindowCoordinator.ClearInventoryOperation());
            }
        } else {
            charge = 0;
        }
    }

    @Override
    public void click(int button) {
        if (getDisplayedItem() != null && !getDisplayedItem().isEmpty()) {
            super.click(button);
        } else {
            charging = true;
        }
    }

    @Override
    public void unclick(int button) {
        charging = false;
    }

    @Override
    public ItemStack getDisplayedItem() {
        if (gui.touching == this) {
            ItemStack fakeItem = mc.physicalGuiManager.getVirtualHeldItem();
            if (!fakeItem.isEmpty())
                return fakeItem;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return super.getBoundingBox().shrink(0.13);
    }
}
