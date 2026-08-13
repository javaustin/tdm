package com.carrotguy69.tdm.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.models.db.GameStat;
import com.carrotguy69.tdm.utils.Logger;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTeam {

    private final Game game;
    private int index;
    private final String id;
    private final String name;
    private final int rgbColor;
    private final List<GamePlayer> players;

    // Matchmaking scores (if the score is lower, then the team is underbalanced and needs more players)
    public double matchmakingScore = 0;

    private final Map<String, Double> stats = new HashMap<>();

    public GameTeam(@NotNull Game game, int index, @NotNull String id, @NotNull String name, int rgbColor, List<GamePlayer> players) {
        this.game = game;
        this.index = index;
        this.id = id;
        this.name = name;
        this.rgbColor = rgbColor;
        this.players = players;
    }

    public int getScore() {
        return (int) getStat("kills", 0);
    }

    public void setScore(int score) {
        setStat("kills", score);
    }

    public Game getGame() {
        return this.game;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int i) {
        this.index = i;
    }

    public String getDisplayName() {
        return this.name;
    }

    public String getID() {
        return this.id;
    }

    public int getRGBColor() {
        return this.rgbColor;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public double getCombinedHealth() {
        double hp = 0;

        for (GamePlayer gp : players) {
            hp += gp.getBukkitPlayer().getHealth();
        }

        return hp;
    }

    public List<GamePlayer> getPlayers() {
        return this.players;
    }

    public void addPlayer(GamePlayer gp) {

        GameStat playerLifetimeKills = GameStat.getStat(gp.getUUID(), "tdm-lifetime-kills");

        if (playerLifetimeKills != null) {
            this.matchmakingScore += Integer.parseInt(playerLifetimeKills.getValue());
        }

        GameStat playerLifetimeWins = GameStat.getStat(gp.getUUID(), "tdm-lifetime-wins");

        if (playerLifetimeKills != null) {
            // Scale by x0.5 (wins are a less significant skill indicator than kills)
            this.matchmakingScore += ((double) Integer.parseInt(playerLifetimeWins.getValue()) / 2);
        }

        this.players.add(gp);
    }

    public void removePlayer(GamePlayer gp) {

        GameStat playerLifetimeKills = GameStat.getStat(gp.getUUID(), "tdm-lifetime-kills");

        if (playerLifetimeKills != null) {
            this.matchmakingScore -= Integer.parseInt(playerLifetimeKills.getValue());
        }

        GameStat playerLifetimeWins = GameStat.getStat(gp.getUUID(), "tdm-lifetime-wins");

        if (playerLifetimeKills != null) {
            // Scale by x0.5 (wins are a less significant skill indicator than kills)
            this.matchmakingScore -= ((double) Integer.parseInt(playerLifetimeWins.getValue()) / 2);
        }

        this.players.remove(gp);
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public double getStat(String key, double def) {
        return stats.getOrDefault(key, def);
    }

    public void setStat(String key, double value) {
        stats.put(key, value);
    }

    public void sendTeamMessage(String unparsedContent, Map<String, Object> formatMap, List<GamePlayer> excludingPlayers) {
        TextComponent component = MessageUtils.createMessage(unparsedContent, formatMap);

        for (GamePlayer gp : this.players) {
            if (gp.getBukkitPlayer() == null) {
                continue;
            }

            if (excludingPlayers.contains(gp))
                continue;

            Player p = gp.getBukkitPlayer();

            p.sendMessage(component);
        }
    }

    @Override
    public String toString() {
        return "GameTeam{"
                + "index=" + index + ","
                + "id=" + id  + ","
                + "name=" + name  + ","
                + "rgbColor=" + rgbColor + ","
                + "players=" + players +
                "}";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GameTeam && ((GameTeam) other).getDisplayName().equals(this.name);
    }

}
