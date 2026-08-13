package com.carrotguy69.tdm.game.items.managers;

import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.classes.CustomItem;

public class CustomItemManager {

    public static void registerCustomItem(CustomItem.Type type, CustomItem item) {
        GenericItemRegistry.customItems.put(type, item);
    }

    public static CustomItem getByType(CustomItem.Type type) {
        return GenericItemRegistry.customItems.get(type);
    }
}
