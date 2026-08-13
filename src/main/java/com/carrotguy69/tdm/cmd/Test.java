package com.carrotguy69.tdm.cmd;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Test implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length < 2 || !(sender instanceof Player p)) {
            sender.sendMessage("no");
            return true;
        }

        try {
            int slot = Integer.parseInt(args[0]);
            Material mat = Material.valueOf(args[1].toUpperCase());

            p.getInventory().setItem(slot, new ItemStack(mat));
        }
        catch (RuntimeException e) {
            p.sendMessage(e.getMessage());
            return true;
        }

        return true;
    }
}
