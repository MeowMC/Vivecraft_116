package org.vivecraft.gui.physical.interactables;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.vivecraft.utils.math.Quaternion;

public interface Interactable {
    void render(double partialTicks, int renderLayer);

    Vector3d getPosition(double partialTicks);

    Quaternion getRotation(double partialTicks);

    Vector3d getAnchorPos(double partialTicks);

    Quaternion getAnchorRotation(double partialTicks);

    boolean isEnabled();

    default boolean isTouchable() {
        return isEnabled();
    }

    void touch();

    void untouch();

    void click(int button);

    void unclick(int button);

    default void update() {
    }

    default void onDragDrop(Interactable source) {
    }

    AxisAlignedBB getBoundingBox();
}
