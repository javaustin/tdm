package com.carrotguy69.tdm.game.items;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface GenericItem {
    record Enchant(String id, int level) {}
    enum Type {
        MC_ITEM,
        GUN,
        CUSTOM_ITEM
    }

    String getID();
    String getCustomName();
    Material getMaterial();
    int getAmount();
    Type getType();
    List<String> getLore();
    List<Enchant> getEnchants();
    Object copy();

    ItemStack toItemStack();

}
