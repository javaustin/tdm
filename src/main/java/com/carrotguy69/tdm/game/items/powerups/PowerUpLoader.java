package com.carrotguy69.tdm.game.items.powerups;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.utils.BroadcastUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.other.DamageSource;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.utils.Logger;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

import static com.carrotguy69.cxyz.CXYZ.f;
import static com.carrotguy69.cxyz.CXYZ.plugin;

public class PowerUpLoader {

    public static void load() {

        PowerUp explosiveArrow = GenericItemRegistry.powerUps.get("explosive-arrow");

        explosiveArrow.setPickupAction(gp -> {
            Player p = gp.getBukkitPlayer();

            ItemStack itemStack = explosiveArrow.getOriginalItem().toItemStack();

            p.getInventory().addItem(itemStack);

            MessageUtils.sendParsedMessage(p, MessageGrabber.grab(TDMMessageKey.POWER_UP_PICKUP), Map.of("display-name", f(explosiveArrow.getCustomName())));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0f, 1.0f);
        });

        explosiveArrow.on(ProjectileHitEvent.class, (e, powerUp) -> {
            Location center = e.getEntity().getLocation();
            e.getEntity().remove();

            Player shooter = (Player) e.getEntity().getShooter();
            Game game = Game.getByPlayer(shooter);

            if (game == null) {
                return;
            }

            Logger.log("Entity name: " + e.getEntity().getName());
            Logger.log("PowerUp Item name: " + f(powerUp.getCustomName()));

            if (!e.getEntity().getName().equals(f(powerUp.getCustomName()))) {
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

                Logger.log(p.getName() + String.format(" is %s blocks away from the center.", distance));

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

            // for now it is commented bc i want to test it better

//            new BukkitRunnable() {
//                public void run() {
//                    // Avoid a concurrent modification exception
//                    GenericItemRegistry.powerUpsByPlayer.remove(shooterGP.getUUID(), explosiveArrow);
//                }
//            }.runTaskLater(plugin, 1);
        });

    }

}
