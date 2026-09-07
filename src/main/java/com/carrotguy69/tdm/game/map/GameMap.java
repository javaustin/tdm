package com.carrotguy69.tdm.game.map;

import com.carrotguy69.cxyz.exceptions.InvalidConfigException;

import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.utils.objects.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.List;

import static com.carrotguy69.tdm.TDM.mapsYML;

public class GameMap {

    private final String id;
    private final String name;
//    private final MapSource source;
    private final List<Location> spawns;
    private final List<Location> powerUpSpawns;
    private final BoundingBox bounds;
    private final World world;
    private final boolean isBorderEnabled;
    public boolean isInUse;

    private GameMap(String id, String name, List<Location> spawns, List<Location> powerUpSpawns, BoundingBox bounds, World world, boolean isBorderEnabled) {
        this.id = id;
        this.name = name;
        this.spawns = spawns;
        this.powerUpSpawns = powerUpSpawns;
        this.bounds = bounds;
        this.world = world;
        this.isBorderEnabled = isBorderEnabled;
        this.isInUse = false;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Location> getSpawns() {
        return spawns;
    }
    public List<Location> getPowerUpSpawns() {
        return powerUpSpawns;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public World getWorld() {
        return world;
    }

    public boolean isWorldBorderEnabled() {
        return isBorderEnabled;
    }

    public static List<GameMap> getMaps() {

        List<GameMap> results = new ArrayList<>();

        ConfigurationSection section = mapsYML.getConfigurationSection("maps");

        if (section == null) {
            throw new InvalidConfigException("maps.yml", "maps", "Could not find YAML section!");
        }

        for (String mapID : section.getKeys(false)) {
            String displayName = section.getString(mapID + ".display-name", mapID.toUpperCase());

            String gameWorldName = section.getString(mapID + ".world", null);
            if (gameWorldName == null) {
                throw new InvalidConfigException("maps.yml", "maps." + mapID + ".world", "World not defined!");
            }

            World world = Bukkit.getWorld(gameWorldName);

            if (world == null) {
                throw new RuntimeException(String.format("World \"%s\" could not be found!", gameWorldName));
            }

            // Get copy bounds
            Location copyBoundsPos1 = LocationUtils.getLocationFromYML(section.getMapList(mapID + ".source.copy-bounds.pos1"));
            Location copyBoundsPos2 = LocationUtils.getLocationFromYML(section.getMapList(mapID + ".source.copy-bounds.pos2"));

            BoundingBox copyBounds = null;
            if (copyBoundsPos1 != null && copyBoundsPos2 != null)
                copyBounds = new BoundingBox(copyBoundsPos1.x(), copyBoundsPos1.y(), copyBoundsPos1.z(), copyBoundsPos2.x(), copyBoundsPos2.y(), copyBoundsPos2.z());

            // Get spawns
            List<Location> spawns = LocationUtils.getLocationsFromYML(section.getMapList(mapID + ".spawns"));
            List<Location> powerUpSpawns = LocationUtils.getLocationsFromYML(section.getMapList(mapID + ".powerup-spawns"));

            Location boundsPos1 = LocationUtils.getLocationFromYML(section.getMapList(mapID + ".bounds.pos1"));
            Location boundsPos2 = LocationUtils.getLocationFromYML(section.getMapList(mapID + ".bounds.pos2"));

            BoundingBox mapBounds;

            if (spawns.isEmpty()) {
                throw new InvalidConfigException("maps.yml", "maps." + mapID + ".spawns", "Spawns not defined!");
            }

            if (spawns.size() < 2 && !mapID.equalsIgnoreCase("lobby"))
                throw new InvalidConfigException("maps.yml", "maps." + mapID + ".spawns", "There must be at least two spawns!");


            if (boundsPos1 == null) {
                throw new InvalidConfigException("maps.yml", "maps." + mapID + ".bounds.pos1", "Position 1 not defined!");
            }

            if (boundsPos2 == null) {
                throw new InvalidConfigException("maps.yml", "maps." + mapID + ".bounds.pos2", "Position 2 not defined!");
            }

            mapBounds = new BoundingBox(boundsPos1.x(), boundsPos1.y(), boundsPos1.z(), boundsPos2.x(), boundsPos2.y(), boundsPos2.z());

            boolean enabled = section.getBoolean(mapID + ".world-border.enabled", false);

            GameMap map = new GameMap(mapID, displayName, /*source,*/ spawns, powerUpSpawns, mapBounds, world, enabled);

            results.add(map);
        }

        return results;
    }

    public static GameMap getByID(String mapID) {
        for (GameMap map : TDM.gameMaps.values()) {
            if (map.getID().equalsIgnoreCase(mapID)) {
                return map;
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GameMap))
            return false;

        return this.getID().equalsIgnoreCase(((GameMap) other).getID());
    }

    @Override
    public String toString() {
        return "GameMap{"
                + "id=" + id + ","
                + "name=" + name + ","
//                + "source=" + source + ","
                + "spawns=" + spawns + ","
                + "bounds=" + bounds + ","
                + "world=" + world.getName() + ","
                + "isBorderEnabled=" + isBorderEnabled + ","
                + "isInUse=" + isInUse +
                "}";
    }

}
