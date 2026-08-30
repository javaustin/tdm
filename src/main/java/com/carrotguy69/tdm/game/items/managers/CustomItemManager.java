package com.carrotguy69.tdm.game.items.managers;

import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.items.classes.CustomItem;
import com.carrotguy69.tdm.game.items.powerups.PowerUp;

public class CustomItemManager {

    public static void registerCustomItem(CustomItem.Type type, CustomItem item) {
        if (type == CustomItem.Type.POWER_UP) {

            PowerUp powerUp = new PowerUp(item, null);

            GenericItemRegistry.powerUps.put(item.getID(), powerUp);
            return;
        }

        GenericItemRegistry.customItems.put(type, item);
    }

    public static CustomItem getByType(CustomItem.Type type) {
        return GenericItemRegistry.customItems.get(type);
    }
}
