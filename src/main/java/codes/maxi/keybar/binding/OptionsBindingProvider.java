package codes.maxi.keybar.binding;
import codes.maxi.keybar.mixin.KeyMappingAccessor;

import java.util.*;

import static codes.maxi.keybar.Keybar.mc;

/**
 * Collects keybindings from Minecraft.options.keyMappings, AKA the options screen
 * Supports vanilla and AMECS keybindings.
 */
public class OptionsBindingProvider implements BindingProvider<OptionsBinding> {
    @Override
    public List<OptionsBinding> getBindings() {
        return Arrays.stream(mc.options.keyMappings).map(binding ->
            new OptionsBinding(binding,this)
        ).toList();
    }

    @Override
    public Set<String> getOrder() {
        List<String> order = new LinkedList<>();
        KeyMappingAccessor.getCategoryOrder().forEach((cat, num) -> {
            num -= 1; // order is one based instead of zero based for some stupid reason
            final int size = order.size();
            if(num >= size) {
                order.addAll(Collections.nCopies(num - size + 1, null));
            }
            order.set(num, cat);
        });

        return new LinkedHashSet<>(order);
    }
}
