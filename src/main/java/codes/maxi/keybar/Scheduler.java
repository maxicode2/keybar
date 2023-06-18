package codes.maxi.keybar;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Scheduler {

    private List<Scheduled> startScheduled = new ArrayList<>();
    private List<Consumer<Minecraft>> startListeners = new ArrayList<>();

    public void scheduleForNextTickStart(Consumer<Minecraft> cb) {
        scheduleForTickStart(cb, 1);}
    public void scheduleForTickStart(Consumer<Minecraft> cb, int delay) {
        startScheduled.add(new Scheduled(cb, delay));
    }
    public void listenForTickStart(Consumer<Minecraft> cb) {
        startListeners.add(cb);
    }
    public void onStartClientTick(Minecraft mc) {
        List<Consumer<Minecraft>> listeners = new ArrayList<>(startListeners);
        listeners.forEach(listener -> listener.accept(mc));

        List<Scheduled> scheduled = startScheduled;
        startScheduled = new ArrayList<>();
        scheduled.forEach(cb -> {
            if(!cb.tick(mc)) startScheduled.add(cb);
        });
    }


    private List<Scheduled> endScheduled = new ArrayList<>();
    private List<Consumer<Minecraft>> endListeners = new ArrayList<>();

    public void scheduleForNextTickEnd(Consumer<Minecraft> cb) {
        scheduleForTickEnd(cb, 1);}
    public void scheduleForTickEnd(Consumer<Minecraft> cb, int delay) {
        endScheduled.add(new Scheduled(cb, delay));
    }
    public void listenForTickEnd(Consumer<Minecraft> cb) {
        endListeners.add(cb);
    }
    public void onEndClientTick(Minecraft mc) {
        List<Consumer<Minecraft>> listeners = new ArrayList<>(endListeners);
        listeners.forEach(listener -> listener.accept(mc));

        List<Scheduled> scheduled = endScheduled;
        endScheduled = new ArrayList<>();
        scheduled.forEach(cb -> {
            if(!cb.tick(mc)) endScheduled.add(cb);
        });
    }



    private static class Scheduled {
        private final Consumer<Minecraft> cb;
        private int remaining;

        public Scheduled(Consumer<Minecraft> cb, int remaining) {
            this.cb = cb;
            this.remaining = remaining;
        }
        public boolean tick(Minecraft mc) {
            if(remaining-- <= 0) {
                cb.accept(mc);
                return true;
            }
            return false;
        }
    }
}
