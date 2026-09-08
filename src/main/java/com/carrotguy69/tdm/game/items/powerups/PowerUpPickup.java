package com.carrotguy69.tdm.game.items.powerups;


import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import com.carrotguy69.tdm.utils.Logger;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.carrotguy69.cxyz.CXYZ.f;
import static com.carrotguy69.cxyz.CXYZ.plugin;

public class PowerUpPickup extends CustomItem {

    public static List<PowerUpPickup> activePickupLocations = new ArrayList<>();

    private final CustomItem originalItem;
    private Location location;
    public boolean isDead;

    public PowerUpPickup(CustomItem item, Location location) {
        super(
                item.getID(),
                item.getCustomName(),
                item.getMaterial(),
                item.getCustomItemType().name(),
                item.getAmount(),
                item.getLore(),
                item.getEnchants()
        );
        this.location = location;
        this.originalItem = item;
        this.isDead = false;
    }

    public void spawn() {

        Logger.log("Spawned " + this.getID() + " at" + this.getLocation());

        // Remove previous power up if somehow not cleared
        for (Entity en : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
            if (en.getType() == EntityType.ARMOR_STAND || en.getType() == EntityType.ITEM_DISPLAY) {
                en.remove();
            }
        }

        location = location.clone().add(0.5, 0, 0.5);

        ArmorStand nameTagStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        nameTagStand.setVisible(false);
        nameTagStand.setGravity(false);
        nameTagStand.setCustomNameVisible(true);
        nameTagStand.setCustomName(f(originalItem.getCustomName()));

        ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(location.clone().add(0, 1.5, 0), EntityType.ITEM_DISPLAY);
        itemDisplay.setItemStack(originalItem.toItemStack());
        itemDisplay.setDisplayWidth(0.25f);
        itemDisplay.setDisplayHeight(0.25f);

        int[] frame = {1};
        new BukkitRunnable() {public void run () {
            if (itemDisplay.isDead() || nameTagStand.isDead()) {
                this.cancel();
                return;
            }

            Location location = itemDisplay.getLocation();

            double wave = Math.sin(Math.PI * frame[0]);
            frame[0] += 1;

            location.add(0, wave, 0);
            location.setRotation(frame[0], 0);

            itemDisplay.teleport(location);
        }}.runTaskTimer(plugin, 0L, 0L);

        activePickupLocations.add(this);
    }

    public void despawn() {
        for (Entity en : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
            if (en.getType() == EntityType.ARMOR_STAND || en.getType() == EntityType.ITEM_DISPLAY) {
                en.remove();
            }
        }

        this.isDead = true;

        activePickupLocations.remove(this);
    }

    public static void despawnAll() {
        for (PowerUpPickup entry : activePickupLocations) {
            Location location = entry.location;

            for (Entity en : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
                if (en.getType() == EntityType.ARMOR_STAND || en.getType() == EntityType.ITEM_DISPLAY) {
                    en.remove();
                }
            }

        }
        activePickupLocations.clear();
    }

    public boolean applyTo(GamePlayer gp) {

        PowerUp powerUp = GenericItemRegistry.powerUps.get(originalItem.getID());

        if (powerUp == null) {
            throw new RuntimeException(String.format("Pickup failed because powerUp with id='%s' was null!", originalItem.getID()));
        }

        if (GenericItemRegistry.powerUpsByPlayer.containsEntry(gp.getUUID(), powerUp)) {
            MessageUtils.sendActionBar(gp.getBukkitPlayer(), MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP_FAIL, Map.of("", "")));
            return false;
        }

        powerUp.getPickupAction().accept(gp);
        GenericItemRegistry.powerUpsByPlayer.put(gp.getUUID(), powerUp);

        return true;
    }

    public Location getLocation() {
        return this.location;
    }

    public static PowerUpPickup getNearby(Location location) {

        if (activePickupLocations.isEmpty()) {
            return null;
        }

        PowerUpPickup closest = activePickupLocations.getFirst();

        for (PowerUpPickup pickup : activePickupLocations) {
            if (pickup.getLocation().distance(location) < closest.getLocation().distance(location)) {
                closest = pickup;
            }
        }

        return closest;
    }

    public static void cleanLocations() {
        if (activePickupLocations.size() <= 1) {
            return;
        }

        PowerUpPickup lastPickup = activePickupLocations.getLast();

        for (int i = activePickupLocations.size() - 1; i >= 0; i--) {
            PowerUpPickup pickup = activePickupLocations.get(i);

            if (pickup != lastPickup && pickup.getLocation().getX() == lastPickup.getLocation().getX() && pickup.getLocation().getZ() == lastPickup.getLocation().getZ()) {
                Logger.warning("Active pickups are similar: " + pickup + ", " + lastPickup);
                activePickupLocations.remove(i);
            }
        }

    };

    public String toString() {
        return "PowerUpPickup{" +
                "id=" + this.getID() + "," +
                "location=" + this.getLocation() +
                "}";
    }

}
