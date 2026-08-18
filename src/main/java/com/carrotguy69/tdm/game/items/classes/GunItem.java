package com.carrotguy69.tdm.game.items.classes;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.game.items.managers.CustomItemManager;
import com.carrotguy69.tdm.game.items.managers.GunManager;
import com.carrotguy69.tdm.game.items.GenericItem;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.utils.Logger;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.carrotguy69.cxyz.CXYZ.f;
import static com.carrotguy69.cxyz.CXYZ.plugin;


public class GunItem implements GenericItem {

    private final String id;
    private String displayName;
    private final Material material;
    private final int magSize;
    private int currentAmmo;
    private final int projectilesAmount;
    private final double spreadVertical;
    private final double spreadHorizontal;
    private final double fireRate;
    private final double reloadTime;
    private final double damage;
    private final double range;

    private long lastFiredTime = 0;

    private boolean isReloading;

    private List<String> lore;
    private List<Enchant> enchants;

    public GunItem(String id, String displayName, Material material, int magSize, int startingAmmo, int projectilesAmount, double spreadVertical, double spreadHorizontal, double fireRate, double reloadTime, double damage, double range) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.magSize = magSize;
        this.currentAmmo = startingAmmo;
        this.projectilesAmount = projectilesAmount;
        this.spreadVertical = spreadVertical;
        this.spreadHorizontal = spreadHorizontal;
        this.fireRate = fireRate;
        this.reloadTime = reloadTime;
        this.damage = damage;
        this.range = range;
    }

    public String getID() {
        return id;
    }

    @Override
    public String getCustomName() {
        return displayName;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public int getAmount() {
        return 1;
    }

    @Override
    public Type getType() {
        return Type.GUN;
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public List<Enchant> getEnchants() {
        return enchants;
    }

    @Override
    public GunItem copy() {
        GunItem gunItem = new GunItem(this.id, this.displayName, this.material, this.magSize, this.currentAmmo, this.projectilesAmount, this.spreadVertical, this.spreadHorizontal, this.fireRate, this.reloadTime, this.damage, this.range);

        gunItem.setLore(gunItem.getLore());
        gunItem.setEnchants(gunItem.getEnchants());

        return gunItem;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setEnchants(List<Enchant> enchants) {
        this.enchants = enchants;
    }

    public double getSpreadHorizontal() {
        return spreadHorizontal;
    }

    public int getMagSize() {
        return magSize;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getProjectilesAmount() {
        return projectilesAmount;
    }

    public double getSpreadVertical() {
        return spreadVertical;
    }

    public double getFireRate() {
        return fireRate;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public double getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }

    public void setCurrentAmmo(int newAmount) {

        if (newAmount > magSize) {
            Logger.severe(String.format("Attempted to set an amount of ammo beyond the mag size! (%d > %d)", newAmount, magSize));
            return;
        }

        this.currentAmmo = newAmount;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        meta.getPersistentDataContainer().set(
                GenericItemRegistry.customTypeKey,
                PersistentDataType.STRING,
                id
        );

        // Sorry paper, I like my coloring better
        meta.setDisplayName(f(displayName));

        if (lore != null) {
            List<String> coloredLore = new ArrayList<>();

            for (String line : lore) {
                coloredLore.add(f(line));
            }

            meta.setLore(coloredLore);
        }

        if (enchants != null) {
            for (Enchant enchant : enchants) {

                Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

                Enchantment mcEnchantment = registry.get(NamespacedKey.minecraft(enchant.id()));

                if (mcEnchantment == null) {
                    continue;
                }

                meta.addEnchant(mcEnchantment, enchant.level(), true);            }
        }

        itemStack.setItemMeta(meta);


        return itemStack;
    }

    public void fire(Player shooter) {

        Game game = Game.getByPlayer(shooter);

        if (game == null) {
            return;
        }

        if (game.getGameState() != GameState.ACTIVE) {
            return;
        }

        if (isReloading)
            return;

        if (currentAmmo <= 0) {

            if (GunManager.getSpareAmmo(shooter) <= 0) {
                shooter.playSound(shooter, Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
                MessageUtils.sendActionBar(shooter, MessageGrabber.grab(TDMMessageKey.GUN_OUT_OF_AMMO));
            }

            else {
                reload(shooter);
            }

            return;
        }

        if (!canFire()) {
            return;
        }

        lastFiredTime = System.currentTimeMillis();

        float pitch = (float) (1.5f + (0.025 * damage)); // pitch will be based off of damage

        shooter.getWorld().playSound(shooter, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, pitch);

        Location startLocation = shooter.getEyeLocation();
        Vector direction = startLocation.getDirection();

        spawnBulletProjectiles(shooter, projectilesAmount, startLocation);

        Vector recoilVector = direction.clone().multiply(-0.05);
        shooter.setVelocity(shooter.getVelocity().add(recoilVector));

        doBulletCasingAnimation(startLocation, direction, CustomItemManager.getByType(CustomItem.Type.AMMO).toItemStack());

        currentAmmo--;
        MessageUtils.sendActionBar(shooter, MessageGrabber.grab(TDMMessageKey.GUN_AMMO_INDICATOR, Map.of("ammo", currentAmmo, "mag-size", magSize)));
    }

    public void reload(Player shooter) {

        Game game = Game.getByPlayer(shooter);

        if (game == null) {
            return;
        }

        if (game.getGameState() != GameState.ACTIVE) {
            return;
        }

        if (currentAmmo >= magSize)
            return;

        if (isReloading)
            return;

        int maxReloadAmount = Math.min(GunManager.getSpareAmmo(shooter), magSize); // 3

        if (maxReloadAmount == 0) {
            shooter.playSound(shooter, Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            MessageUtils.sendActionBar(shooter, MessageGrabber.grab(TDMMessageKey.GUN_OUT_OF_AMMO));
            return;
        }

        int ammoToAdd = Math.min(magSize - currentAmmo, maxReloadAmount);

        final int[] tick = new int[]{0};
        final int[] lastFrame = new int[]{0};
        GunItem gunRef = this;

        new BukkitRunnable() {public void run() {
            isReloading = true;

            int frame = (int) Math.floor(tick[0] / (2 * reloadTime));
            MessageUtils.sendActionBar(shooter, getReloadAnimationFrame(frame));

            if (frame > lastFrame[0]) {
                // We only want to play sounds when the progress bar frame updates.
                shooter.playSound(shooter, Sound.UI_BUTTON_CLICK, 1.0f, 0.5f + (frame * 0.15f));
            }
            lastFrame[0] = frame;

            String gunID = GunManager.getGunID(shooter.getInventory().getItemInMainHand());

            if (gunID == null || !gunID.equals(gunRef.id)) {
                // cancel reload because player switched hands
                isReloading = false;

                MessageUtils.sendActionBar(shooter, MessageGrabber.grab(TDMMessageKey.GUN_RELOAD_FAIL));
                shooter.playSound(shooter, Sound.BLOCK_NOTE_BLOCK_HARP, 2.0f, 1f);

                this.cancel();
                return;
            }

            if (((double) tick[0] / 20) >= reloadTime) {
                // reload is complete


                isReloading = false;

                MessageUtils.sendActionBar(shooter, MessageGrabber.grab(TDMMessageKey.GUN_RELOAD_SUCCESS));
                shooter.playSound(shooter, Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 2f);

                int newAmountInChamber = gunRef.currentAmmo + ammoToAdd;
                gunRef.setCurrentAmmo(newAmountInChamber);

                int leftover = GunManager.getSpareAmmo(shooter);

                GunManager.setLeftoverAmmo(shooter, Math.max(0, leftover - ammoToAdd));


                this.cancel();
                return;
            }

            tick[0] += 1;
        }}.runTaskTimer(plugin, 0L, 1L);
    }

    private String getReloadAnimationFrame(int count) {
        StringBuilder progress = new StringBuilder();
        progress.repeat(MessageGrabber.grab(TDMMessageKey.GUN_RELOAD_PROGRESS_UNITS), Math.max(0, count));

        StringBuilder background = new StringBuilder();
        background.repeat(MessageGrabber.grab(TDMMessageKey.GUN_RELOAD_BG_UNITS), Math.max(0, (10 - count)));

        return progress + background.toString();
    }

    private void doBulletCasingAnimation(Location startLocation, Vector direction, ItemStack bulletItem) {
        World world = startLocation.getWorld();
        // Spawn golden nugget representing bullet
        Item bullet = world.dropItem(startLocation, bulletItem);
        bullet.setVelocity(direction.multiply(-0.1)); // Adjust velocity for bullet speed
        bullet.setPickupDelay(32767); // Prevent pickup

        // Schedule task to remove bullet after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                bullet.remove();
            }
        }.runTaskLater(plugin, 60L); // 60 ticks = 3 seconds
    }

    private void spawnBulletProjectiles(Player shooter, int n, Location origin) {
        // Normalize to unit vector so math is predictable
        Vector direction = origin.getDirection().normalize();

        // This vector is where all the bullets would go if there were no randomness/spread.
        Vector centerOffset = direction.clone().multiply(range);

        // Chose a world reference up vector. A stable global direction we can cross product with.
        Vector worldUp = new Vector(0, 1, 0);

        // If direction is close to worldUp, pick another (degeneracy check).
        if (Math.abs(direction.dot(worldUp)) > 0.99) {
            worldUp = new Vector(1, 0, 0);
        }

        // We want two vectors that form axes (right, up) in the plane perpendicular to the muzzle direction.
        // Then any spread offset can be described as right*x + up*y
        Vector right = direction.clone().crossProduct(worldUp).normalize();

        Vector up = direction.clone().crossProduct(right).normalize();

        // Compute end vector
        for (int i = 0; i < n; i++) {
            double x = (n == 1)
                    ? 0.0
                    : (2.0 * i) / (n - 1.0) - 1.0;

            x *= spreadHorizontal;

            double y = (Math.random() * 2.0 - 1.0) * spreadVertical;

            Vector offset = right.clone()
                    .multiply(x)
                    .add(up.clone().multiply(y));

            Vector end = centerOffset.clone().add(offset);

            Location endLoc = origin.clone().add(end);

            spawnSingleBullet(shooter, origin, endLoc);
        }
    }

    private void spawnSingleBullet(Player shooter, Location origin, Location end) {
        World world = origin.getWorld();
        world.spawnParticle(Particle.CRIT, origin, 1, 0, 0, 0, 0); // Spawn particle at start location

        Vector toEnd = end.toVector().subtract(origin.toVector());
        double maxDist = toEnd.length();


        Vector direction = toEnd.clone().multiply(1.0 / maxDist);

        RayTraceResult blockHit = shooter.getWorld().rayTraceBlocks(origin, direction, maxDist, FluidCollisionMode.NEVER, true);

        double blockHitDistance = Double.MAX_VALUE;

        if (blockHit != null) {
            blockHitDistance = blockHit.getHitPosition()
                    .distance(origin.toVector());
        }

        double stepSize = 0.2; // Smaller step size for closely spaced particles


        for (double distance = 0; distance < maxDist; distance += stepSize) {

            if (distance >= blockHitDistance) {
                return;
            }

            Location particleLocation = origin.clone().add(direction.clone().multiply(distance));

            world.spawnParticle(Particle.CRIT, particleLocation, 1, 0, 0, 0, 0); // Spawn particle along trajectory

            for (Entity en : world.getNearbyEntities(particleLocation, 0.1, 0.1, 0.1)) {
                if (en != shooter) {
                    if (en instanceof Player) {
                        Game game = Game.getByPlayer(shooter);

                        if (game == null) {
                            continue;
                        }

                        boolean damageDealt = game.damageWithGun((Player) en, shooter, damage);

                        if (damageDealt) {
                            shooter.playSound(shooter, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
                            ((Player) en).setNoDamageTicks(0);
                            return;
                        }
                    }
                }
            }

//            if (!particleLocation.getBlock().isPassable() && particleLocation.getBlock().isCollidable()) {
//                return;
//            }
        }
    }

    public boolean canFire() {
        long currentTime = System.currentTimeMillis();
        double timeSinceLastFire = (currentTime - lastFiredTime) / 1000.0; // Convert milliseconds to seconds

        return timeSinceLastFire >= (1.0 / fireRate); // Check if enough time has passed since last firing
    }


    @Override
    public String toString() {
        return "GunItem{"
                + "id=" + id + ","
                + "displayName=" + displayName  + ","
                + "material=" + material.name()  + ","
                + "enchants=" + enchants + ","
                + "lore=" + lore +
                "}";
    }

}
