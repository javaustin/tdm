package com.carrotguy69.tdm.game.items.classes;

import com.carrotguy69.cxyz.exceptions.InvalidConfigException;
import com.carrotguy69.tdm.game.items.GenericItem;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.carrotguy69.cxyz.CXYZ.f;

public class CustomItem implements GenericItem {

    public enum Type {
        AMMO,
        GRENADE,
        PLAYER_TRACKER
    }

    private final String id;
    private final String customName;
    private final Material material;
    private final Type customItemType;
    private final int amount;
    private final List<String> lore;
    private final List<Enchant> enchants;


    public CustomItem(String id, String customName, Material material, String customItemType, int amount, List<String> lore, List<Enchant> enchants) {
        this.id = id;
        this.customName = customName;
        this.material = material;
        this.amount = amount;
        this.lore = lore;
        this.enchants = enchants;

        try {
            this.customItemType = Type.valueOf(customItemType.toUpperCase().replace("-", "_"));
        }
        catch (IllegalArgumentException e) {
            throw new InvalidConfigException("kits.yml", "custom-items." + id + ".custom-type", "Invalid type! The only supported custom types are: " + String.join(", ", Arrays.stream(Type.values()).map(Type::name).toList()));
        }
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getCustomName() {
        return customName;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public GenericItem.Type getType() {
        return GenericItem.Type.CUSTOM_ITEM;
    }

    public CustomItem.Type getCustomItemType() {
        return customItemType;
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public List<Enchant> getEnchants() {
        return enchants;
    }

    @Override
    public CustomItem copy() {
        return new CustomItem(id, customName, material, this.customItemType.name(), amount, lore, enchants);
    }

    public static CustomItem getByType(Type type) {
        return GenericItemRegistry.customItems.get(type);
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material);

        itemStack.setAmount(amount);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        if (meta == null) {
            return itemStack;
        }

        meta.getPersistentDataContainer().set(
                GenericItemRegistry.customTypeKey,
                PersistentDataType.STRING,
                id
        );

        if (customName != null) {
            // Sorry paper, I like my coloring better
            meta.setDisplayName(f(customName));
        }

        if (lore != null) {
            List<String> coloredLore = new ArrayList<>();

            for (String line : lore) {
                coloredLore.add(f(line));
            }

            meta.setLore(coloredLore);
        }

        if (enchants != null) {
            for (Enchant enchant : enchants) {

                Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

                Enchantment mcEnchantment = registry.get(NamespacedKey.minecraft(enchant.id()));

                if (mcEnchantment == null) {
                    continue;
                }

                meta.addEnchant(mcEnchantment, enchant.level(), true);            }
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }


    @Override
    public String toString() {
        return "MCItem{"
                + "id=" + id + ","
                + "displayName=" + customName  + ","
                + "material=" + material.name()  + ","
                + "amount=" + amount  + ","
                + "enchants=" + enchants + ","
                + "lore=" + lore + ","
                + "customItemType=" + customItemType.name() +
                "}";
    }
}
