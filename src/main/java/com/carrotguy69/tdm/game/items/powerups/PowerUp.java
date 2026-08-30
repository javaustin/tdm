package com.carrotguy69.tdm.game.items.powerups;

import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PowerUp extends CustomItem {

    /*
    CustomItem (with type powerup) - contains data about the powerup, no implementation

    PowerUp (extends CustomItem)
        - contains pickupAction
        - we don't know what defines as being used, so we need to expose events
        * Think of PowerUp as a wrapper of CustomItem (where CustomItem.Type = POWER_UP)

        - consumers should contain (GamePlayer)

    PowerUpPickup (extends CustomItem)
        - contains location of the pickup and calls pickupAction from PowerUp

     */

    private Consumer<GamePlayer> pickupAction = powerUp -> {};
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final CustomItem originalItem;

    private final GamePlayer player;


    public PowerUp(CustomItem customItem, GamePlayer player) {
        super(
                customItem.getID(),
                customItem.getCustomName(),
                customItem.getMaterial(),
                customItem.getCustomItemType().name(),
                customItem.getAmount(),
                customItem.getLore(),
                customItem.getEnchants()
        );

        this.originalItem = customItem;
        this.player = player;
    }

    public @Nullable GamePlayer getPlayer() {
        return this.player;
    }

    public CustomItem getOriginalItem() {
        return this.originalItem;
    }

    public void setPickupAction(Consumer<GamePlayer> action) {
        this.pickupAction = action;
    }

    public Consumer<GamePlayer> getPickupAction() {
        return pickupAction;
    }

    public void addTask(BukkitTask task) {
        tasks.add(task);
    }

    public void removeTask(BukkitTask task) {
        tasks.remove(task);
    }

    public List<BukkitTask> getTasks() {
        return tasks;
    }

    // --- Listeners / Handlers ---

    public final Map<Class<? extends Event>, List<PowerUpListener<?>>> listeners = new HashMap<>();

    public <T extends Event> void on(Class<T> eventClass, PowerUpListener<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void handleEvent(T event) {
        List<PowerUpListener<?>> list = listeners.get(event.getClass());

        if (list == null)
            return;

        for (PowerUpListener<?> raw : list) {
            ((PowerUpListener<T>) raw).handle(event, this);
        }
    }

}
