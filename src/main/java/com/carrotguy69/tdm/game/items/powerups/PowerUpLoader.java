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


        explosiveArrow.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = explosiveArrow.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", explosiveArrow.getCustomName(), "n", "n"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        explosiveArrow.on(ProjectileHitEvent.class, (e, po) -> {


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
            center.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);

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
                GenericItemRegistry.powerUpsByPlayer.remove(shooterGP.getUUID(), explosiveArrow);
            }}.runTaskLater(TDM.plugin, 1);
        });
    }

    private static void loadJumpPack() {
        PowerUp jumpPack = GenericItemRegistry.powerUps.get("jump-pack");

        if (jumpPack == null) {
            return;
        }


        jumpPack.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = jumpPack.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", jumpPack.getCustomName(), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

            p.setAllowFlight(true);
        });

        jumpPack.on(PlayerToggleFlightEvent.class, ((event, powerUp) -> {

            if (event.getPlayer().isFlying()) {
                return;
            }

            event.setCancelled(true);

            Player p = event.getPlayer();

            Game game = Game.getByPlayer(p);

            if (game == null)
                return;

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
                p.setFlying(false);
                return;
            }

            p.getInventory().removeItem(matching);

            p.setVelocity(p.getLocation().getDirection().multiply(2).setY(1));
            p.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
            p.setAllowFlight(false);
            p.setFlying(false);

            game.jumpPackNoFallDamagePlayers.add(p.getUniqueId());

        }));

        jumpPack.on(PlayerMoveEvent.class, ((event, powerUp) -> {

            Player p = event.getPlayer();

            Game game = Game.getByPlayer(p);

            if (game == null)
                return;

            if (p.isOnGround() && game.jumpPackNoFallDamagePlayers.contains(p.getUniqueId())) {
                new BukkitRunnable() {public void run() {
                        GenericItemRegistry.powerUpsByPlayer.remove(p.getUniqueId(), jumpPack);
                        game.jumpPackNoFallDamagePlayers.remove(p.getUniqueId());
                }}.runTaskLater(TDM.plugin, 1);
            }
        }));
    }

    private static void loadSmokeGrenade() {
        PowerUp smokeGrenade = GenericItemRegistry.powerUps.get("smoke-bomb");

        if (smokeGrenade == null) {
            return;
        }

        smokeGrenade.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = smokeGrenade.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", smokeGrenade.getCustomName(), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        smokeGrenade.on(ProjectileHitEvent.class, (e, powerUp) -> {

            Location center = e.getEntity().getLocation();
            e.getEntity().remove();

            Player shooter = (Player) e.getEntity().getShooter();
            Game game = Game.getByPlayer(shooter);

            if (game == null) {
                return;
            }

            GamePlayer shooterGP = game.getPlayer(shooter);

            center.getWorld().spawnParticle(Particle.DUST, center, 1700, 6, 2, 6, new Particle.DustOptions(Color.BLACK, 4));
            center.getWorld().spawnParticle(Particle.DUST, center, 1700, 6, 2, 6, new Particle.DustOptions(Color.GRAY, 4));
            center.getWorld().spawnParticle(Particle.DUST, center, 1700, 6, 2, 6, new Particle.DustOptions(Color.WHITE, 4));
            center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 2.0f);

            for (Player p : center.getWorld().getNearbyPlayers(center, 6, 4, 6)) {

                GamePlayer hitGP = game.getPlayer(p);

                if (hitGP.getTeam().getID().equalsIgnoreCase(shooterGP.getTeam().getID())) {
                    continue;
                }

                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7 * 20, 1, false, true));
            }


            new BukkitRunnable() {public void run() {
                GenericItemRegistry.powerUpsByPlayer.remove(shooterGP.getUUID(), powerUp);
            }}.runTaskLater(TDM.plugin, 1);
        });
    }
}
