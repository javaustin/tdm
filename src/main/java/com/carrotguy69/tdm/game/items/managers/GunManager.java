package com.carrotguy69.tdm.game.items.managers;

import com.carrotguy69.tdm.game.items.GenericItem;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import com.carrotguy69.tdm.game.items.classes.GunItem;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GunManager {
    public static Multimap<Player, GunItem> playerGunMap = ArrayListMultimap.create();


    public static GunItem getByID(String id) {
        GenericItem generic = GenericItemRegistry.getItemByID(id);

        if (!(generic instanceof GunItem)) {
            return null;
        }

        return (GunItem) generic;
    }

    public static void registerGun(@NotNull Player p, @NotNull GunItem g) {
        playerGunMap.put(p, g);
    }

    public static void removeGuns(Player p) {
        playerGunMap.removeAll(p);
    }

    public static void handleClick(@NotNull Player p, Action action) {

        GunItem gun = null;

        Collection<GunItem> playerGuns = playerGunMap.get(p);

        for (GunItem gunItem : playerGuns) {
            ItemStack is = p.getInventory().getItemInMainHand();

            if (is.getItemMeta() == null) {
                continue;
            }

            String type = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(GenericItemRegistry.customTypeKey, PersistentDataType.STRING);

            if (Objects.equals(type, gunItem.getID())) {
                gun = gunItem;
            }
        }

        if (gun == null) {
            return;
        }

        if (action.isLeftClick())
            gun.reload(p);

        if (action.isRightClick())
            gun.fire(p);
    }



    public static int getSpareAmmo(Player p) {
        int amount = 0;

        PlayerInventory i = p.getInventory();

        for (ItemStack is : i.getStorageContents()) {
            if (is != null && is.getItemMeta() == null)
                continue;

            if (isAmmo(is)) {
                return is.getAmount();
            }
        }

        return amount;
    }

    public static void setLeftoverAmmo(Player p, int amount) {
        PlayerInventory i = p.getInventory();

        for (ItemStack is : i.getStorageContents()) {
            if (is != null && is.getItemMeta() == null)
                continue;

            if (isAmmo(is)) {
                is.setAmount(amount);
            }
        }
    }

    public static boolean isAmmo(ItemStack is) {
        if (is == null || is.getItemMeta() == null)
            return false;

        String type = is.getItemMeta().getPersistentDataContainer().get(GenericItemRegistry.customTypeKey, PersistentDataType.STRING);

        GenericItem genericItem = GenericItemRegistry.allItems.get(type);

        return genericItem != null && genericItem.getType() == GenericItem.Type.CUSTOM_ITEM && ((CustomItem) genericItem).getCustomItemType() == CustomItem.Type.AMMO;
    }

    public static @Nullable String getGunID(ItemStack is) {
        if (is.getItemMeta() == null)
            return null;

        String type;

        try {
            type = is.getItemMeta().getPersistentDataContainer().get(GenericItemRegistry.customTypeKey, PersistentDataType.STRING);
        }
        catch (IllegalArgumentException ignore) {
            return null;
        }

        GenericItem genericItem = GenericItemRegistry.allItems.get(type);

        if (genericItem instanceof GunItem) {
            return genericItem.getID();
        }

        return null;
    }
}
