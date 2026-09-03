package com.carrotguy69.tdm.game.items.powerups;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.other.DamageSource;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.utils.Logger;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

import static com.carrotguy69.cxyz.CXYZ.f;

public class PowerUpLoader {


    public static void load() {
        loadExplosiveArrow();
        loadJumpPack();
        loadSmokeGrenade();
    }

    private static void loadExplosiveArrow() {

        PowerUp explosiveArrow = GenericItemRegistry.powerUps.get("explosive-arrow");

        if (explosiveArrow == null) {
            return;
        }

        Logger.log("Registered " + explosiveArrow.getID());


        explosiveArrow.setPickupAction(gp -> {
            Logger.log("Picked up " + explosiveArrow.getID());
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = explosiveArrow.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", f(explosiveArrow.getCustomName()), "n", "n"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        explosiveArrow.on(ProjectileHitEvent.class, (e, po) -> {
            Logger.log("Event " + explosiveArrow.getID());


            Location center = e.getEntity().getLocation();
            e.getEntity().remove();

            Player shooter = (Player) e.getEntity().getShooter();
            Game game = Game.getByPlayer(shooter);

            if (game == null) {
                return;
            }

            if (!e.getEntity().getName().equals(f(explosiveArrow.getCustomName()))) {
                return;
            }

            GamePlayer shooterGP = game.getPlayer(shooter);

            center.getWorld().spawnParticle(Particle.WHITE_ASH, center, 5000, 6, 2, 6);
            center.getWorld().spawnParticle(Particle.ASH, center, 5000, 6, 2, 6);
            center.getWorld().spawnParticle(Particle.EXPLOSION, center, 150, 6, 2, 6);
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

            for (Player p : center.getWorld().getNearbyEntitiesByType(Player.class, center, 8, 4, 8)) {

                GamePlayer hitGP = game.getPlayer(p);

                if (hitGP.getTeam().getID().equalsIgnoreCase(shooterGP.getTeam().getID())) {
                    continue;
                }

                double damage = 0;
                double distance = p.getLocation().distance(center);

                if (distance <= 4) {
                    damage = 20;
                }

                else if (distance <= 6) {
                    damage = 10;
                }

                else if (distance <= 8) {
                    damage = 5;
                }

                DamageSource source = new DamageSource(shooterGP, DamageSource.Reason.EXPLOSIVE);
                game.setLastDamageSource(hitGP, source);

                p.damage(damage);
            }


            new BukkitRunnable() {public void run() {
                Logger.log("Removed " + explosiveArrow.getID());
                GenericItemRegistry.powerUpsByPlayer.remove(shooterGP.getUUID(), explosiveArrow);
            }}.runTaskLater(TDM.plugin, 1);
        });
    }

    private static void loadJumpPack() {
        PowerUp jumpPack = GenericItemRegistry.powerUps.get("jump-pack");

        if (jumpPack == null) {
            return;
        }

        Logger.log("Registered " + jumpPack.getID());


        jumpPack.setPickupAction(gp -> {
            Logger.log("Picked up " + jumpPack.getID());
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = jumpPack.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", f(jumpPack.getCustomName()), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

            p.setAllowFlight(true);
        });

        jumpPack.on(PlayerToggleFlightEvent.class, ((event, powerUp) -> {
            Logger.log("Event (toggle flight)" + jumpPack.getID());


            Player p = event.getPlayer();

            Game game = Game.getByPlayer(p);

            if (game == null)
                return;

            GamePlayer gp = game.getPlayer(p);

            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                return;
            }

            if (!p.getAllowFlight())
                return;

            // Find the item which allows this

            ItemStack matching = null;
            for (ItemStack itemStack : p.getInventory().all(jumpPack.getMaterial()).values()) {
                ItemMeta meta = itemStack.getItemMeta();

                if (meta != null && meta.getDisplayName().equals(f(jumpPack.getCustomName()))) {
                    matching = itemStack;
                    break;
                }
            }

            if (matching == null) {
                p.setAllowFlight(false);
                return;
            }

            p.getInventory().removeItem(matching);

            event.setCancelled(true);

            p.setVelocity(p.getLocation().getDirection().multiply(2).setY(1));
            p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            p.setAllowFlight(false);
            p.setFlying(false);

            new BukkitRunnable() {
                public void run() {
                    p.setAllowFlight(true);
                    game.noFallDamagePlayers.add(p.getUniqueId());
                }
            };

            new BukkitRunnable() {
                public void run() {
                    Logger.log("Removed " + jumpPack.getID());
                    GenericItemRegistry.powerUpsByPlayer.remove(p.getUniqueId(), jumpPack);
                }
            }.runTaskLater(TDM.plugin, 1);
        }));

        jumpPack.on(PlayerMoveEvent.class, ((event, powerUp) -> {
            Logger.log("Event (move) " + jumpPack.getID());


            Player p = event.getPlayer();

            Game game = Game.getByPlayer(p);

            if (game == null)
                return;

            GamePlayer gp = game.getPlayer(p);

            if (p.isOnGround()) {
                new BukkitRunnable() {
                    public void run() {
                        Logger.log("Removed " + jumpPack.getID());

                        GenericItemRegistry.powerUpsByPlayer.remove(p.getUniqueId(), jumpPack);
                        game.noFallDamagePlayers.remove(p.getUniqueId());
                    }
                }.runTaskLater(TDM.plugin, 1);
            }
        }));
    }

    private static void loadSmokeGrenade() {
        PowerUp smokeGrenade = GenericItemRegistry.powerUps.get("smoke-grenade");

        if (smokeGrenade == null) {
            return;
        }

        Logger.log("Registered " + smokeGrenade.getID());

        // BUG
        /*
§x§f§f§c§8§c§8S§x§f§f§c§6§c§1m§x§f§f§c§4§b§9o§x§f§f§c§2§b§2k§x§f§f§c§0§a§ae
vs
§x§f§f§b§c§9§bG§x§f§f§b§9§9§4r§x§f§f§b§7§8§ce§x§f§f§b§5§8§5n§x§f§f§b§3§7§da§x§f§f§b§1§7§6d§x§f§f§a§f§6§ee


        */

        smokeGrenade.setPickupAction(gp -> {
            Logger.log("Picked up " + smokeGrenade.getID());
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = smokeGrenade.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", f(smokeGrenade.getCustomName()), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        smokeGrenade.on(ProjectileHitEvent.class, (e, powerUp) -> {
            Logger.log("Event " + smokeGrenade.getID());

            Location center = e.getEntity().getLocation();
            e.getEntity().remove();

            Player shooter = (Player) e.getEntity().getShooter();
            Game game = Game.getByPlayer(shooter);

            if (game == null) {
                return;
            }

            if (!e.getEntity().getName().equals(f(powerUp.getCustomName()))) {
                Logger.log(e.getEntity().getName());
                Logger.log(f(powerUp.getCustomName()));
                return;
            }

            GamePlayer shooterGP = game.getPlayer(shooter);

            center.getWorld().spawnParticle(Particle.DUST, center, 10000, 6, 2, 6, new Particle.DustOptions(Color.WHITE, 4));
            center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 2.0f);

            for (Player p : center.getWorld().getNearbyEntitiesByType(Player.class, center, 8, 4, 8)) {

                GamePlayer hitGP = game.getPlayer(p);

                if (hitGP.getTeam().getID().equalsIgnoreCase(shooterGP.getTeam().getID())) {
                    continue;
                }

                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7, 1, false, true));
            }


            new BukkitRunnable() {public void run() {
                Logger.log("Removed " + smokeGrenade.getID());

                GenericItemRegistry.powerUpsByPlayer.remove(shooterGP.getUUID(), powerUp);
            }}.runTaskLater(TDM.plugin, 1);
        });
    }
}
