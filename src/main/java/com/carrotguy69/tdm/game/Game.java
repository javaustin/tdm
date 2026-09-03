package com.carrotguy69.tdm.game;

import com.carrotguy69.cxyz.CXYZ;
import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.models.config.channel.registry.ChannelFunction;
import com.carrotguy69.cxyz.models.config.channel.registry.ChannelRegistry;
import com.carrotguy69.cxyz.models.db.GameStat;
import com.carrotguy69.cxyz.models.db.NetworkPlayer;
import com.carrotguy69.cxyz.utils.BroadcastUtils;
import com.carrotguy69.cxyz.utils.ColorUtils;
import com.carrotguy69.cxyz.utils.ItemUtils;
import com.carrotguy69.cxyz.utils.NumberRange;
import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.game.items.managers.GunManager;
import com.carrotguy69.tdm.game.items.GenericItem;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import com.carrotguy69.tdm.game.items.classes.GunItem;
import com.carrotguy69.tdm.game.items.powerups.PowerUp;
import com.carrotguy69.tdm.game.items.powerups.PowerUpPickup;
import com.carrotguy69.tdm.game.map.GameMap;
import com.carrotguy69.tdm.game.other.DamageSource;
import com.carrotguy69.tdm.game.other.Durations;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import com.carrotguy69.tdm.utils.Logger;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.carrotguy69.cxyz.CXYZ.f;
import static com.carrotguy69.cxyz.CXYZ.msgYML;
import static com.carrotguy69.cxyz.CXYZ.random;
import static com.carrotguy69.cxyz.messages.MessageUtils.formatPlaceholders;
import static com.carrotguy69.tdm.TDM.configYML;
import static com.carrotguy69.tdm.TDM.gameIDMap;
import static com.carrotguy69.tdm.TDM.gameMaps;
import static com.carrotguy69.tdm.TDM.gameScoreboardLines;
import static com.carrotguy69.tdm.TDM.lobbyMap;
import static com.carrotguy69.tdm.TDM.lobbyScoreboardLines;
import static com.carrotguy69.tdm.TDM.messagesYML;
import static com.carrotguy69.tdm.TDM.plugin;
import static com.carrotguy69.tdm.TDM.scoreboardsEnabled;

import static com.carrotguy69.tdm.messages.TDMMessageKey.COMMAND_DELETE_GAME_FADE_IN_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.COMMAND_DELETE_GAME_FADE_OUT_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.COMMAND_DELETE_GAME_STAY_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.DEATH_RESPAWN_FADE_IN_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.DEATH_RESPAWN_FADE_OUT_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.DEATH_RESPAWN_STAY_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.DEATH_RESPAWN_SUBTITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.DEATH_RESPAWN_TITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.GAME_LEAVE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.INFO_BLURB;
import static com.carrotguy69.tdm.messages.TDMMessageKey.INFO_MID_GAME_JOIN_MESSAGE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOBBY_ALL_READY;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOBBY_COUNTDOWN;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOBBY_LEAVE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOSE_FADE_IN_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOSE_FADE_OUT_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOSE_STAY_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOSE_SUBTITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.LOSE_TITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.MID_GAME_JOIN_FADE_IN_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.MID_GAME_JOIN_FADE_OUT_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.MID_GAME_JOIN_STAY_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.MID_GAME_JOIN_SUBTITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.MID_GAME_JOIN_TITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RECAP_WINNER;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RESPAWN_FADE_IN_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RESPAWN_FADE_OUT_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RESPAWN_MESSAGE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RESPAWN_STAY_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RESPAWN_SUBTITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.RESPAWN_TITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.START_CANCELLED;
import static com.carrotguy69.tdm.messages.TDMMessageKey.TEAM_LIST_DELIMITER;
import static com.carrotguy69.tdm.messages.TDMMessageKey.TEAM_LIST_ENTRY_FORMAT;
import static com.carrotguy69.tdm.messages.TDMMessageKey.TEAM_LIST_MAX_ENTRIES;
import static com.carrotguy69.tdm.messages.TDMMessageKey.TOP_KILLERS_LIST_DELIMITER;
import static com.carrotguy69.tdm.messages.TDMMessageKey.TOP_KILLERS_LIST_ENTRY_FORMAT;
import static com.carrotguy69.tdm.messages.TDMMessageKey.TOP_KILLERS_LIST_MAX_ENTRIES;
import static com.carrotguy69.tdm.messages.TDMMessageKey.WIN_FADE_IN_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.WIN_FADE_OUT_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.WIN_STAY_TICKS;
import static com.carrotguy69.tdm.messages.TDMMessageKey.WIN_SUBTITLE;
import static com.carrotguy69.tdm.messages.TDMMessageKey.WIN_TITLE;

public class Game {

    private GameMap map;

    private final String gameID;
    private GameState gameState;

    private final List<GameTeam> teams;
    private final List<GamePlayer> players;
    private final List<Integer> taskIDs;

    private NumberRange gameCapacity;

    public Durations durations;

    public boolean counting = false;

    private final GameMode defaultGamemode;

    public int killsToWin;

    // Runtime specific variables
    public boolean invulEnabled = true;
    private final Map<GamePlayer, DamageSource> playerLastDamageSourceMap = new Hashtable<>();

    public int originalPlayersSize = 0;
    public int originalTeamsSize = 0;
    public int elapsedSeconds = 0;

    boolean frozen = false;

    // Updated settings (settings that can't be changed in the middle of the script, but will be applied during our transfer method)
    public GameMap nextMap;
    public NumberRange nextCapacity;

    public String defaultKit;

    public List<UUID> noFallDamagePlayers = new ArrayList<>();

    public Game(String id, GameMap map, NumberRange gameCapacity, String defaultKit) {

        this.gameID = id.toLowerCase();

        TDM.gameIDMap.put(gameID, this);

        this.map = map;


        if (map.getID().equalsIgnoreCase("lobby")) {
            throw new RuntimeException("The lobby cannot be used as a game map.");
        }

        map.isInUse = true;

        this.teams = new ArrayList<>();
        this.players = new ArrayList<>();
        this.taskIDs = new ArrayList<>();

        this.gameCapacity = gameCapacity;

        this.durations = new Durations();

        gameState = GameState.WAITING;

        this.defaultGamemode = GameMode.valueOf(configYML.getString("game.misc.default-gamemode", "adventure").toUpperCase());

        nextMap = map;
        nextCapacity = gameCapacity;

        if (defaultKit == null) {
            defaultKit = TDM.defaultKit;
        }
        this.defaultKit = defaultKit;

        initialize();
    }

    public static Game getByID(String id) {
        return gameIDMap.get(id.toLowerCase());
    }

    public static Game getByPlayer(Player p) {
        if (p == null)
            return null;

        for (Map.Entry<String, Game> entry : gameIDMap.entrySet()) {
            if (entry.getValue().getBukkitPlayers().contains(p)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public List<Player> getBukkitPlayers() {
        return this.players.stream().map(GamePlayer::getBukkitPlayer).toList();
    }

    public void spawnArmor(GamePlayer gp) {
        Player p = gp.getBukkitPlayer();

        if (!configYML.getBoolean("game.team.use-armor", true)) {
            return;
        }

        if (gp.getTeam() == null) {
            return;
        }

        Color color = ColorUtils.getAdaptedArmorColor(gp.getTeam().getRGBColor());

        ItemStack helmet = ItemUtils.createColoredLeatherArmor(Material.LEATHER_HELMET, color);
        ItemStack chest = ItemUtils.createColoredLeatherArmor(Material.LEATHER_CHESTPLATE, color);
        ItemStack legs = ItemUtils.createColoredLeatherArmor(Material.LEATHER_LEGGINGS, color);
        ItemStack boots = ItemUtils.createColoredLeatherArmor(Material.LEATHER_BOOTS, color);

        p.getInventory().setHelmet(helmet);
        p.getInventory().setChestplate(chest);
        p.getInventory().setLeggings(legs);
        p.getInventory().setBoots(boots);
    }

    public void createTeams() {
        ConfigurationSection section = configYML.getConfigurationSection("game.teams");

        if (section == null) {
            throw new RuntimeException("No teams are defined in config.yml. Cannot continue!");
        }

        List<String> keys = new ArrayList<>(section.getKeys(false));
        for (int i = 0; i < keys.size(); i++) {

            String key = keys.get(i);

            if (!key.startsWith("team-")) {
                continue;
            }

            String id = section.getString(key + ".id");
            String name = section.getString(key + ".display-name");

            if (id == null) {
                Logger.warning("Couldn't find config value of \"game.teams.%s.id\".".formatted(key));
                id = String.valueOf(i + 1);
            }

            if (name == null) {
                Logger.warning("Couldn't find config value of \"game.teams.%s.display-name\".".formatted(key));
                name = "&aTeam " + (i + 1) + " ";
            }

            int color = ColorUtils.getRGB(name);

            GameTeam team = new GameTeam(this, i, id, name, color, new ArrayList<>());

            this.teams.add(team);
        }
    }

    private void initialize() {

        createTeams();

        if (lobbyMap.isWorldBorderEnabled()) {
            lobbyMap.getWorld().getWorldBorder().setCenter(Math.round(lobbyMap.getBounds().getCenterX()), Math.round(lobbyMap.getBounds().getCenterZ()));
            lobbyMap.getWorld().getWorldBorder().setSize(Math.round(Math.max(lobbyMap.getBounds().getWidthX(), lobbyMap.getBounds().getWidthZ())));
        }
        else {
            lobbyMap.getWorld().getWorldBorder().setSize(1_000_000);
        }


        lobbyMap.getWorld().setSpawnLocation(lobbyMap.getSpawns().getFirst());

        taskIDs.add(
                new BukkitRunnable() {public void run() {
                    if (gameState == GameState.WAITING) {
                        updateScoreboard();
                    }
                    else {
                        this.cancel();
                    }

                }}.runTaskTimer(plugin, 20, 20).getTaskId()
        );

        if (isPlayable()) {
            tryLobbyCountdown();
        }
    }

    public void addPlayer(GamePlayer gp) {

        if (gp == null) {
            return;
        }

        for (Map.Entry<String, Game> entry : gameIDMap.entrySet()) {
            Game game = entry.getValue();

            if (game.getPlayers().contains(gp)) {
                throw new RuntimeException("A player may not be in two games at once!");
            }
        }

        players.add(gp);
        Player p = gp.getBukkitPlayer();

        p.getInventory().clear();

        GameStat tdmLifetimeKills = GameStat.getStat(gp.getUUID(), "tdm-lifetime-kills");
        GameStat tdmLifetimeWins = GameStat.getStat(gp.getUUID(), "tdm-lifetime-wins");

        if (tdmLifetimeKills == null) {
            GameStat.setStat(gp.getUUID(), "tdm-lifetime-kills", "0").sync();
        }

        if (tdmLifetimeWins == null) {
            GameStat.setStat(gp.getUUID(), "tdm-lifetime-wins", "0").sync();
        }

        gp.setTemporaryStat("kills", 0);

//        GlowUtils.resetGlowing(p);

        if (gameState == GameState.WAITING) {
            spawnPlayer(p, lobbyMap.getSpawns().size() > 1 ? lobbyMap.getSpawns().get(new Random().nextInt(0, lobbyMap.getSpawns().size() - 1)) : lobbyMap.getSpawns().getFirst());

            this.announce(
                    MessageGrabber.grab(TDMMessageKey.LOBBY_JOIN),
                    MapFormatters.gamePlayerFormatter(gp),
                    List.of()
            );

            Map<String, Object> commonMap = MapFormatters.gameFormatter(this);
            commonMap.putAll(MapFormatters.gamePlayerFormatter(gp));
            runConfigCommands(configYML.getStringList("game.command-actions.on-lobby"), commonMap);


            if (isPlayable()) {
                tryLobbyCountdown();
            }
        }

        else {
            handleJoinMidGame(gp);
        }

        originalPlayersSize = this.players.size();
        originalTeamsSize = this.teams.size();

        updateScoreboard();
    }

    public void removePlayer(@Nullable GamePlayer gp) {

        if (gp == null) {
            return;
        }

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(gp);

        if (gameState == GameState.WAITING) {
            this.announce(
                    MessageGrabber.grab(LOBBY_LEAVE),
                    commonMap,
                    List.of()
            );
        }

        else {

            this.announce(
                    MessageGrabber.grab(GAME_LEAVE),
                    commonMap,
                    List.of()
            );

            if (gameState == GameState.ACTIVE) {
                // We should eliminate the player just so there is something handling death and a possible forced win (via forfeit).
                kill(gp, false);
            }

            if (gp.getTeam() != null) {
                gp.getTeam().removePlayer(gp);
            }
        }

        closeScoreboard(gp.getBukkitPlayer());

        players.remove(gp);

        originalPlayersSize = this.getPlayers().size();
        originalTeamsSize = this.teams.size();


        updateScoreboard();

        if (!isPlayable() && gameState == GameState.ACTIVE) {
            GameTeam lead = getLeadingTeam();

            if (lead != null) {
                win(lead);
            }
        }

        GenericItemRegistry.powerUpsByPlayer.removeAll(gp.getUUID());
        noFallDamagePlayers.clear();
    }

    public List<GamePlayer> getPlayers() {
        return players;
    }

    private void spawnPlayer(Player p, Location l) {
        p.closeInventory();
        p.getInventory().clear(); // only for tdm
        p.setFireTicks(0);
        p.setGameMode(defaultGamemode);
        p.setAllowFlight(false);
        p.setFlying(false);
        Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20.0); // The "official" (non-deprecated) way to set max health?
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.teleport(l.clone().add(0.5, 1, 0.5));
    }

    private void prepInventory(GamePlayer gp) {
        spawnArmor(gp);
        GenericItemRegistry.spawnKit(gp.getBukkitPlayer(), GenericItemRegistry.getKit(gp.kit));
    }

    public String getGameID() {
        return gameID;
    }

    public GameMap getGameMap() {
        return this.map;
    }

    public boolean isPlayable() {
        // A game is playable unless all players belong to the same team, or unless there are fewer players than twice the minimum game capacity.
        if (players.size() < gameCapacity.min().intValue()) {
            return false;
        }

        GameTeam firstTeam = null;

        for (GamePlayer gp : players) {
            GameTeam team = gp.getTeam();

            if (team == null) {
                return true;
            }

            if (firstTeam == null) {
                firstTeam = team;
                continue;
            }

            if (!team.getDisplayName().equalsIgnoreCase(firstTeam.getDisplayName())) {
                return true;
            }
        }

        return false;
    }

    public void announce(String unparsedContent, Map<String, Object> formatMap, List<GamePlayer> excludingPlayers) {
        announce(unparsedContent, formatMap, excludingPlayers, null);
    }

    public void announce(String unparsedContent, Map<String, Object> formatMap, List<GamePlayer> excludingPlayers, @Nullable NetworkPlayer sender) {
        for (GamePlayer gp : this.players) {
            NetworkPlayer np = NetworkPlayer.resolvePlayer(gp.getUUID());

            if (gp.getBukkitPlayer() == null) {
                continue;
            }

            if (np != null && sender != null) {
                if (np.isIgnoring(sender)) {
                    continue;
                }

                if (np.isMutingChannel(ChannelRegistry.getChannelByFunction(ChannelFunction.PUBLIC))) {
                    continue;
                }
            }

            if (excludingPlayers.contains(gp))
                continue;

            Player p = gp.getBukkitPlayer();

            p.sendMessage(MessageUtils.createMessage(unparsedContent, formatMap));
        }
    }

    public void updateScoreboard() {

        if (!scoreboardsEnabled) {
            return;
        }

        Map<String, Object> ogCommonMap = MapFormatters.gameFormatter(this);

        for (GamePlayer gp : players) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

            List<String> scoreboardLines;

            if (gameState == GameState.ACTIVE || gameState == GameState.STARTING) {
                scoreboardLines = gameScoreboardLines;
            }
            else {
                scoreboardLines = lobbyScoreboardLines;
            }

            if (scoreboardLines.isEmpty()) {
                scoreboardLines.add("Sample text");
            }

            Map<String, Object> commonMap = new HashMap<>(Map.copyOf(ogCommonMap));
            commonMap.putAll(MapFormatters.gamePlayerFormatter(gp));


            Objective objective = scoreboard.registerNewObjective("sidebar", Criteria.DUMMY, f(formatPlaceholders(scoreboardLines.getFirst(), commonMap)));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);


            for (int i = 1; i < scoreboardLines.size(); i++) {
                objective.getScore(f(formatPlaceholders(scoreboardLines.get(i), commonMap))).setScore(Math.abs(scoreboardLines.size() - i));
            }

            gp.getBukkitPlayer().setScoreboard(scoreboard);
        }
    }

    public void closeScoreboard() {
        if (!scoreboardsEnabled) {
            return;
        }

        for (Player p : getBukkitPlayers()) {
            Scoreboard scoreboard = p.getScoreboard();
            Objective obj = scoreboard.getObjective("sidebar");
            if (obj != null) {
                obj.unregister();
            }
        }
    }

    public void closeScoreboard(Player p) {
        Scoreboard scoreboard = p.getScoreboard();
        Objective obj = scoreboard.getObjective("sidebar");
        if (obj != null) {
            obj.unregister();
        }
    }

    private void handleJoinMidGame(GamePlayer gp) {
        Player p = gp.getBukkitPlayer();

        if (gp.getTeam() == null) {
            assignTeam(gp);
        }

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(gp);

        // Message
        MessageUtils.sendParsedMessage(
                p,
                MessageGrabber.grab(INFO_MID_GAME_JOIN_MESSAGE),
                Map.of()
        );

        final int[] respawnSeconds = {TDM.respawnSeconds};

        new BukkitRunnable() {public void run() {

            if (respawnSeconds[0] <= 0) {
                respawn(gp);

                this.cancel();
                return;
            }

            commonMap.put("count", respawnSeconds[0]);

            BroadcastUtils.sendTitle(
                    List.of(p),
                    formatPlaceholders(MessageGrabber.grab(MID_GAME_JOIN_TITLE), commonMap),
                    formatPlaceholders(MessageGrabber.grab(MID_GAME_JOIN_SUBTITLE), commonMap),
                    messagesYML.getInt(MID_GAME_JOIN_FADE_IN_TICKS.getPath(), 0),
                    messagesYML.getInt(MID_GAME_JOIN_STAY_TICKS.getPath(), 40),
                    messagesYML.getInt(MID_GAME_JOIN_FADE_OUT_TICKS.getPath(), 20)
            );

            respawnSeconds[0] -= 1;
        }}.runTaskTimer(plugin, 0, 20);

        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
    }

    public void assignTeam(GamePlayer gp, GameTeam team) {
        if (team.getPlayers().size() == this.getPlayers().size() - 1 && this.getPlayers().size() != 1) {
            throw new RuntimeException(String.format("Denied adding player %s to team because it would cause only one team to have players.", gp.getNetworkPlayer().getUsername()));
        }

        team.addPlayer(gp);
        gp.setTeam(team); // IMPORTANT: update the GamePlayer object so it knows what team it is a part of
    }

    public GameTeam assignTeam(GamePlayer gp) {
        /*
        Primarily used as a "last resort", when the player does not self-assign.
        Adds a player to a team and returns that GameTeam.

        Assign player to:
        0. A non-full team
        1. the team with the least players
        2. the team with the least matchmaking score
        */

        // This function relies on (takes for granted) the following conditions:
        assert teams != null;
        assert !teams.isEmpty();
        assert teams.size() >= 2;

        if (gp.getTeam() != null) {
            return gp.getTeam();
        }

        GameTeam chosenTeam = null;
        for (GameTeam team : teams) {

            if (getPlayers().size() == team.getPlayers().size() - 1) {
                continue;
            }

            if (chosenTeam == null) {
                chosenTeam = team;
                continue;
            }

            if (team.getPlayers().size() < chosenTeam.getPlayers().size()) {
                chosenTeam = team;
                continue;
            }

            if (team.matchmakingScore < chosenTeam.matchmakingScore) {
                chosenTeam = team;
                continue;
            }

        }

        if (chosenTeam == null) {
            throw new RuntimeException(String.format("All teams in game %s are full", gameID));
        }

        chosenTeam.addPlayer(gp);
        gp.setTeam(chosenTeam);

        return chosenTeam;
    }

    public @Nullable GamePlayer getViableSpawn(GamePlayer gp) {
        // Use for spawns mid-game.
        // Gets the most viable spawn for a player by ensuring the player is teleported

        if (gp.getTeam() == null) {
            throw new RuntimeException("Cannot spawn player " + gp.getNetworkPlayer().getUsername() + " to a viable teammate because their team is null");
        }

        List<GamePlayer> mostViable = gp
                .getTeam()
                .getPlayers()                     // Must be on the same team
                .stream()
                .filter(player -> !player.equals(gp)) // Cannot return the same player
                .filter(GamePlayer::isAlive) // Cannot be a dead player
                .sorted(
                        Comparator.comparingDouble(
                                player -> player.getBukkitPlayer().getHealth()
                        )
                )
                .toList()
                .reversed()
                ;

        if (!mostViable.isEmpty()) {
            return mostViable.getFirst();
        }

        return null;
    }

    public void respawn(@NotNull GamePlayer gp) {

        if (gameState != GameState.ACTIVE && gameState != GameState.ENDING) {
            return;
        }

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(gp);

        GamePlayer viableSpawnPartner = getViableSpawn(gp);

        // Find a spawn preferably with another teammate (with the most health), otherwise use a random spawn.
        Location spawn = viableSpawnPartner != null ? viableSpawnPartner.getBukkitPlayer().getLocation() : map.getSpawns().get(random.nextInt(0, map.getSpawns().size() - 1));

        spawnPlayer(gp.getBukkitPlayer(), spawn);

        prepInventory(gp);

        BroadcastUtils.sendTitle(
                List.of(gp.getBukkitPlayer()),
                formatPlaceholders(MessageGrabber.grab(RESPAWN_TITLE), commonMap),
                formatPlaceholders(MessageGrabber.grab(RESPAWN_SUBTITLE), commonMap),
                messagesYML.getInt(RESPAWN_FADE_IN_TICKS.getPath(), 0),
                messagesYML.getInt(RESPAWN_STAY_TICKS.getPath(), 40),
                messagesYML.getInt(RESPAWN_FADE_OUT_TICKS.getPath(), 20)
        );
        gp.getBukkitPlayer().playSound(gp.getBukkitPlayer().getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.6f, 2.0f);

        gp.setAlive(true);

        MessageUtils.sendParsedMessage(
                gp.getBukkitPlayer(),
                MessageGrabber.grab(RESPAWN_MESSAGE),
                commonMap
        );

    }

    private void tryLobbyCountdown() {
        /*
        Lobby countdown:
        - gs == WAITING
        - Players are at the lobby spawn point
        - Teams are not assigned and switching is allowed
        - Leaving may cancel the countdown if it leaves an insufficient amount of players
        - If a lobby countdown finalizes (without breaking), it triggers the start() method which triggers the game countdown.
         */

        // Will return if there is already a countdown in progress.
        if (counting) {
            return;
        }

        int id = new BukkitRunnable() {public void run() {
            updateScoreboard();

            if (frozen) {
                return;
            }

            counting = true;


            if (!isPlayable()) {

                announce(MessageGrabber.grab(START_CANCELLED), Map.of(), List.of());
                BroadcastUtils.playSound(getBukkitPlayers(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);

                counting = false;

                restartCurrentCountdown();
                updateScoreboard();

                this.cancel();

            }

            else if (durations.lobbyCountdown > 0 && !players.stream().allMatch(GamePlayer::isReady)) {
                if (durations.lobbyCountdown <= 5 || durations.lobbyCountdown == getNextEventTimeMaxSeconds()) {
                    announce(MessageGrabber.grab(LOBBY_COUNTDOWN), Map.of("count", durations.lobbyCountdown), List.of());
                    BroadcastUtils.playSound(getBukkitPlayers(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.8f, 1.0f);
                }
                durations.lobbyCountdown -= 1;
            }

            else {
                if (players.stream().allMatch(GamePlayer::isReady)) {
                    // announce what happened
                    announce(MessageGrabber.grab(LOBBY_ALL_READY), Map.of(), List.of());
                }

                this.cancel();
                counting = false;
                durations.lobbyCountdown = -1;
                prep();
            }

        }}.runTaskTimer(TDM.plugin, 0, 20).getTaskId();

        taskIDs.add(id);
    }

    public void restartCurrentCountdown() {
        String cur = getNextEventName();

        switch (cur) {
            case "Game start":
                durations.lobbyCountdown = configYML.getInt("timers.lobby-countdown", 10);
                break;
            case "PVP on":
                durations.gameStartCountdown = configYML.getInt("timers.game-countdown", 10);
                durations.gameEndCountdown = configYML.getInt("timers.game-end", 360);
                break;
            case "Game end":
                durations.gameEndCountdown = configYML.getInt("timers.game-end", 360);
                break;
            default:
                return;
        }
    }

    public String getNextEventName() {
        if (durations.lobbyCountdown >= 0) {
            // This signifies the teleport from the lobby to the arena
            return "Game start";
        }

        if (durations.gameStartCountdown >= 0) {
            return "PVP on";
        }

        return "Game end";
    }

    public int getNextEventTimeSeconds() {
        if (durations.lobbyCountdown >= 0) {
            // This signifies the teleport from the lobby to the arena
            return durations.lobbyCountdown;
        }

        if (durations.gameStartCountdown >= 0) {
            return durations.gameStartCountdown;
        }

        return durations.gameEndCountdown;
    }

    public int getNextEventTimeMaxSeconds() {

        if (durations.lobbyCountdown >= 0) {
            // This signifies the teleport from the lobby to the arena
            return configYML.getInt("timers.lobby-countdown", 10);
        }

        if (durations.gameStartCountdown >= 0) {
            return configYML.getInt("timers.game-countdown", 10);
        }

        return configYML.getInt("timers.game-end", 360);
    }

    private void prep() {

        originalPlayersSize = this.players.size();
        originalTeamsSize = this.teams.size();

        for (GamePlayer gp : players) {
            assignTeam(gp);
        }

        teams.removeIf(GameTeam::isEmpty);
        recalcTeamIndexes();

        for (Player p : getBukkitPlayers()) {
            p.getInventory().clear();
        }
        spawnTeams();

        gameState = GameState.STARTING;

        removeGroundItems();

        if (map.isWorldBorderEnabled()) {
            map.getWorld().getWorldBorder().setCenter(map.getBounds().getCenterX() + 0.5, map.getBounds().getCenterZ() + 0.5);
            map.getWorld().getWorldBorder().setSize(Math.max(map.getBounds().getWidthX() + 0.5, map.getBounds().getWidthZ()) + 0.5);
        }
        else {
            lobbyMap.getWorld().getWorldBorder().setSize(1_000_000);
        }

        killsToWin = (players.size() % 2) == 0 ? 5 * players.size() : (players.size() - 1) * 5;

        Map<String, Object> commonMap = MapFormatters.gameFormatter(this);
        runConfigCommands(configYML.getStringList("game.command-actions.on-prep"), commonMap);

        tryGameCountdown();

        // To remove pickups from previous games that failed to delete.
        for (Location powerUpSpawn : map.getPowerUpSpawns()) {
            powerUpSpawn.getWorld().loadChunk(powerUpSpawn.getChunk());
            for (Entity entity : powerUpSpawn.getNearbyEntities(5, 5, 5)) {
                if (entity.getType() == EntityType.ARMOR_STAND) {
                    entity.remove();
                }
            }
        }
        PowerUpPickup.activePickupLocations.clear();
    }

    private void recalcTeamIndexes() {
        for (int i = 0; i < teams.size(); i++) {
            GameTeam team = teams.get(i);
            team.setIndex(i);
        }
    }

    private void spawnTeams() {
        // Teleport all teams to their respective spawn point.

        for (int i = 0; i < teams.size(); i++) {
            GameTeam team = teams.get(i);

            int spawnIndex = (int) Math.ceil(((double) i / (double) teams.size()) * map.getSpawns().size());

            for (GamePlayer gp : team.getPlayers()) {
                Player p = gp.getBukkitPlayer();

                spawnPlayer(p, map.getSpawns().get(spawnIndex));
                prepInventory(gp);

//                GlowUtils.setGlowing(p, gp.getTeam().getRGBColor());
            }
        }
    }

    private void removeGroundItems() {
        for (Item entity : map.getWorld().getEntitiesByClass(Item.class)) {
            entity.remove();
        }
    }

    private void tryGameCountdown() {
        /*
        Game countdown:
        - gs == STARTING
        - Players have been teleported to their respective spawn points
        - Teams are final and unswitchable (a new joining player *may* still be able to join a team but not by their choosing)
        - Leaving may cancel the countdown if the game is left with an insufficient amount of players
        - Leaving will count as an elimination
        - If a game countdown finalizes (w/out breaking), the game will start
        */

        int id = new BukkitRunnable() {public void run() {

            updateScoreboard();

            if (frozen) {
                return;
            }

            if (!isPlayable()) {

                cancelStart();
                this.cancel();
            }


            else if (durations.gameStartCountdown > 0) {
                // Not really in the mood to expose this to the config. We will keep this countdown as a hard coded title.
                String color = switch (durations.gameStartCountdown) {
                    case 3 -> "&c&l";
                    case 2 -> "&6&l";
                    case 1 -> "&e&l";
                    default -> "&c";
                };

                List<Player> gamePlayers = players.stream().map(g -> Bukkit.getPlayer(g.getUUID())).toList();
                BroadcastUtils.sendTitle(
                        gamePlayers,
                        color + durations.gameStartCountdown,
                        "",
                        0,
                        20,
                        40
                );
                for (Player p : gamePlayers) {
                    p.playSound(p, Sound.UI_BUTTON_CLICK, 0.7F, 1F);
                }
                durations.gameStartCountdown -= 1;
            }

            else { // count == 0
                List<Player> gamePlayers = players.stream().map(g -> Bukkit.getPlayer(g.getUUID())).toList();

                BroadcastUtils.sendTitle(
                        gamePlayers,
                        "&a&lGO",
                        "",
                        0,
                        20,
                        40
                );

                for (Player p : gamePlayers) {
                    p.playSound(p, Sound.ENTITY_WITHER_SPAWN, 0.7F, 1F);
                }
                durations.gameStartCountdown = -1;

                this.cancel();
                start();
            }

        }}.runTaskTimer(TDM.plugin, 0, 20).getTaskId();

        taskIDs.add(id);
    }

    private void cancelStart() {

        gameState = GameState.WAITING;

        List<Player> players = this.getBukkitPlayers();

        int nSpawns = lobbyMap.getSpawns().size();
        int nPlayers = players.size();

        if (lobbyMap.isWorldBorderEnabled()) {
            lobbyMap.getWorld().getWorldBorder().setCenter(Math.round(lobbyMap.getBounds().getCenterX()), Math.round(lobbyMap.getBounds().getCenterZ()));
            lobbyMap.getWorld().getWorldBorder().setSize(Math.round(Math.max(lobbyMap.getBounds().getWidthX(), lobbyMap.getBounds().getWidthZ())));
        }
        else {
            lobbyMap.getWorld().getWorldBorder().setSize(lobbyMap.getWorld().getWorldBorder().getMaxSize());
        }

        lobbyMap.getWorld().setSpawnLocation(lobbyMap.getSpawns().getFirst());

        // For loop spawns all players throughout all spawns repeating some spawns if nPlayers > nSpawns
        for (int i = 0; i < nPlayers; i++) {
            int j = (i < nSpawns) ? i : (i % nSpawns);

            spawnPlayer(players.get(i), lobbyMap.getSpawns().get(j));
        }

        tryLobbyCountdown();
    }

    private void start() {

        gameState = GameState.ACTIVE;
        Map<String, Object> commonMap = MapFormatters.gameFormatter(this);

        // Send info blurb on game start
        announce(MessageGrabber.grab(INFO_BLURB), commonMap, List.of());

        invulEnabled = false;


        for (GamePlayer gp : this.getPlayers()) {
            // command actions for on-start
            Map<String, Object> newCommonMap = new HashMap<>();
            newCommonMap.putAll(commonMap);
            newCommonMap.putAll(MapFormatters.gamePlayerFormatter(gp));
            runConfigCommands(configYML.getStringList("game.command-actions.on-start"), newCommonMap);

        }

        long despawnTicks = configYML.getLong("game.power-ups.despawn-ticks");
        int maxAmount = configYML.getInt("game.power-ups.max");

        spawnPowerUps(maxAmount, despawnTicks);

        taskIDs.add(
                new BukkitRunnable() {public void run() {
                    if (gameState != GameState.ACTIVE) {
                        return;
                    }

                    for (PowerUpPickup pickup : new ArrayList<>(PowerUpPickup.activePickupLocations)) {
                        for (Player nearby : pickup.getLocation().getNearbyPlayers(1)) {
                            pickup.applyTo(getPlayer(nearby));

                            PowerUpPickup toDelete = PowerUpPickup.getNearby(pickup.getLocation());

                            if (toDelete == null) {
                                Logger.warning("Failed to delete applied powerup because it was not found! " + pickup.getID() + " at" + pickup.getLocation());
                                continue;
                            }

                            toDelete.despawn();
                            break;
                        }
                    }
                }}.runTaskTimer(plugin, 0L, 2L).getTaskId()
        );

        // Anything in this task runs every second
        taskIDs.add(

                new BukkitRunnable() {public void run() {
                    if (!frozen) {
                        elapsedSeconds += 1;
                    }

                    updateScoreboard();
                }}.runTaskTimer(plugin, 20, 20).getTaskId()
        );


        // Game end timer
        taskIDs.add(
                new BukkitRunnable() {public void run() {
                    if (frozen) {
                        return;
                    }

                    if (durations.gameEndCountdown > 0) {
                        durations.gameEndCountdown -= 1;
                    }

                    else {
                        durations.gameEndCountdown = 0;
                        if (gameState != GameState.ENDING) {
                            forceWin();
                        }
                        this.cancel();
                    }

                }}.runTaskTimer(TDM.plugin, 0, 20).getTaskId()
        );
    }

    public void forceWin() {
        /*
         1. Choose a team based on total damage dealt (store this as a team stat)
         2. Quietly eliminate all other teams (set team to not alive and set all players into spectator mode)
         3. Teleport all spectators to a member of the winning team
         */

        GameTeam winner = getLeadingTeam();

        for (GameTeam gt : teams) {
            if (Objects.equals(gt, winner))
                continue;

            if (gt.getPlayers().isEmpty())
                continue;

            for (GamePlayer gp : gt.getPlayers()) {
                resetLastDamageSource(gp);
                kill(gp, false);
            }
        }

        if (winner != null) {
            win(winner);
        }

    }

    public void resetLastDamageSource(GamePlayer gp) {
        this.playerLastDamageSourceMap.remove(gp);
    }


    public void setLastDamageSource(GamePlayer player, DamageSource source) {
        // The playerLastDamageSourceMap map is structured Map<GamePlayer, DamageSource>, where DamageSource is (@Nullable GamePlayer attacker, DamageSource.Reason reason).
        // Entries are to expire within 10 seconds (200 ticks). If a player dies within 10 seconds of being attacked by another player, the kill is attributed to the attacker, and not suicide.

        // DamageSource can contain a player if the type is MELEE, PROJECTILE, or EXPLOSIVE. The provided player is allowed to be themselves.


        this.playerLastDamageSourceMap.put(player, source);

        long expireTicks = 10 * 20L;
        new BukkitRunnable() {public void run() {
            playerLastDamageSourceMap.remove(player);
        }}.runTaskLater(plugin, expireTicks);
    }

    public void kill(@NotNull GamePlayer player, boolean tryRespawn) {

        if (!player.isAlive()) {
            return;
        }

        player.setAlive(false);
        player.getBukkitPlayer().setGameMode(GameMode.SPECTATOR);

        GunManager.removeGuns(player.getBukkitPlayer());
        player.getBukkitPlayer().getInventory().clear();

        DamageSource lastDamageSource = playerLastDamageSourceMap.get(player);

        if (lastDamageSource == null || lastDamageSource.isAttackerSelf(player)) {
            // A player shouldn't get kill credit if they kill themselves.
            lastDamageSource = new DamageSource(null, DamageSource.Reason.NATURAL);
        }

        GamePlayer attacker = lastDamageSource.attacker();

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(player);

        // If an attacker exists, update the common map and send kill messages to the attacker first.
        if (attacker != null) {
            commonMap.putAll(MapFormatters.cloneFormaterToNewKey(MapFormatters.gamePlayerFormatter(attacker), "player", "attacker"));
        }

        // Announce death to game
        announce(
                MessageGrabber.grab(TDMMessageKey.valueOf("DEATH_ANNOUNCEMENT_" + lastDamageSource.reason().name().toUpperCase())),
                commonMap,
                List.of()
        );

        // Send death message to the player who died
        MessageUtils.sendParsedMessage(
                player.getBukkitPlayer(),
                MessageGrabber.grab(TDMMessageKey.valueOf("DEATH_MESSAGE_" + lastDamageSource.reason().name().toUpperCase())),
                commonMap
        );


        final int[] respawnSeconds = {TDM.respawnSeconds};

        if (tryRespawn) {
            int taskID = new BukkitRunnable() {
                public void run() {

                    if (gameState != GameState.ACTIVE) {
                        this.cancel();
                        return;
                    }

                    if (respawnSeconds[0] <= 0) {
                        respawn(player);

                        this.cancel();
                        return;
                    }

                    commonMap.put("count", respawnSeconds[0]);

                    BroadcastUtils.sendTitle(
                            List.of(player.getBukkitPlayer()),
                            formatPlaceholders(MessageGrabber.grab(DEATH_RESPAWN_TITLE), commonMap),
                            formatPlaceholders(MessageGrabber.grab(DEATH_RESPAWN_SUBTITLE), commonMap),
                            messagesYML.getInt(DEATH_RESPAWN_FADE_IN_TICKS.getPath(), 0),
                            messagesYML.getInt(DEATH_RESPAWN_STAY_TICKS.getPath(), 40),
                            messagesYML.getInt(DEATH_RESPAWN_FADE_OUT_TICKS.getPath(), 20)
                    );

                    respawnSeconds[0] -= 1;
                }
            }.runTaskTimer(plugin, 0, 20).getTaskId();

            taskIDs.add(taskID);
        }

        if (attacker != null) {

            double teamKills = attacker.getTeam().getStat("kills", 0);
            attacker.getTeam().setStat("kills", teamKills + 1);

            double kills = attacker.getTemporaryStat("kills", 0);
            attacker.setTemporaryStat("kills", kills + 1);

            attacker.getBukkitPlayer().playSound(attacker.getBukkitPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);


            GameStat attackerLifetimeKills = GameStat.getStat(attacker.getUUID(), "tdm-lifetime-kills");


            GameStat.setStat(attacker.getUUID(), "tdm-lifetime-kills", attackerLifetimeKills != null ? String.valueOf(Integer.parseInt(attackerLifetimeKills.getValue()) + 1) : "1")
                    .sync();

            replenish(attacker);

            MessageUtils.sendParsedMessage(
                    player.getBukkitPlayer(),
                    MessageGrabber.grab(TDMMessageKey.valueOf("KILL_MESSAGE_" + lastDamageSource.reason().name().toUpperCase())),
                    commonMap
            );
        }

        // Run config-defined command actions for 'on-kill' and 'on-death' YAML keys:
        runConfigCommands(configYML.getStringList("game.command-actions.on-kill"), commonMap);
        runConfigCommands(configYML.getStringList("game.command-actions.on-death"), commonMap);


        // Teleport player to center of map on death (after the config commands run).
        player.getBukkitPlayer().teleport(map.getBounds().getCenter().toLocation(map.getWorld()));

        // Why do this after the config commands run?
        // Because I want to allow commands to control kill effects. So, we need to expose the location. We can do this by allowing the player
        // entity to act as the location. (e.g.: /summon lightning {player})

        if (isWon()) {
            GameTeam lead = getLeadingTeam();

            if (lead != null) {
                // the opposite of this condition should be impossible
                win(lead);
            }
        }
    }

    public boolean isWon() {
        GameTeam lead = getLeadingTeam();

        return lead != null && lead.getStat("kills", 0) >= killsToWin;
    }

    public @Nullable GameTeam getLeadingTeam() {

        GameTeam current = teams.getFirst();

        for (GameTeam team : teams) {
            if (team.getPlayers().isEmpty())
                continue;

            if (team.getStat("kills", 0) > current.getStat("kills", 0)) {
                current = team;
            }
        }

        return current;
    }

    public GameTeam getNonLeadingTeam() {
        return teams.stream().filter(gt -> !(gt == getLeadingTeam())).findAny().orElse(null);
    }

    private void replenish(GamePlayer gp) {
        // We can replenish either with an arrow or ammo.

        Player p = gp.getBukkitPlayer();
        PlayerInventory inv = p.getInventory();

        ItemStack hand = inv.getItemInMainHand();

        if (hand.getType().name().contains("BOW")) {
            // Get the arrow defined in config.

            GenericItem item = GenericItemRegistry.getItemByID("arrow");

            // attempt to respect the preferred slot from kits.yml
            int preferredSlot = -10;

            for (Map.Entry<Integer, GenericItem> entry : GenericItemRegistry.getKit(gp.kit).entrySet()) {
                if (entry.getValue().getID().equalsIgnoreCase(item.getID())) {

                    if (inv.containsAtLeast(new ItemStack(Material.ARROW), entry.getValue().toItemStack().getAmount())) {
                        // Don't allow players to stack more arrows than they deserve.
                        return;
                    }

                    preferredSlot = entry.getKey();
                }
            }

            if (item == null) {
                replenish(gp, new ItemStack(Material.ARROW), preferredSlot);
                return;
            }

            replenish(gp, item.toItemStack(), preferredSlot);
            return;
        }

        String gunID = GunManager.getGunID(hand);

        if (gunID == null) {
            return;
        }

        GunItem gunInHand = GunManager.getByID(gunID);

        if (gunInHand == null) {
            return;
        }

        CustomItem ammo = CustomItem.getByType(CustomItem.Type.AMMO);

        ItemStack ammoStack = ammo.toItemStack();

        // attempt to respect the preferred slot from kits.yml
        int preferredSlot = -10;

        for (Map.Entry<Integer, GenericItem> entry : GenericItemRegistry.getKit(defaultKit).entrySet()) {
            if (entry.getValue().getID().equalsIgnoreCase(ammo.getID())) {
                preferredSlot = entry.getKey();
            }
        }

        if (ammo.getAmount() == 1) {
            ammoStack.setAmount(gunInHand.getMagSize());
        }

        replenish(gp, ammoStack, preferredSlot);
    }

    private void replenish(GamePlayer attacker, ItemStack stack, int preferredSlot) {
        PlayerInventory inv = attacker.getBukkitPlayer().getInventory();

        if (stack == null) {
            return;
        }

        ItemStack currentSlot = null;

        if (preferredSlot != -10) {

            if (preferredSlot >= 0) {
                currentSlot = inv.getContents()[preferredSlot];
            }

            if (currentSlot == null) {
                ItemUtils.setItem(inv, stack, preferredSlot);
            }

            else if (currentSlot.isSimilar(stack) && currentSlot.getAmount() + stack.getAmount() <= currentSlot.getMaxStackSize()) {
                stack.setAmount(stack.getAmount() + currentSlot.getAmount());
                ItemUtils.setItem(inv, stack, preferredSlot);
            }

            else
                inv.addItem(stack);
        }

        else
            inv.addItem(stack);
    }

    public void win(GameTeam winningTeam) {
        gameState = GameState.ENDING;
        cancelAllTasks();

        invulEnabled = true;

        PowerUpPickup.despawnAll();

        // Send victory title for winners
        List<Player> winnerBukkitPlayers = winningTeam.getPlayers().stream().map(GamePlayer::getBukkitPlayer).toList();

        for (Player p : winnerBukkitPlayers) {
            GameStat attackerLifetimeKills = GameStat.getStat(p.getUniqueId(), "tdm-lifetime-wins");

            GameStat.setStat(p.getUniqueId(), "tdm-lifetime-wins", attackerLifetimeKills != null ? String.valueOf(Integer.parseInt(attackerLifetimeKills.getValue()) + 1) : "1")
                    .sync();
        }

        BroadcastUtils.playSound(winnerBukkitPlayers, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        BroadcastUtils.sendTitle(
                winnerBukkitPlayers,
                MessageGrabber.grab(WIN_TITLE),
                MessageGrabber.grab(WIN_SUBTITLE),
                messagesYML.getInt(WIN_FADE_IN_TICKS.getPath(), 0),
                messagesYML.getInt(WIN_STAY_TICKS.getPath(), 40),
                messagesYML.getInt(WIN_FADE_OUT_TICKS.getPath(), 20)
        );

        // Send game over title for losers

        List<GamePlayer> loserPlayers = players.stream().filter(gp -> !winningTeam.getPlayers().contains(gp)).toList();
        List<Player> loserBukkitPlayers = loserPlayers.stream().map(GamePlayer::getBukkitPlayer).toList();

        BroadcastUtils.sendTitle(
                loserBukkitPlayers,
                MessageGrabber.grab(LOSE_TITLE),
                MessageGrabber.grab(LOSE_SUBTITLE),
                messagesYML.getInt(LOSE_FADE_IN_TICKS.getPath(), 0),
                messagesYML.getInt(LOSE_STAY_TICKS.getPath(), 40),
                messagesYML.getInt(LOSE_FADE_OUT_TICKS.getPath(), 20)
        );


        sendRecap(winningTeam);

        Location destination;

        if (winningTeam.getPlayers().isEmpty()) {
            destination = map.getBounds().getCenter().toLocation(map.getWorld());
        }

        else {
            destination = winningTeam.getPlayers().getFirst().getBukkitPlayer().getLocation();
        }

        Map<String, Object> commonMap = MapFormatters.gameFormatter(this);

        for (GamePlayer gp : loserPlayers) {
            gp.getBukkitPlayer().teleport(destination);

            Map<String, Object> newCommonMap = MapFormatters.gamePlayerFormatter(gp);
            newCommonMap.putAll(commonMap);
            runConfigCommands(configYML.getStringList("game.command-actions.on-lose"), newCommonMap);
        }

        int rgb = winningTeam.getRGBColor();

        doFireworks(winnerBukkitPlayers, Color.fromRGB(rgb));

        // Covers both solo winner and team winner cases
        for (GamePlayer gp : winningTeam.getPlayers()) {
            Map<String, Object> newCommonMap = MapFormatters.gamePlayerFormatter(gp);
            newCommonMap.putAll(commonMap);
            runConfigCommands(configYML.getStringList("game.command-actions.on-win"), commonMap);
        }

        new BukkitRunnable(){
            public void run() {
                cancelAllTasks();
                gameState = GameState.RESET;
                Game newGame = transfer();
            }
        }.runTaskLater(plugin, 7 * 20L);

        updateScoreboard();
    }

    public static void runConfigCommands(List<String> commandLines, Map<String, Object> commonMap) {
        for (String line : commandLines) {
            String formattedLine = formatPlaceholders(line, commonMap);

            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedLine);
            }
            catch (CommandException ignore) {

            }
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<GameTeam> getTeams() {
        return teams;
    }

    public void cancelAllTasks() {
        for (Integer taskID : taskIDs) {
            Bukkit.getScheduler().cancelTask(taskID);
        }

        for (GamePlayer gp : players) {
            if (gp.compassTask != null) {
                gp.compassTask.cancel();
            }
        }
    }

    private void sendRecap(GameTeam winningTeam) {

        Map<String, Object> commonMap = MapFormatters.gameFormatter(this);

        commonMap.put("game-id", gameID);

        String unparsed = MessageGrabber.grab(RECAP_WINNER);

        // Fulfill {team-members}
        Pair<String, Map<String, Object>> pair1 = getTeamMembersText(winningTeam);

        String teamMembersText = pair1.getLeft();
        commonMap.putAll(pair1.getRight());

        unparsed = unparsed.replace("{winner-team-members}", teamMembersText);

        // Fulfill {top-killers}
        Pair<String, Map<String, Object>> pair2 = getTopKillersText();

        String topKillersText = pair2.getLeft();
        commonMap.putAll(pair2.getRight());

        unparsed = unparsed.replace("{top-killers}", topKillersText);

        announce(unparsed, commonMap, List.of());
    }

    private Pair<String, Map<String, Object>> getTopKillersText() {

        players.sort(Comparator.comparingDouble(gp -> gp.getTemporaryStat("kills", 0)));


        com.carrotguy69.cxyz.messages.utils.MapFormatters.NumberedListFormatter topKillsFormatter = MapFormatters.gamePlayerNumberedListFormatter(
                players.reversed(),
                MessageGrabber.grab(TOP_KILLERS_LIST_ENTRY_FORMAT) != null ? MessageGrabber.grab(TOP_KILLERS_LIST_ENTRY_FORMAT) : "{player}",
                MessageGrabber.grab(TOP_KILLERS_LIST_DELIMITER) != null ? MessageGrabber.grab(TOP_KILLERS_LIST_DELIMITER) : "\n{i}.) ",
                messagesYML.getInt(TOP_KILLERS_LIST_MAX_ENTRIES.getPath(), 9999),
                1
        );

        // Creating a new class for this result would be too much abstraction, and secondly I am too lazy.
        return Pair.of(topKillsFormatter.generatePage(1), topKillsFormatter.getFormatMap());
    }

    private static Pair<String, Map<String, Object>> getTeamMembersText(GameTeam winnerTeam) {

        if (winnerTeam.getPlayers().isEmpty()) {
            Logger.severe("The winning team has no players therefore a list of players could not be generated. This would have thrown an exception in CXYZ if not caught here!");
            Logger.severe(winnerTeam.toString());
            return Pair.of("", Map.of());
        }

        com.carrotguy69.cxyz.messages.utils.MapFormatters.ListFormatter playerFormatter = MapFormatters.gamePlayerListFormatter(
                winnerTeam.getPlayers(),
                MessageGrabber.grab(TEAM_LIST_ENTRY_FORMAT) != null ? MessageGrabber.grab(TEAM_LIST_ENTRY_FORMAT) : "{player}",
                MessageGrabber.grab(TEAM_LIST_DELIMITER) != null ? MessageGrabber.grab(TEAM_LIST_DELIMITER) : ",",
                messagesYML.getInt(TEAM_LIST_MAX_ENTRIES.getPath(), 9999),
                1
        );

        return Pair.of(playerFormatter.generatePage(1), playerFormatter.getFormatMap());
    }

    private void doFireworks(List<Player> targets, Color color) {
        int id = new BukkitRunnable() {public void run() {
            for (Player p : targets) {
                Firework fw = p.getWorld().spawn(p.getLocation(), Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();


                meta.addEffect(FireworkEffect.builder().withColor(color).trail(true).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
                new BukkitRunnable() {
                    public void run() {
                        fw.detonate();
                    }
                }.runTaskLater(plugin, 20L);
            }

        }}.runTaskTimer(plugin, 0L, 20L).getTaskId();

        this.taskIDs.add(id);
    }

    public Game transfer() {

        if (gameIDMap.get(this.gameID) == null) {
            // Game was deleted by an admin. Do not auto transfer.
            return null;
        }

        map.isInUse = false;

        GameMap newMap = nextMap.equals(map) ? gameMaps.values().stream().filter(m -> !Objects.equals(m, map)).findAny().orElse(map) : nextMap;
        String nextKit = this.defaultKit;

        List<GamePlayer> keepPlayers = this.players;

        this.delete(true);
        Game newGame = new Game(this.gameID, newMap, nextCapacity, nextKit);
        newGame.frozen = this.frozen;

        new BukkitRunnable() {public void run() {
            for (GamePlayer gp : keepPlayers) {
                GamePlayer newGp = new GamePlayer(gp.getUUID());
                newGp.kit = gp.kit; // we want to persist their kit

                newGame.addPlayer(newGp);
            }
        }}.runTaskLater(CXYZ.plugin, 2L);

        return newGame;
    }

    public void delete(boolean isTransfer) {
        // Send players to lobby and cancel tasks

        this.cancelAllTasks();

        List<Player> players = this.getBukkitPlayers();

        int nSpawns = lobbyMap.getSpawns().size();
        int nPlayers = players.size();


        // stolen from the initialization script
        if (lobbyMap.isWorldBorderEnabled()) {
            lobbyMap.getWorld().getWorldBorder().setCenter(Math.round(lobbyMap.getBounds().getCenterX()), Math.round(lobbyMap.getBounds().getCenterZ()));
            lobbyMap.getWorld().getWorldBorder().setSize(Math.round(Math.max(lobbyMap.getBounds().getWidthX(), lobbyMap.getBounds().getWidthZ())));
        }
        else {
            lobbyMap.getWorld().getWorldBorder().setSize(lobbyMap.getWorld().getWorldBorder().getMaxSize());
        }

        lobbyMap.getWorld().setSpawnLocation(lobbyMap.getSpawns().getFirst());


        for (int i = 0; i < nPlayers; i++) {
            int j = (i < nSpawns) ? i : (i % nSpawns);

            players.get(i).getInventory().clear();

            spawnPlayer(players.get(i), lobbyMap.getSpawns().get(j));
        }

        if (!isTransfer) {
            BroadcastUtils.sendTitle(
                    getBukkitPlayers(),
                    MessageGrabber.grab(TDMMessageKey.COMMAND_DELETE_GAME_TITLE),
                    MessageGrabber.grab(TDMMessageKey.COMMAND_DELETE_GAME_SUBTITLE),
                    msgYML.getInt(COMMAND_DELETE_GAME_FADE_IN_TICKS.getPath(), 0),
                    msgYML.getInt(COMMAND_DELETE_GAME_STAY_TICKS.getPath(), 40),
                    msgYML.getInt(COMMAND_DELETE_GAME_FADE_OUT_TICKS.getPath(), 20)
            );
        }

        closeScoreboard();

        gameIDMap.remove(this.gameID, this);
    }

    public GamePlayer getPlayer(Player p) {
        for (GamePlayer gp : players) {
            if (gp.getUUID() == p.getUniqueId()) {
                return gp;
            }
        }

        return null;
    }

    public boolean damageWithGun(Player victim, Player attacker, double damage) {
        // return true if we determine the damage should be applied

        if (victim.getGameMode() != defaultGamemode)
            return false;

        Game game = Game.getByPlayer(victim);

        if (game == null)
            return false;

        GamePlayer gp = game.getPlayer(victim);

        if (Game.getByPlayer(attacker) == null || game != Game.getByPlayer(attacker)) {
            return false;
        }

        GamePlayer shooter = game.getPlayer(attacker);

        if (shooter.equals(gp)) {
            return false;
        }

        if (shooter.getTeam().equals(gp.getTeam())) {
            return false;
        }

        if (game.gameState != GameState.ACTIVE) {
            return false;
        }


        setLastDamageSource(gp, new DamageSource(shooter, DamageSource.Reason.PROJECTILE)); // Set our own damage source map.

        victim.damage(damage);
        return true;
    }

    public GameTeam getTeamByName(String name) {
        for (GameTeam team : teams) {
            if (team.getID().strip().equalsIgnoreCase(name)) {
                return team;
            }
        }

        return null;
    }

    public void spawnPowerUps(int amount, long despawnTicks) {
        List<PowerUp> powerUps = new ArrayList<>(GenericItemRegistry.powerUps.values());
        List<Location> powerUpSpawns = map.getPowerUpSpawns();

        amount = Math.min(amount, Math.min(map.getPowerUpSpawns().size(), powerUps.size()));

        if (powerUpSpawns.isEmpty()) {
            return;
        }

        for (int i = 0; i < amount; i++) {
            PowerUp powerUp = powerUps.get(i);

            Location spawnLocation = powerUpSpawns.size() - 1 > 0 ? powerUpSpawns.get(random.nextInt(0, powerUpSpawns.size())) : powerUpSpawns.getFirst();

            PowerUpPickup pickup = new PowerUpPickup(powerUp.getOriginalItem(), spawnLocation);
            pickup.spawn();

            taskIDs.add(new BukkitRunnable() {public void run() {
                if (gameState != GameState.ACTIVE) {
                    return;
                }

                pickup.despawn();

                spawnPowerUps(1, despawnTicks);
            }}.runTaskLater(plugin, despawnTicks).getTaskId());
        }

    }

    public void freeze(GamePlayer admin) {
        this.frozen = !this.frozen;

        announce(MessageGrabber.grab(TDMMessageKey.valueOf("GAME_" + (this.frozen ? "FREEZE" : "UNFREEZE") + "_ANNOUNCEMENT")), MapFormatters.gamePlayerFormatter(admin), List.of());
    }

    public void setGameMap(GameMap map) {
        this.map = map;
    }

    public NumberRange getGameCapacity() {
        return gameCapacity;
    }

    public void setGameCapacity(NumberRange gameCapacity) {
        this.gameCapacity = gameCapacity;
    }

    public String getDefaultKit() {
        return defaultKit;
    }

    public void setDefaultKit(String kitID) {
        this.defaultKit = kitID;
    }

    @Override
    public String toString() {
        return "Game{"
                + "gameID=" + gameID + ","
                + "teams=" + teams.stream().map(GameTeam::getDisplayName).toList() + ","
                + "players=" + players.stream().map(GamePlayer::getNetworkPlayer).map(NetworkPlayer::getUsername).toList() + ","
                + "taskIDs(size)=" + taskIDs.size() + ","
                + "capacity=" + gameCapacity.toPrettyString() + ","
                + "gameState=" + gameState.name() + ","
                + "defaultGamemode=" + defaultGamemode.name() +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Game && ((Game) obj).getGameID().equals(this.gameID);
    }
}
