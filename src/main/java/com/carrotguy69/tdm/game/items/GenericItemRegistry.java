package com.carrotguy69.tdm.game.items;

import com.carrotguy69.cxyz.exceptions.InvalidConfigException;
import com.carrotguy69.cxyz.utils.ItemUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.managers.CustomItemManager;
import com.carrotguy69.tdm.game.items.managers.GunManager;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import com.carrotguy69.tdm.game.items.classes.GunItem;
import com.carrotguy69.tdm.game.items.classes.MCItem;
import com.carrotguy69.tdm.utils.objects.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.carrotguy69.tdm.TDM.kitsYML;
import static com.carrotguy69.tdm.TDM.plugin;

public class GenericItemRegistry {

    public static NamespacedKey customTypeKey = new NamespacedKey(plugin, "tdm_custom_type");


    public static Map<CustomItem.Type, CustomItem> customItems = new HashMap<>();
    public static Map<String, GenericItem> allItems = new HashMap<>();
    public static Map<String, Map<Integer, GenericItem>> kits = new HashMap<>();

    public static void loadKits() {
        ConfigurationSection section = kitsYML.getConfigurationSection("kits");

        if (section == null) {
            throw new InvalidConfigException("kits.yml", "kits", "Section not found!");
        }

        for (String key : section.getKeys(false)) {
            Map<Integer, GenericItem> slotItemMap = new HashMap<>();

            List<Map<?, ?>> itemMapList = kitsYML.getMapList("kits." + key);

            for (Map<?, ?> map : itemMapList) {
                String itemID = (String) map.get("id");
                int slot = (int) map.get("slot");

                GenericItem item = allItems.get(itemID);

                if (item == null) {
                    throw new RuntimeException(String.format("GenericItem by id '%s' not found. Make sure it is defined in kits.yml under mc-items, guns, or custom-items.", itemID));
                }

                slotItemMap.put(slot, item);
            }

            if (!slotItemMap.isEmpty()) {
                kits.put(key, slotItemMap);
            }
        }
    }

    public static Map<Integer, GenericItem> getKit(String id) {
        return kits.get(id);
    }

    public static GenericItem getItemByID(String id) {
        return allItems.get(id);
    }

    public static void spawnKit(Player p, Map<Integer, GenericItem> kit) {

        for (Map.Entry<Integer, GenericItem> entry : kit.entrySet()) {
            int slot = entry.getKey();
            GenericItem item = entry.getValue();

            item = (GenericItem) item.copy(); // Important: make a copy of the item so players (with guns) don't affect the template.

            if (item.getType() == GenericItem.Type.GUN) {
                GunManager.registerGun(p, (GunItem) item);
            }

            ItemUtils.setItem(p.getInventory(), item.toItemStack(), slot);

            // create compass task if exist
            Game game = Game.getByPlayer(p);

            if (game == null) {
                continue;
            }

            GamePlayer gp = game.getPlayer(p);

            handleCompassTask(game, gp);
        }
    }

    private static void handleCompassTask(Game game, GamePlayer gp) {

        if (gp == null) {
            return;
        }

        if (gp.compassTask != null) {
            gp.compassTask.cancel();
        }

        gp.compassTask = new BukkitRunnable() {public void run() {

            Location target = LocationUtils.getNearestPlayerLocation(game, gp);

            if (target != null)
                gp.getBukkitPlayer().setCompassTarget(target);

        }}.runTaskTimer(plugin, 0L, 2L);
    }

    public static void loadCustomItems() {

        ConfigurationSection section = kitsYML.getConfigurationSection("custom-items");

        if (section == null) {
            throw new InvalidConfigException("kits.yml", "custom-items", "Section not found!");
        }

        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name", "");
            Material material = Material.valueOf(section.getString(id + ".material", "AIR"));
            String type = section.getString(id + ".custom-type", "");
            int amount = section.getInt(id + ".amount", 1);
            List<String> lore = section.getStringList(id + ".lore");
            List<Map<?, ?>> enchantsMapList = section.getMapList(id + ".enchants");
            List<GenericItem.Enchant> enchants = parseEnchantMapList(enchantsMapList);

            CustomItem item = new CustomItem(id, name, material, type ,amount, lore, enchants);

            allItems.put(id, item);
            CustomItemManager.registerCustomItem(item.getCustomItemType(), item);
        }
    }

    public static void loadItems() {

        ConfigurationSection section = kitsYML.getConfigurationSection("mc-items");

        if (section == null) {
            throw new InvalidConfigException("kits.yml", "mc-items", "Section not found!");
        }

        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name", "");
            Material material = Material.valueOf(section.getString(id + ".material", "AIR"));
            int amount = section.getInt(id + ".amount", 1);
            List<String> lore = section.getStringList(id + ".lore");
            List<Map<?, ?>> enchantsMapList = section.getMapList(id + ".enchants");
            List<GenericItem.Enchant> enchants = parseEnchantMapList(enchantsMapList);

            MCItem item = new MCItem(id, name, material, amount, lore, enchants);

            allItems.put(id, item);
        }
    }

    public static void loadGuns() {
        ConfigurationSection section = kitsYML.getConfigurationSection("guns");

        if (section == null) {
            throw new InvalidConfigException("kits.yml", "guns", "Section not found!");
        }

        for (String id : section.getKeys(false)) {
            String name = section.getString(id + ".name", "");
            Material material = Material.valueOf(section.getString(id + ".material", "AIR"));
            int magSize = section.getInt(id + ".magazine-size");
            int startingAmmo = section.getInt(id + ".starting-ammo", magSize);
            int projectiles = section.getInt(id + ".projectiles");
            double spreadVertical = section.getDouble(id + ".vertical-spread");
            double spreadHorizontal = section.getDouble(id + ".horizontal-spread");
            double fireRate = section.getDouble(id + ".fire-rate-per-second");
            double reloadTime = section.getDouble(id + ".reload-time-seconds");
            double damage = section.getDouble(id + ".damage");
            double range = section.getDouble(id + ".range");
            List<String> lore = section.getStringList(id + ".lore");
            List<Map<?, ?>> enchantsMapList = section.getMapList(id + ".enchants");
            List<GenericItem.Enchant> enchants = parseEnchantMapList(enchantsMapList);

            GunItem gunItem = new GunItem(id, name, material, magSize, startingAmmo, projectiles, spreadVertical, spreadHorizontal, fireRate, reloadTime, damage, range);

            gunItem.setLore(lore);
            gunItem.setEnchants(enchants);

            allItems.put(id, gunItem);
        }
    }

    private static List<GenericItem.Enchant> parseEnchantMapList(List<Map<?, ?>> enchants) {
        List<GenericItem.Enchant> results = new ArrayList<>();

        for (Map<?, ?> enchant : enchants) {
            Object idObj = enchant.get("id");
            Object levelObj = enchant.get("level");

            if (idObj == null) {
                continue;
            }

            if (levelObj == null) {
                levelObj = "1";
            }

            String id = idObj.toString();
            int level = Integer.parseInt(levelObj.toString());

            results.add(new GenericItem.Enchant(id.toLowerCase(), level));
        }

        return results;
    }

}
