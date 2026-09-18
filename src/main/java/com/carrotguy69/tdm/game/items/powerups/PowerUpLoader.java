package com.carrotguy69.tdm.game.items.powerups;

import com.carrotguy69.cxyz.cmd.ChatColor;
import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.utils.BroadcastUtils;
import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameTeam;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import com.carrotguy69.tdm.utils.Logger;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.carrotguy69.cxyz.CXYZ.f;

public class PowerUpLoader {


    public static void load() {
        loadExplosiveArrow();
        loadJumpPack();
        loadSmokeGrenade();
        loadHealthPickup();
        loadGrappleHook();
        loadTeamBuff();
        loadFishBomb();
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

            center.getWorld().spawn(center, TNTPrimed.class, tnt -> {
                tnt.setFuseTicks(0);
                tnt.setSource(shooterGP.getBukkitPlayer());
            });

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

        jumpPack.on(PlayerInteractEvent.class, ((event, powerUp) -> {
            if (event.getAction().isRightClick() && event.getPlayer().getInventory().getItemInMainHand().getType() == jumpPack.getOriginalItem().getMaterial()) {
                event.setCancelled(true);
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

            if (!e.getEntity().getType().equals(EntityType.SNOWBALL)) {
                return;
            }

            GamePlayer shooterGP = game.getPlayer(shooter);

            center.getWorld().spawnParticle(Particle.DUST, center, 2500, 6, 2, 6, new Particle.DustOptions(Color.BLACK, 4));
            center.getWorld().spawnParticle(Particle.DUST, center, 2500, 6, 2, 6, new Particle.DustOptions(Color.GRAY, 4));
            center.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 2.0f);

            for (Player p : center.getWorld().getNearbyPlayers(center, 6, 4, 6)) {

                GamePlayer hitGP = game.getPlayer(p);

                if (hitGP.getTeam().getID().equalsIgnoreCase(shooterGP.getTeam().getID())) {
                    continue;
                }

                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, false, true));
            }


            new BukkitRunnable() {public void run() {
                GenericItemRegistry.powerUpsByPlayer.remove(shooterGP.getUUID(), powerUp);
            }}.runTaskLater(TDM.plugin, 1);
        });
    }

    private static void loadHealthPickup() {
        PowerUp healthPickup = GenericItemRegistry.powerUps.get("health-pickup");

        if (healthPickup == null) {
            return;
        }

        healthPickup.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = healthPickup.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", healthPickup.getCustomName(), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        healthPickup.on(PlayerInteractEvent.class, ((event, powerUp) -> {

            Player p = event.getPlayer();

            Game game = Game.getByPlayer(p);

            if (game == null) {
                return;
            }
            if (!event.getAction().isRightClick()) {
                return;
            }

            ItemStack hand = p.getInventory().getItemInMainHand();
            ItemMeta handMeta = hand.getItemMeta();

            if (handMeta == null) {
                return;
            }
            if (!handMeta.getDisplayName().equalsIgnoreCase(f(healthPickup.getCustomName()))) {
                return;
            }

            AttributeInstance attr = p.getAttribute(Attribute.MAX_HEALTH);

            double max = attr != null ? attr.getValue() : 20.0;

            p.setHealth(max);
            p.setFoodLevel(20);

            MessageUtils.sendActionBar(p, f("&dYou've been healed!"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);

            p.getInventory().removeItem(hand);

            new BukkitRunnable() {public void run() {
                GenericItemRegistry.powerUpsByPlayer.remove(p.getUniqueId(), powerUp);
            }}.runTaskLater(TDM.plugin, 1);
        }));
    }

    private static void loadGrappleHook() {
        PowerUp grappleHook = GenericItemRegistry.powerUps.get("grapple-hook");

        if (grappleHook == null) {
            return;
        }

        grappleHook.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            p.getInventory().addItem(grappleHook.toItemStack());

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", grappleHook.getCustomName(), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        grappleHook.on(PlayerFishEvent.class, (event, powerUp) -> {
            Player p = event.getPlayer();

            Game game = Game.getByPlayer(p);

            if (game == null) {
                return;
            }

            event.getHook().setVelocity(p.getLocation().getDirection().multiply(1.5));

            Logger.log(event.getState().name());

            if (event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == PlayerFishEvent.State.IN_GROUND) {
                p.setVelocity(event.getHook().getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(3.0));

                p.playSound(p, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 2.0f);
                MessageUtils.sendActionBar(p, f("&dWhoosh!"));
            }
        });
    }

    private static void loadTeamBuff() {
        PowerUp teamBuff = GenericItemRegistry.powerUps.get("team-buff");

        if (teamBuff == null) {
            return;
        }

        teamBuff.setPickupAction(gp -> {

            GameTeam gpTeam = gp.getTeam();

            List<Player> players = gpTeam.getPlayers().stream().map(GamePlayer::getBukkitPlayer).toList();

            for (Player p : players) {
                p.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(120 * 20, 0));
                p.addPotionEffect(PotionEffectType.INSTANT_HEALTH.createEffect(0, 0));
                p.addPotionEffect(PotionEffectType.REGENERATION.createEffect(5 * 20, 1));
                p.setFoodLevel(20);

                MessageUtils.sendParsedMessage(p, "{player-team-color}{player} picked up %s!".formatted(teamBuff.getCustomName()), MapFormatters.gamePlayerFormatter(gp));
            }
            BroadcastUtils.playSound(players, Sound.ENTITY_PLAYER_BURP, 1, 1.5f);
        });
    }

    private static void loadFishBomb() {
        PowerUp fishBomb = GenericItemRegistry.powerUps.get("fish-bomb");

        if (fishBomb == null)
            return;

        fishBomb.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            p.getInventory().addItem(fishBomb.toItemStack());

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", fishBomb.getCustomName(), "n", ""));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        fishBomb.on(PotionSplashEvent.class, (e, powerUp) -> {
            Player p = (Player) e.getPotion().getShooter();

            Location center = e.getPotion().getLocation();

            List<EntityType> types = List.of(EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.TROPICAL_FISH);
            for (int i = 0; i < 500; i++) {

                EntityType type = types.get(new Random().nextInt(types.size()));
                Class<? extends Entity> clazz = type.getEntityClass();

                if (clazz == null) {
                    throw new RuntimeException(String.format("Failed to spawn a fish (could not get class from EntityType %s)", type));
                }

                center.getWorld().spawn(center, clazz, fish -> {
                    fish.setInvulnerable(true);
                    fish.setCustomName(f(new ArrayList<>(ChatColor.reverseMap.keySet()).get(new Random().nextInt(ChatColor.reverseMap.size())) + "Fishhhh"));

                    new BukkitRunnable() {public void run() {
                        fish.remove();
                    }}.runTaskLater(TDM.plugin, new Random().nextInt(7) * 20L);
                });
            }

        });
    }


}
