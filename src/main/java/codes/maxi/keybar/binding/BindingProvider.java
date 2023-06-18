package codes.maxi.keybar.binding;
import java.util.List;
import java.util.Set;

public interface BindingProvider<T extends Binding> {
    List<T> getBindings();
    default List<T> getOrdered() {

        return null;
    }

    Set<String> getOrder();
}
