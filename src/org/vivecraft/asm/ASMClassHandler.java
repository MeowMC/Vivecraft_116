package org.vivecraft.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class ASMClassHandler {
    public abstract String getDesiredClass();

    public abstract ASMMethodHandler[] getMethodHandlers();

    public boolean shouldPatchClass() {
        return true;
    }

    protected void patchClassRoot(ClassNode classNode) {
    }

    public final void patchClass(ClassNode classNode) {
        patchClassRoot(classNode);
        ASMMethodHandler[] handlers = getMethodHandlers();
        ArrayList<MethodNode> methodsCopy = new ArrayList<MethodNode>(classNode.methods);
        for (Iterator<MethodNode> methods = methodsCopy.iterator(); methods.hasNext(); ) {
            MethodNode method = methods.next();
            for (ASMMethodHandler handler : handlers) {
                MethodTuple tuple = handler.getDesiredMethod();
                if (method.name.equals(tuple.methodName) && method.desc.equals(tuple.methodDesc)) {
                    System.out.println("Patching method: " + method.name + method.desc);
                    handler.patchMethod(method, classNode);
                }
            }
        }
    }
}
