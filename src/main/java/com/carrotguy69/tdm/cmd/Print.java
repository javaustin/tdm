package com.carrotguy69.tdm.cmd;

import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.utils.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Print implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "games":
                Logger.info("games: " + TDM.gameIDMap.toString());
                break;

            case "kits":
                Logger.info("kits:" + GenericItemRegistry.kits);
                break;


        }

        return true;
    }
}
