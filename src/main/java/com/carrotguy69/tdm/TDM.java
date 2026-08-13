package com.carrotguy69.tdm;

import com.carrotguy69.cxyz.events.custom.PublicChatEvent;
import com.carrotguy69.cxyz.events.custom.VanishToggleEvent;
import com.carrotguy69.cxyz.events.custom.base.Priority;
import com.carrotguy69.cxyz.events.custom.service.EventService;
import com.carrotguy69.tdm.eventHandler.CoreChatHandler;
import com.carrotguy69.tdm.eventHandler.VanishHandler;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.game.items.managers.GunManager;
import com.carrotguy69.tdm.game.map.GameMap;
import com.carrotguy69.tdm.game.other.DamageSource;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import com.carrotguy69.tdm.utils.Logger;
import com.carrotguy69.tdm.utils.Startup;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TDM extends JavaPlugin implements Listener {

    /*
    TODO:
        - once you come up with at least 4 unique powerups then add them
        - glow players
        - test map manager
    */

    public static JavaPlugin plugin;
    public static FileConfiguration configYML;
    public static FileConfiguration kitsYML;
    public static FileConfiguration mapsYML;
    public static FileConfiguration messagesYML;

    public static GameMap lobbyMap;

    public static Map<String, Game> gameIDMap = new HashMap<>();

    public static Map<String, GameMap> gameMaps = new HashMap<>();

    public static boolean scoreboardsEnabled;
    public static List<String> gameScoreboardLines;
    public static List<String> lobbyScoreboardLines;

    public static int respawnSeconds;


    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = JavaPlugin.getPlugin(TDM.class);

        Startup.loadConfigYMLs();
        Startup.loadConstants();
        Startup.registerCommands();
        Startup.registerBukkitEvents();


        EventService.registerHandler(PublicChatEvent.class, new CoreChatHandler(), Priority.NORMAL);
        EventService.registerHandler(VanishToggleEvent.class, new VanishHandler(), Priority.NORMAL);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        Logger.info("See ya later!");
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        Game game = Game.getByPlayer(p);

        if (game != null) {
            GamePlayer gp = game.getPlayer(p);
            game.removePlayer(gp);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        // traditionally this handler is only used for natural damages such as fall damage.
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        GamePlayer gp = game.getPlayer(p);

        if (game.invulEnabled) {
            e.setCancelled(true);
            return;
        }

        EntityDamageEvent.DamageCause cause = e.getCause();

        if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            e.setCancelled(true);
            return;
        }

        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK || cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            return;
        }

        double damageTaken = gp.getTemporaryStat("damage-taken", 0.0);
        gp.setTemporaryStat("damage-taken", damageTaken + e.getFinalDamage());

        double hp = p.getHealth() - e.getFinalDamage();

        if (hp <= 0) {
            e.setCancelled(true);
            game.kill(gp, true);
        }
    }

    @EventHandler
    public void onDamageByPlayer(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }

        Entity attackerEntity = e.getDamager();
        Player attacker = null;
        DamageSource.Reason reason = null;

        if (attackerEntity.getType() == EntityType.PLAYER) {
            assert attackerEntity instanceof Player;
            attacker = (Player) attackerEntity;
            reason = DamageSource.Reason.MELEE;
        }

        else if (attackerEntity instanceof Projectile projectile) {
            assert attackerEntity instanceof Arrow;

            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
                reason = DamageSource.Reason.PROJECTILE;
            }
        }

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        if (game.invulEnabled)
            return;

        GamePlayer gp = game.getPlayer(p); // The above check ensures that the game player is not null (because the player is sourced from a game)
        GamePlayer attackerGP = null;

        if (attacker != null) {
            attackerGP = game.getPlayer(attacker);
        }

        if (attackerGP == null) { // attacker was outside the game
            e.setCancelled(true);
        }

        else {
            DamageSource source = new DamageSource(attackerGP, reason);
            game.setLastDamageSource(gp, source);

            double damageTaken = gp.getTemporaryStat("damage-taken", 0.0);
            gp.setTemporaryStat("damage-taken", damageTaken + e.getFinalDamage());

            double damageDealt = gp.getTemporaryStat("damage-dealt", 0.0);
            attackerGP.setTemporaryStat("damage-dealt", damageDealt + e.getFinalDamage());
        }

        double hp = p.getHealth() - e.getFinalDamage();
        if (hp <= 0) {
            e.setCancelled(true);
            game.kill(gp, true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        // Do not allow hunger change when game is not active (keep them fed until game time)

        Player p = (Player) e.getEntity();
        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        e.setFoodLevel(20);
    }

    @EventHandler
    public void onPearl(PlayerTeleportEvent e) {

        // Easiest way to cancel pearl damage is to cancel the pearl event and teleport the player ourselves (and play the pearl sound).

        Player p = e.getPlayer();

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        if (game.getGameState() != GameState.ACTIVE) {
            return;
        }

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            p.teleport(e.getTo());
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1.0f, 1.0f);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Game game = Game.getByPlayer(e.getPlayer());

        if (game == null) {
            return;
        }

        e.getPlayer().teleport(game.getGameMap().getSpawns().getFirst());
        e.getPlayer().setRespawnLocation(game.getGameMap().getSpawns().getFirst());
    }

    @EventHandler
    public void onContainerOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        if ((e.getInventory().getType() != InventoryType.PLAYER && e.getInventory().getType() != InventoryType.CHEST)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Game game = Game.getByPlayer(e.getPlayer());

        if (game == null) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        ConfigurationSection section = configYML.getConfigurationSection("game.click-actions");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase().replace("-", "_"));
                    String actionTypeString = section.getString(key + ".click-type", "RIGHT_CLICK");

                    if (!e.getAction().name().startsWith(actionTypeString.toUpperCase().replace("-", "_")) || material != hand.getType()) {
                        continue;
                    }

                    List<String> actions = section.getStringList(key + ".actions");

                    Game.runConfigCommands(actions, MapFormatters.gamePlayerFormatter(game.getPlayer(e.getPlayer())));
                }
                catch (IllegalArgumentException ex) {
                    Logger.log("Failed to run click action command because %s is not a valid item!".formatted(key));
                }
            }

        }



        if (hand.getType() == Material.BOW) {
            // Instead of cancelling, let's set the bow to un-usable.

            if (game.getGameState() != GameState.ACTIVE) {
                e.setUseItemInHand(Event.Result.DENY);
            }
            return;
        }


        GunManager.handleClick(e.getPlayer(), e.getAction());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();

        if (!(projectile instanceof Arrow arrow)) {
            return;
        }

        if (arrow.getShooter() == null || !(arrow.getShooter() instanceof Player p)) {
            return;
        }

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        e.getEntity().remove();
    }

    @EventHandler
    public void onDurability(PlayerItemDamageEvent e) {
        Game game = Game.getByPlayer(e.getPlayer());

        if (game == null) {
            return;
        }

        e.setCancelled(true);
    }


}
