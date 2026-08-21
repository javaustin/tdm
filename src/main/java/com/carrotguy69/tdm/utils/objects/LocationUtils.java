package com.carrotguy69.tdm.utils.objects;

import com.carrotguy69.cxyz.utils.ObjectUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameTeam;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationUtils {

    public static List<Location> getLocationsFromYML(List<Map<?, ?>> ymlList) {

        List<Location> results = new ArrayList<>();

        for (Map<?, ?> location : ymlList) {
            Object worldObj = location.get("world");
            Object xObj = location.get("x");
            Object yObj = location.get("y");
            Object zObj = location.get("z");
            Object yawObj = location.get("yaw") != null ? location.get("yaw") : 0F;
            Object pitchObj = location.get("pitch") != null ? location.get("pitch") : 0F;

            for (Object o : List.of(worldObj, xObj, yObj, zObj)) {
                if (o == null) {
                    throw new RuntimeException("Incomplete location! (Missing world, x, y, or z.)");
                }
            }


            String worldName = (String) worldObj;
            double x = ((Number) xObj).doubleValue();
            double y = ((Number) yObj).doubleValue();
            double z = ((Number) zObj).doubleValue();

            float yaw = ObjectUtils.isValidNumber(String.valueOf(yawObj)) ? ObjectUtils.parseAs(Float.class, String.valueOf(yawObj)) : 0F;
            float pitch = ObjectUtils.isValidNumber(String.valueOf(pitchObj)) ? ObjectUtils.parseAs(Float.class, String.valueOf(pitchObj)) : 0F;

            World bukkitWorld = Bukkit.getWorld(worldName);

            Location loc = new Location(bukkitWorld, x, y, z, yaw, pitch);
            results.add(loc);
        }
        
        return results;
    }

    public static Location getLocationFromYML(List<Map<?, ?>> ymlList) {
        // Returns the first element of the list returned by getLocationsFromYML(List<Map<?, ?>> ymlList)\
        // Use if you are trying to get a single location (e.g. paste-location) from the config.

        List<Location> results = getLocationsFromYML(ymlList);
        
        if (results.isEmpty()) {
            return null;
        }

        return results.getFirst();
    }

    public static Location getNearestPlayerLocation(Game game, GamePlayer gp) { // the only reason the compass would point outwards is if a player was vanished
        Player nearestPlayer = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        // get opposite team members
        List<GamePlayer> enemies = new ArrayList<>();

        for (GameTeam team : game.getTeams()) {
            if (team.getPlayers().contains(gp)) {
                continue;
            }

            enemies.addAll(team.getPlayers());
        }

        for (GamePlayer enemy : enemies) {
            if (gp == null)
                continue;

            if (gp.getBukkitPlayer() == null)
                continue;

            if (enemy == null)
                continue;

            if (enemy.getBukkitPlayer() == null)
                continue;

            if (!(gp.getBukkitPlayer().canSee(enemy.getBukkitPlayer()) || enemy.getBukkitPlayer().getWorld() == gp.getBukkitPlayer().getWorld()))
                continue;

            double distanceSquared = gp.getBukkitPlayer().getLocation().distanceSquared(enemy.getBukkitPlayer().getLocation());
            if (distanceSquared < nearestDistanceSquared) {
                nearestPlayer = enemy.getBukkitPlayer();
                nearestDistanceSquared = distanceSquared;
            }

        }

        return nearestPlayer != null ? nearestPlayer.getLocation() : null;
    }
    
    
}
