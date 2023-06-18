package codes.maxi.keybar.binding;
import codes.maxi.keybar.mixin.KeyMappingAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.KeyBindingUtils;
import de.siphalor.amecs.api.KeyModifiers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import de.siphalor.amecs.api.AmecsKeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static codes.maxi.keybar.Keybar.kb;
import static codes.maxi.keybar.Keybar.mc;

public class OptionsBinding extends Binding<OptionsBindingProvider> {
    private final KeyMapping mcKeyMapping;
    public OptionsBinding(KeyMapping keybinding, OptionsBindingProvider source) {
        super(source,
            keybinding.getName(),
            keybinding.getCategory(),
            getKeyArray(keybinding)
        );


        this.mcKeyMapping = keybinding;
    }

    /**
     * utility to get array of keys in KeyMapping because there is too much logic to do that in the constructor super() call
     */
    private static InputConstants.Key[] getKeyArray(KeyMapping keybinding) {
        List<InputConstants.Key> keys = new ArrayList<>(List.of(new InputConstants.Key[]{((KeyMappingAccessor) keybinding).getKey()}));

        if (keybinding.isUnbound()) return new InputConstants.Key[]{};
        if (keybinding instanceof AmecsKeyBinding amecs) {
            KeyModifiers mods = KeyBindingUtils.getBoundModifiers(amecs);
            if(mods.getControl()) keys.add(InputConstants.getKey(GLFW.GLFW_KEY_LEFT_SHIFT, -1));
        }
        else return new InputConstants.Key[]{((KeyMappingAccessor) keybinding).getKey()};

        return keys.toArray(new InputConstants.Key[]{});
    }

    @Override
    public void activate() {
        // Mark KeyMapping as wasPressed AND press it for two ticks
        mcKeyMapping.setDown(true);
        if(keys.length > 0) KeyMapping.click(keys[0]);

        // Screenshot and fullscreen keys are checked for using KeyMapping.matchesKey() in Keyboard.onKey()
        // instead of KeyMapping.wasPressed()
        if(mcKeyMapping.equals(mc.options.keyScreenshot)) Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(), msg -> mc.execute(() -> mc.gui.getChat().addMessage(msg)));
        else if(mcKeyMapping.equals(mc.options.keyFullscreen)) {
            mc.getWindow().toggleFullScreen();
            mc.options.fullscreen().set(mc.getWindow().isFullscreen());
        }

        kb.scheduler.scheduleForTickStart(mc -> mcKeyMapping.setDown(false), 2);
    }
}
