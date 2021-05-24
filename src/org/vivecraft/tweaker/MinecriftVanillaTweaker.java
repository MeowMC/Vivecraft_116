package org.vivecraft.tweaker;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.List;

public class MinecriftVanillaTweaker implements ITweaker {
    private static void dbg(String str) {
        System.out.println(str);
    }

    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        dbg("MinecriftVanillaTweaker: acceptOptions");
    }

    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        dbg("MinecriftVanillaTweaker: injectIntoClassLoader");
        classLoader.addTransformerExclusion("org.vivecraft.asm.");
        classLoader.registerTransformer("org.vivecraft.tweaker.MinecriftClassTransformer");
    }

    public String getLaunchTarget() {
        dbg("MinecriftVanillaTweaker: getLaunchTarget");
        return "net.minecraft.client.main.Main";
    }

    public String[] getLaunchArguments() {
        dbg("MinecriftVanillaTweaker: getLaunchArguments");
        return new String[0];
    }
}
