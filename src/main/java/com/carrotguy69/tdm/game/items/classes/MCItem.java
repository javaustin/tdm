package com.carrotguy69.tdm.game.items.classes;

import com.carrotguy69.tdm.game.items.GenericItem;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static com.carrotguy69.cxyz.CXYZ.f;

public class MCItem implements GenericItem {

    private final String id;
    private final String customName;
    private final Material material;
    private final int amount;
    private final List<String> lore;
    private final List<Enchant> enchants;


    public MCItem(String id, String customName, Material material, int amount, List<String> lore, List<Enchant> enchants) {
        this.id = id;
        this.customName = customName;
        this.material = material;
        this.amount = amount;
        this.lore = lore;
        this.enchants = enchants;
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
    public Type getType() {
        return Type.MC_ITEM;
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
    public MCItem copy() {
        return new MCItem(id, customName, material, amount, lore, enchants);
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material);

        itemStack.setAmount(amount);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);

        if (meta == null) {
            return itemStack;
        }

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

                meta.addEnchant(mcEnchantment, enchant.level(), true);
            }
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
                + "lore=" + lore +
                "}";
    }
}
