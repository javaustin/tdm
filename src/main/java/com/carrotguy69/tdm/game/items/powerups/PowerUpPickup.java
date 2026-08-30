package com.carrotguy69.tdm.game.items.powerups;


import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

import static com.carrotguy69.cxyz.CXYZ.f;
import static com.carrotguy69.cxyz.CXYZ.plugin;

public class PowerUpPickup extends CustomItem {

    public static List<PowerUpPickup> activePickupLocations = new ArrayList<>();

    private final CustomItem originalItem;
    private final Location location;

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
    }

    public void spawn() {

        // Remove previous power up if somehow not cleared
        for (Entity en : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
            if (en.getType() == EntityType.ARMOR_STAND) {
                en.remove();
            }
        }

        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.5, -1, 0.5), EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setHelmet(originalItem.toItemStack());

        ArmorStand nameTagStand = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
        nameTagStand.setVisible(false);
        nameTagStand.setGravity(false);
        nameTagStand.setCustomNameVisible(true);
        nameTagStand.setCustomName(f(originalItem.getCustomName()));

        new BukkitRunnable() {public void run () {
            if (armorStand.isDead()) {
                this.cancel();
                return;
            }
            Location location = armorStand.getLocation();
            location.setYaw(location.getYaw() + 2); // Rotate the yaw (horizontal rotation) by 1 degree
            armorStand.teleport(location); // Teleport the armor stand to update its rotation
        }}.runTaskTimer(plugin, 0L, 0L);

        activePickupLocations.add(this);
    }

    public void despawn() {
        for (Entity en : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
            if (en.getType() == EntityType.ARMOR_STAND) {
                en.remove();
            }
        }

        activePickupLocations.remove(this);
    }

    public static void despawnAll() {
        for (PowerUpPickup entry : activePickupLocations) {
            Location location = entry.location;

            for (Entity en : location.getWorld().getNearbyEntities(location, 2, 2, 2)) {
                if (en.getType() == EntityType.ARMOR_STAND) {
                    en.remove();
                }
            }

        }
        activePickupLocations.clear();
    }

    public void applyTo(GamePlayer gp) {
        PowerUp powerUp = GenericItemRegistry.powerUps.get(originalItem.getID());

        if (powerUp == null) {
            throw new RuntimeException(String.format("Pickup failed because powerUp with id='%s' was null!", originalItem.getID()));
        }

        GenericItemRegistry.powerUpsByPlayer.put(gp.getUUID(), powerUp);

        powerUp.getPickupAction().accept(gp);
    }

    public Location getLocation() {
        return this.location;
    }

}
