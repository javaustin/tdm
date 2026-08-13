package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.utils.ObjectUtils;
import com.carrotguy69.tdm.cmd.game.team._TeamSupercommand;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.tabCompleters.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class _GameSupercommand implements CommandExecutor {
    public static CommandExecutor executor = new _GameSupercommand();
    public static TabCompleter tabCompleter = new Game();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        String node = "tdm";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("permission", node));
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.MISSING_GENERAL), Map.of("missing-args", "subcommand"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                Create.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "join":
                Join.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "leave":
                Leave.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "delete":
                Delete.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "list":
                List.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "kit":
                Kit.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "setting":
                Setting.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "freeze":
                Freeze.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "ready":
                Ready.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            case "team":
                _TeamSupercommand.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
                break;

            default:
                Info.executor.onCommand(sender, command, label, ObjectUtils.slice(args, 1));
        }

        return true;
    }
}
