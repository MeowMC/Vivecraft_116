package org.vivecraft.gui.framework;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.vector.Vector2f;
import net.optifine.Lang;
import org.vivecraft.settings.VRSettings;

import java.util.function.BiFunction;

public class VROptionLayout {
    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;
    public boolean _enabled;
    Position _pos;
    float _row;
    String _title = "";
    int _ordinal;
    boolean _defaultb;
    float _defaultf;
    float _maxf;
    float _minf;
    float _incrementf;
    int _defaulti;
    int _maxi;
    int _mini;
    int _incrementi;
    Class<? extends Screen> screen;
    BiFunction<GuiVROptionButton, Vector2f, Boolean> customHandler;
    private VRSettings.VrOptions _e;
    public VROptionLayout(VRSettings.VrOptions e, BiFunction<GuiVROptionButton, Vector2f, Boolean> handler, Position pos, float row, boolean enabled, String title) {
        _e = e;
        _pos = pos;
        _row = row;
        if (title != null)
            _title = title;
        _enabled = enabled;
        this.customHandler = handler;
    }

    public VROptionLayout(VRSettings.VrOptions e, Position pos, float row, boolean enabled, String title) {
        _e = e;
        _pos = pos;
        _row = row;
        if (title != null)
            _title = title;
        _enabled = enabled;
    }

    public VROptionLayout(Class<? extends Screen> screen, BiFunction<GuiVROptionButton, Vector2f, Boolean> handler, Position pos, float row, boolean enabled, String title) {
        _pos = pos;
        _row = row;
        if (title != null)
            _title = title;
        _enabled = enabled;
        this.screen = screen;
        this.customHandler = handler;
    }

    public VROptionLayout(Class<? extends Screen> screen, Position pos, float row, boolean enabled, String title) {
        _pos = pos;
        _row = row;
        if (title != null)
            _title = title;
        _enabled = enabled;
        this.screen = screen;
    }

    public VROptionLayout(BiFunction<GuiVROptionButton, Vector2f, Boolean> handler, Position pos, float row, boolean enabled, String title) {
        _pos = pos;
        _row = row;
        if (title != null)
            _title = title;
        _enabled = enabled;
        this.customHandler = handler;
    }

    public VROptionLayout(int ordinal, Position pos, float row, boolean enabled, String title) {
        _ordinal = ordinal;
        _pos = pos;
        _row = row;
        _title = title;
        _enabled = enabled;
    }

    public int getX(int screenWidth) {
        if (_pos == Position.POS_LEFT)
            return screenWidth / 2 - 155 + 0 * 160;
        else if (_pos == Position.POS_RIGHT)
            return screenWidth / 2 - 155 + 1 * 160;
        else
            return screenWidth / 2 - 155 + 1 * 160 / 2;
    }

    public int getY(int screenHeight) {
        return (int) Math.ceil(screenHeight / 6 + 21 * _row - 10);
    }

    public String getButtonText() {
        if (_title.isEmpty()) {
            if (_e != null)
                return Minecraft.getInstance().vrSettings.getButtonDisplayString(_e);
        }

        return Lang.get(_title);
    }

    public VRSettings.VrOptions getOption() {
        return _e;
    }

    public Class<? extends Screen> getScreen() {
        return screen;
    }

    public BiFunction<GuiVROptionButton, Vector2f, Boolean> getCustomHandler() {
        return customHandler;
    }

    public int getOrdinal() {
        if (_e == null)
            return _ordinal;
        else
            return _e.returnEnumOrdinal();
    }

    public enum Position {
        POS_LEFT,
        POS_CENTER,
        POS_RIGHT,
    }
}
