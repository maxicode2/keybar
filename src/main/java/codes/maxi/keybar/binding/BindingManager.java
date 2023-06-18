package codes.maxi.keybar.binding;

import java.util.*;

public class BindingManager implements BindingProvider<Binding> {
    private final List<BindingProvider<Binding>> providers = new ArrayList<>();

    public void addProvider(BindingProvider<? extends Binding> provider) {
        this.providers.add((BindingProvider<Binding>) provider);
    }

    public Set<String> getOrder() {
        Set<String> order = new LinkedHashSet<>();

        this.providers.forEach(provider -> order.addAll(provider.getOrder()));
        return order;
    }
    public List<Binding> getBindings() {
        return this.providers.stream().flatMap(provider -> provider.getBindings().stream()).toList();
    }

    public List<Binding> getOrdered() {
        // Group the list of bindings by category
        HashMap<String, List<Binding>> bindings = new HashMap<>();

        this.providers.forEach(provider -> {
            provider.getBindings().forEach(binding -> {
                List<Binding> l;
                String category = binding.getCategory();
                if(bindings.containsKey(category)) l = bindings.get(category);
                else bindings.put(category, (l = new LinkedList<>()));
                l.add(binding);
            });
        });
        return getOrder().stream().map(bindings::get).flatMap(Collection::stream).toList();
    }
}
