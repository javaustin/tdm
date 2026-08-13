package com.carrotguy69.tdm.tabCompleters;

import com.carrotguy69.cxyz.utils.ObjectUtils;
import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.cmd.game.team._TeamSupercommand;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Game implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        String baseNode = "tdm";

        List<String> subcommands = new ArrayList<>(List.of("create", "delete", "freeze", "info", "join", "leave", "list", "ready", "setting", "team", "kit"));
        List<String> options = new ArrayList<>(subcommands);
        List<String> results = new ArrayList<>();


        List<String> temp = new ArrayList<>();
        for (String option : options) {
            if (sender.hasPermission(baseNode + "." + option)) {
                temp.add(option);
            }
        }

        options.clear();
        options.addAll(temp);

        if (args.length == 0) {
            return options;
        }

        String subcommand = args[0];

        // if args len is 1, jump to the bottom and return matching subcommand options

        if (args.length >= 2 && subcommand.equalsIgnoreCase("team")) {
            return _TeamSupercommand.tabCompleter.onTabComplete(sender, command, label, ObjectUtils.slice(args, 1, args.length));
        }

        if (args.length == 2) {
            options = switch (subcommand.toLowerCase()) {
                case "delete", "join", "info" ->
                        TDM.gameIDMap.values().stream().map(com.carrotguy69.tdm.game.Game::getGameID).toList();
                case "setting" ->
                        new ArrayList<>(List.of("map", "capacity", "defaultKit"));
                case "ready" ->
                        ObjectUtils.getCasualBooleanOptions();
                case "kit" ->
                        GenericItemRegistry.kits.keySet().stream().toList();
                default -> List.of();
            };
        }

        if (args.length == 3) {
            if (subcommand.equalsIgnoreCase("create")) {
                options = TDM.gameMaps.keySet().stream().toList();
            }

            else if (subcommand.equalsIgnoreCase("setting")) {
                options = switch (args[1].toLowerCase()) {
                    case "map" -> TDM.gameMaps.keySet().stream().toList();
                    case "defaultkit" -> GenericItemRegistry.kits.keySet().stream().toList();
                    case "capacity" -> List.of("16", "32", "64", "100");
                    default -> List.of();
                };
            }

            else {
                options = List.of();
            }
        }

        if (args.length == 4) {
            if (subcommand.equalsIgnoreCase("create"))
                options = GenericItemRegistry.kits.keySet().stream().toList();
            else
                options = List.of();
        }

        if (args.length == 5) {
            if (subcommand.equalsIgnoreCase("create"))
                options = List.of("8", "16", "32", "64", "100");
            else
                options = List.of();
        }

        if (args.length >= 6) {
            return List.of();
        }

        for (String s : options) {
            if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                results.add(s);
            }
        }

        if (subcommands.contains(args[0])) {
            if (!sender.hasPermission("tdm." + args[0])) {
                results.clear();
            }
        }

        return results;
    }
}
