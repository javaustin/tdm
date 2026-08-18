package com.carrotguy69.tdm.utils;

import com.carrotguy69.cxyz.exceptions.InvalidConfigException;
import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.cmd.Print;
import com.carrotguy69.tdm.cmd.Test;
import com.carrotguy69.tdm.cmd.game._GameSupercommand;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.map.GameMap;
import com.carrotguy69.tdm.utils.objects.GlowUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Objects;

import static com.carrotguy69.tdm.TDM.*;

public class Startup {

    public static void loadConfigYMLs() {
        File dataFolder = plugin.getDataFolder();

        // for config.yml
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        configYML = plugin.getConfig();

        // for kits.yml
        File kitsYMLFile = new File(dataFolder, "kits.yml");

        if (!kitsYMLFile.exists()) {
            plugin.saveResource("kits.yml", false);
        }

        kitsYML = YamlConfiguration.loadConfiguration(kitsYMLFile);

        // for maps.yml
        File mapYMLFile = new File(dataFolder, "maps.yml");

        if (!mapYMLFile.exists()) {
            plugin.saveResource("maps.yml", false);
        }

        mapsYML = YamlConfiguration.loadConfiguration(mapYMLFile);


        // for messages.yml
        File messagesYMLFile = new File(dataFolder, "messages.yml");

        if (!messagesYMLFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesYML = YamlConfiguration.loadConfiguration(messagesYMLFile);
    }

    public static void loadConstants() {

        // Load maps to memory
        for (GameMap map : GameMap.getMaps()) {
            gameMaps.put(map.getID(), map);
        }

        autoJoinEnabled = configYML.getBoolean("auto-join.enabled");
        autoJoinScope = AutoJoinScope.fromString(configYML.getString("auto-join.scope"));

        scoreboardsEnabled = configYML.getBoolean("scoreboards.enabled");
        gameScoreboardLines = configYML.getStringList("scoreboards.game");
        lobbyScoreboardLines = configYML.getStringList("scoreboards.lobby");

        respawnSeconds = configYML.getInt("game.respawns.respawn-seconds");

        GenericItemRegistry.loadItems();
        GenericItemRegistry.loadCustomItems();
        GenericItemRegistry.loadGuns();

        GenericItemRegistry.loadKits();


        if (gameMaps.keySet().stream().noneMatch(id -> id.equalsIgnoreCase("lobby"))) {
            throw new InvalidConfigException("maps.yml", "lobby", "Lobby map not found!");
        }
        else {
            // Ensure that lobby map is not being used as game maps.
            GameMap lobbyMap = GameMap.getByID("lobby");
            gameMaps.remove("lobby");

            TDM.lobbyMap = lobbyMap;
        }

        GlowUtils.SCOREBOARD = Bukkit.getScoreboardManager().getMainScoreboard();


        return;
    }

    public static void registerCommands() {
        Objects.requireNonNull(plugin.getCommand("tdm")).setExecutor(_GameSupercommand.executor);
        Objects.requireNonNull(plugin.getCommand("print")).setExecutor(new Print());
        Objects.requireNonNull(plugin.getCommand("tdm")).setTabCompleter(_GameSupercommand.tabCompleter);
        Objects.requireNonNull(plugin.getCommand("test")).setExecutor(new Test());
    }

    public static void registerBukkitEvents() {
        plugin.getServer().getPluginManager().registerEvents((Listener) plugin, plugin);
    }
}
