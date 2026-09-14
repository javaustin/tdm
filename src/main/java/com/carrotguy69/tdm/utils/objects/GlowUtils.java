package com.carrotguy69.tdm.utils.objects;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GlowUtils {

    public static Scoreboard SCOREBOARD;

    public static void setGlowing(Player p, int rgbColor) {
        String teamName = "glow_" + rgbColor;

        Team team = SCOREBOARD.getTeam(teamName);

        if (team == null) {
            team = SCOREBOARD.registerNewTeam(teamName);

            TextColor chatColor = TextColor.color(rgbColor);

            team.color(NamedTextColor.nearestTo(chatColor));
        }

        for (Team existing : SCOREBOARD.getTeams()) {
            if (existing.hasEntry(p.getName()) && existing != team) {
                existing.removeEntry(p.getName());
            }
        }

        team.addEntry(p.getName());
        p.setGlowing(true);
    }

    public static void resetGlowing(Player p) {
        for (Team team : SCOREBOARD.getTeams()) {
            if (team.getName().startsWith("glow_")){
                team.removeEntry(p.getName());
            }
        }

        p.setGlowing(false);
    }

}
