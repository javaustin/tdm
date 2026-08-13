package com.carrotguy69.tdm.game;

import com.carrotguy69.cxyz.models.db.NetworkPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GamePlayer {
    private final UUID uuid;
    private boolean alive;
    private GameTeam team;
    private final Map<String, Double> stats = new HashMap<>();
    private boolean ready = false;
    public BukkitTask compassTask = null;
    public String kit;


    public GamePlayer(UUID uuid) {
        this.uuid = uuid;
        this.alive = true;
    }

    public boolean isReady() {
        return this.ready;
    }

    public void setReady(boolean value) {
        this.ready = value;
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public NetworkPlayer getNetworkPlayer() {
        return NetworkPlayer.resolvePlayer(this.uuid);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public GameTeam getTeam() {
        return this.team;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setTeam(GameTeam team) {
        this.team = team;
    }

    public Map<String, Double> getTemporaryStat() {
        return stats;
    }

    public double getTemporaryStat(String key, double def) {
        return stats.getOrDefault(key, def);
    }

    public void setTemporaryStat(String key, double value) {
        stats.put(key, value);
    }

    public Game getGame() {
        return Game.getByPlayer(this.getBukkitPlayer());
    }

    public void setGlowing() {
        // team add {player}
        // team join {player}
        // team modify {player} color


        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = scoreboardManager.getMainScoreboard();

        Team scoreboardTeam = scoreboard.getTeam(String.valueOf(getTeam().getRGBColor()));

        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(String.valueOf(getTeam().getRGBColor()));
            scoreboardTeam.color(NamedTextColor.nearestTo(TextColor.color(getTeam().getRGBColor())));
        }

        scoreboardTeam.color(NamedTextColor.nearestTo(TextColor.color(getTeam().getRGBColor())));
        scoreboardTeam.addEntry(getBukkitPlayer().getName());
    }

    @Override
    public String toString() {
        return "GamePlayer{"
                + "uuid=" + uuid + ","
                + "name=" + NetworkPlayer.resolvePlayer(uuid).getDisplayName() + ","
                + "team=" + (team != null ? team.getID() : null) + ","
                + "alive=" + alive + ","
                +
                "}";
    }

    @Override
    public boolean equals(Object otherPlayer) {
        return otherPlayer instanceof GamePlayer && ((GamePlayer) otherPlayer).getUUID() == this.uuid;
    }
}
