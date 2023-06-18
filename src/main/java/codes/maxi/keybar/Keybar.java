package codes.maxi.keybar;


import codes.maxi.keybar.api.KeybarApi;
import codes.maxi.keybar.binding.BindingManager;
import codes.maxi.keybar.binding.OptionsBindingProvider;
import codes.maxi.keybar.gui.screen.KeybarScreen;
import codes.maxi.keybar.mixin.KeyMappingAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class Keybar implements ClientModInitializer, KeybarApi {
    public static final Minecraft mc = Minecraft.getInstance();

    @Nullable
    public static Keybar kb = null;

    public final Scheduler scheduler = new Scheduler();
    public final BindingManager bindings = new BindingManager();

    public final KeyMapping openKeybarKeybinding;

    public Keybar() {
        kb = this;

        KeyMappingAccessor.getCategoryOrder().put("key.category.keybar", KeyMappingAccessor.getCategoryOrder().size());
        openKeybarKeybinding = new KeyMapping(
                "key.keybar.open_keybar",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.category.keybar"
        );
    }

    @Override
    public void onInitializeClient() {
        scheduler.listenForTickEnd(client -> {
            while(openKeybarKeybinding.consumeClick()) {
                client.setScreen(new KeybarScreen());
            }
        });

        bindings.addProvider(new OptionsBindingProvider());
    }
}
