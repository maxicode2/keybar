package codes.maxi.keybar.binding;
import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public abstract class Binding<T extends BindingProvider> {
    @Getter
    protected final T source;
    @Getter
    protected final String name;
    @Getter
    protected final String category;
    @Getter
    protected final InputConstants.Key[] keys;

    protected Binding(T source, String name, String category, InputConstants.Key[] keys) {
        this.source = source;
        this.name = name;
        this.category = category;
        this.keys = keys;
    }


    @Override
    @Nullable
    public String toString() {
        StringBuilder built = null;
        for(var key : keys) {
            if(built == null) built = new StringBuilder(key.getDisplayName().getString());
            else built.append(" + ").append(key.getDisplayName().getString());
        }
        return built != null ? built.toString() : null;
    }

    public abstract void activate();
}
