package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.messages.utils.MapFormatters;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.carrotguy69.tdm.TDM.gameIDMap;
import static com.carrotguy69.tdm.TDM.messagesYML;

public class List implements CommandExecutor {
    public static CommandExecutor executor = new List();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String node = "tdm.list";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("missing-permission", node));
            return true;
        }


        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException ignored) {

            }
        }

        java.util.List<Game> games = new ArrayList<>(gameIDMap.values());

        Map<String, Object> commonMap = new HashMap<>();

        if (games.isEmpty()) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_LIST_GAMES_BLANK), commonMap);
            return true;
        }

        String format = MessageGrabber.grab(TDMMessageKey.COMMAND_LIST_GAMES_FORMAT);
        String delimiter = MessageGrabber.grab(TDMMessageKey.COMMAND_LIST_GAMES_DELIMITER);

        int maxEntriesPerPage = messagesYML.getInt(TDMMessageKey.TEAM_LIST_PLAYERS_MAX_ENTRIES.getPath(), -1);

        MapFormatters.ListFormatter formatter = com.carrotguy69.tdm.messages.utils.MapFormatters.gameListFormatter(games, format, delimiter, maxEntriesPerPage, page);

        commonMap.putAll(formatter.getFormatMap());

        String unparsed = MessageGrabber.grab(TDMMessageKey.COMMAND_LIST_GAMES);

        int min = 1;
        int max = formatter.getMaxPages();

        if (page < 1 || page > max) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_PAGE), Map.of("min", min, "max", max, "page", page));
            return true;
        }

        unparsed = unparsed.replace("{games}", !formatter.getEntries().isEmpty() ? formatter.generatePage(page) : "None");
        unparsed = unparsed.replace("{size}", String.valueOf(games.size()));

        commonMap.put("page", page);
        commonMap.put("previous-page", page - 1);
        commonMap.put("next-page", page + 1);
        commonMap.put("max-pages", max);

        MessageUtils.sendParsedMessage(sender, unparsed, commonMap);

        return false;
    }
}
