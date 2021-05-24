package org.vivecraft.asm.handler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.vivecraft.asm.ASMClassHandler;
import org.vivecraft.asm.ASMMethodHandler;

public class ASMHandlerFluidState extends ASMClassHandler {
    @Override
    public String getDesiredClass() {
        return "net/minecraft/fluid/FluidState";
    }

    @Override
    protected void patchClassRoot(ClassNode classNode) {
        classNode.access &= ~Opcodes.ACC_FINAL;
        System.out.println("Made class non-final");
    }

    @Override
    public ASMMethodHandler[] getMethodHandlers() {
        return new ASMMethodHandler[]{};
    }
}
