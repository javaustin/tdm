package com.carrotguy69.tdm;

import com.carrotguy69.cxyz.events.custom.PublicChatEvent;
import com.carrotguy69.cxyz.events.custom.VanishToggleEvent;
import com.carrotguy69.cxyz.events.custom.base.Priority;
import com.carrotguy69.cxyz.events.custom.service.EventService;
import com.carrotguy69.cxyz.utils.NumberRange;
import com.carrotguy69.tdm.cmd.game.Create;
import com.carrotguy69.tdm.eventHandler.CoreChatHandler;
import com.carrotguy69.tdm.eventHandler.VanishHandler;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.managers.GunManager;
import com.carrotguy69.tdm.game.items.powerups.PowerUp;
import com.carrotguy69.tdm.game.map.GameMap;
import com.carrotguy69.tdm.game.other.DamageSource;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import com.carrotguy69.tdm.utils.Logger;
import com.carrotguy69.tdm.utils.Startup;
import org.bukkit.GameMode;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

public final class TDM extends JavaPlugin implements Listener {

    /*
    TODO:
        - compass is tripping


        - make sure same team players cant damage eachother
            - Powerup(location, [consumer] action)
            ideas:
            - explosive arrow (creates explosion and changes blocks to nether and fire [temporarily, make sure to restore even if server shuts down])
            - jump pack (slime block, launches player into the air in a direction to escape or trickshot)
            - smoke grenade (blinds players within radius and puts particles)
            - full health pickup (custom splash potion that heals all teammates (and self) in radius)
            - a wither/blaze/flying thing that attacks enemy players (easy to kill but flying)
            - temporary (5 second (or until 20 damage is dealt OR explosion) physical shield item that blocks all attacks - use an action bar timer
            - grappling hook
            - ortify (slowness 2 and resistance 2 effects) - use an armor icon and activate thru right click
            - homing missle (dont need to hold right click) take 2 seconds to lock and then shoot an instakill firework at them
            - fish bomb (spawns a lot salmon/carp/whatever they are [remove after 5 seconds])
            - simple ammo pickup
            - team buff (grant all players gapple effects)
            - orbital strike/carpet bomb (dont know how to summon) - mark the circular area with red particles, and blow everything up (physical tnt should drop and ignite as soon as it hits the floor
            - explosions:
            - throw blocks into the air
            - can set (only completely solid) blocks to be corrupter or light the tops on fire (make sure fire spread is off)
            - we should use the smokey particles (whatever they were) from the last TDM minigame for explosions
        - glow players
        - since gun data is based on the player, you shouldnt be able to drop your gun to another player

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

    public static boolean autoJoinEnabled;
    public static AutoJoinScope autoJoinScope;

    public static String defaultKit;

    public static List<UUID> noInteractionTicks = new ArrayList<>();

    public enum AutoJoinScope {
        SERVER,
        WORLD;
        public static AutoJoinScope fromString(@Nullable String s) {
            return s != null && s.toUpperCase().equals(SERVER.name()) ? SERVER : WORLD;
        }
    }



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
    public void onJoin(PlayerJoinEvent e) {
        if (!autoJoinEnabled) {
            return;
        }

        if (!(autoJoinScope == AutoJoinScope.SERVER || (autoJoinScope == AutoJoinScope.WORLD && e.getPlayer().getWorld().equals(GameMap.getMaps().getFirst().getWorld())))) {
            return;
        }

        Game game;
        try {
            game = TDM.gameIDMap.values().stream().max(Comparator.comparingInt(g -> g.getPlayers().size())).stream().findFirst().orElseThrow();
        }
        catch (NoSuchElementException ex) {
            game = new Game(
                    Create.generateValidGameID(),
                    gameMaps.size() - 1 > 0
                            ? new ArrayList<>(gameMaps.values()).get(new Random().nextInt(0, gameMaps.size() - 1))
                            : new ArrayList<>(gameMaps.values()).getFirst(),
                    new NumberRange(2, 32),
                    null
            );
        }

        GamePlayer gamePlayer = new GamePlayer(e.getPlayer().getUniqueId());
        gamePlayer.kit = game.defaultKit;
        game.addPlayer(gamePlayer);
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

        if (cause == EntityDamageEvent.DamageCause.FALL && game.jumpPackNoFallDamagePlayers.contains(p.getUniqueId())) {
            game.jumpPackNoFallDamagePlayers.remove(p.getUniqueId());
            e.setCancelled(true);
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

        else if (attackerGP.getTeam().equals(gp.getTeam())) {
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
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        p.teleport(game.getGameMap().getSpawns().getFirst());
        p.setRespawnLocation(game.getGameMap().getSpawns().getFirst());
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
        Player p = e.getPlayer();

        // PowerUp hook
        Collection<PowerUp> powerUps = GenericItemRegistry.powerUpsByPlayer.get(p.getUniqueId());
        for (PowerUp powerUp : powerUps) {
            powerUp.handleEvent(e);
        }

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();

        if (noInteractionTicks.contains(p.getUniqueId())) {
            return;
        }

        else {
            noInteractionTicks.add(p.getUniqueId());

            new BukkitRunnable() {public void run() {
                noInteractionTicks.remove(p.getUniqueId());
            }}.runTaskLater(this, 1);
        }

        ConfigurationSection section = configYML.getConfigurationSection("game.click-actions");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase().replace("-", "_"));
                    String actionTypeString = section.getString(key + ".click-type", "ANY");

                    if (!e.getAction().name().startsWith(actionTypeString.toUpperCase().replace("-", "_")) && !actionTypeString.equalsIgnoreCase("ANY") || material != hand.getType()) {
                        continue;
                    }

                    List<String> actions = section.getStringList(key + ".actions");

                    Game.runConfigCommands(actions, MapFormatters.gamePlayerFormatter(game.getPlayer(p)));
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


        GunManager.handleClick(p, e.getAction());
    }

    @EventHandler
    public void onInventory(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();


        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        if (game.getGameState() == GameState.WAITING && p.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onInventory(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();

        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        if (game.getGameState() == GameState.WAITING && p.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();


        Game game = Game.getByPlayer(p);

        if (game == null) {
            return;
        }

        if (p.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            noInteractionTicks.add(e.getPlayer().getUniqueId());

            new BukkitRunnable() {public void run() {
                noInteractionTicks.remove(e.getPlayer().getUniqueId());
            }}.runTaskLater(this, 1);
            return;
        }
    }


    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {

        Projectile projectile = e.getEntity();

        if (projectile.getShooter() == null || !(projectile.getShooter() instanceof Player p)) {
            return;
        }

        // If a player is found in ANY Bukkit event, be sure to respect any possible PowerUp hooks.
        Collection<PowerUp> powerUps = GenericItemRegistry.powerUpsByPlayer.get(p.getUniqueId());
        for (PowerUp powerUp : powerUps) {
            powerUp.handleEvent(e);
        }

        if (!(projectile instanceof Arrow)) {
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

    @EventHandler
    public void onFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();

        Collection<PowerUp> powerUps = GenericItemRegistry.powerUpsByPlayer.get(p.getUniqueId());
        for (PowerUp powerUp : powerUps) {
            powerUp.handleEvent(e);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        Collection<PowerUp> powerUps = GenericItemRegistry.powerUpsByPlayer.get(p.getUniqueId());
        for (PowerUp powerUp : powerUps) {
            powerUp.handleEvent(e);
        }
    }


}
