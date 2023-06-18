package codes.maxi.keybar.mixin;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;
import static codes.maxi.keybar.Keybar.kb;

@Mixin(Options.class)
public class OptionsMixin {
    @Mutable
    @Final
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(at = @At("HEAD"), method = "load()V")
    public void load(CallbackInfo info) {
        List<KeyMapping> keys = new ArrayList<>(List.of(keyMappings));
        keys.add(kb.openKeybarKeybinding);
        keyMappings = keys.toArray(new KeyMapping[]{});
    }
}
