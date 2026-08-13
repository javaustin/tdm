package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.messages.utils.MapFormatters;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.carrotguy69.tdm.TDM.messagesYML;

public class Info implements CommandExecutor {

    public static CommandExecutor executor = new Info();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String node = "tdm.info";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("permission", node));
            return true;
        }

        Game game;

        if (args.length == 0 && !(sender instanceof Player)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.MISSING_GENERAL), Map.of("missing-args", "gameID"));
            return true;
        }

        else if (args.length == 0) {
            Player p = (Player) sender;

            game = Game.getByPlayer(p);

            if (game == null) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.ERROR_NOT_IN_GAME), Map.of());
                return true;
            }
        }

        else {
            game = Game.getByID(args[0]);
        }

        if (game == null) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_GAME), Map.of("input", args[0]));
            return true;
        }

        sendGameInfo(sender, game);

        return true;
    }

    private void sendGameInfo(CommandSender sender, Game game) {

        Map<String, Object> commonMap = com.carrotguy69.tdm.messages.utils.MapFormatters.gameFormatter(game);

        String format = MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_INFO_FORMAT);
        String delimiter = MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_INFO_DELIMITER);

        int maxEntriesPerPage = messagesYML.getInt(TDMMessageKey.COMMAND_GAME_INFO_MAX_ENTRIES.getPath(), -1);

        MapFormatters.ListFormatter formatter = com.carrotguy69.tdm.messages.utils.MapFormatters.gamePlayerListFormatter(game.getPlayers(), format, delimiter, maxEntriesPerPage, 1);

        commonMap.putAll(formatter.getFormatMap());

        String unparsed = MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_INFO);

        unparsed = unparsed.replace("{players}", !formatter.getEntries().isEmpty() ? formatter.generatePage(1) : "None");

        commonMap.put("game-players-size", game.getPlayers().size());

        MessageUtils.sendParsedMessage(sender, unparsed, commonMap);

    }
}
