package org.vivecraft.asm;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.objectweb.asm.tree.ClassNode;
import org.vivecraft.asm.handler.ASMHandlerContainerScreen;
import org.vivecraft.asm.handler.ASMHandlerCreativeScreen;
import org.vivecraft.asm.handler.ASMHandlerFluidState;
import org.vivecraft.asm.handler.ASMHandlerItemRayTrace;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VivecraftASMTransformer implements ITransformer<ClassNode> {
    private final List<ASMClassHandler> asmHandlers = new ArrayList<ASMClassHandler>();

    public VivecraftASMTransformer() {
        asmHandlers.add(new ASMHandlerContainerScreen());
        asmHandlers.add(new ASMHandlerCreativeScreen());
        asmHandlers.add(new ASMHandlerFluidState());
        asmHandlers.add(new ASMHandlerItemRayTrace());
    }

    @Override
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        for (ASMClassHandler handler : asmHandlers) {
            if (!handler.shouldPatchClass()) continue;
            String className = handler.getDesiredClass();
            if (className.equals(input.name)) {
                System.out.println("Patching class: " + className);
                handler.patchClass(input);
            }
        }
        return input;
    }

    @Nonnull
    @Override
    public TransformerVoteResult castVote(ITransformerVotingContext iTransformerVotingContext) {
        return TransformerVoteResult.YES;
    }

    @Nonnull
    @Override
    public Set<Target> targets() {
        HashSet<Target> set = new HashSet<>();
        for (ASMClassHandler handler : asmHandlers) {
            if (!handler.shouldPatchClass()) continue;
            Target target = Target.targetClass(handler.getDesiredClass());
            set.add(target);
        }
        return set;
    }
}
