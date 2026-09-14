package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.utils.NumberRange;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.game.items.GenericItem;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.map.GameMap;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Setting implements CommandExecutor {

    public static CommandExecutor executor = new Setting();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        //                       [0]    [1]
        // usage: /game setting {key} {value}

        String node = "tdm.setting";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("permission", node));
            return true;
        }

        Game game;

        if (!(sender instanceof Player p)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_PLAYER_ONLY), Map.of());
            return true;
        }

        String key = "";
        String value = null;

        game = Game.getByPlayer(p);

        if (game == null) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.ERROR_NOT_IN_GAME), Map.of());
            return true;
        }

        if (args.length == 0) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.MISSING_GENERAL), Map.of("missing-args", "setting"));
            return true;
        }

        if (args.length == 1) {
            key = args[0];
        }

        if (args.length == 2) {
            key = args[0];
            value = args[1];
        }

        Map<String, Object> commonMap = MapFormatters.gameFormatter(game);

        if (key.equalsIgnoreCase("map")) {
            commonMap.put("key", "map");
            if (value == null) {
                commonMap.put("value", game.getGameMap().getID());
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_GET), commonMap);
                return true;
            }

            GameMap map = GameMap.getByID(value);

            commonMap.put("input", value);
            commonMap.put("value", value);

            if (map == null || map.getID().equalsIgnoreCase("lobby")) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.LOBBY_INVALID_MAP), commonMap);
                return true;
            }

            if (game.getGameState() != GameState.WAITING) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_FAIL), commonMap);
                return true;
            }

            game.setGameMap(map);
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_SET), commonMap);
            return true;

        }

        else if (key.equalsIgnoreCase("defaultKit")) {
            commonMap.put("key", "defaultKit");

            if (value == null) {
                commonMap.put("value", game.getDefaultKit());
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_GET), commonMap);
                return true;
            }

            Map<Integer, GenericItem> kit = GenericItemRegistry.getKit(value);

            if (kit == null) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_KIT), commonMap);
                return true;
            }

            game.setDefaultKit(value);

            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_SET), commonMap);

        }

        else if (key.equalsIgnoreCase("capacity")) {
            commonMap.put("key", "capacity");

            if (value == null) {
                commonMap.put("value", game.getGameCapacity().toPrettyString());
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_GET), commonMap);
                return true;
            }

            try {
                NumberRange range = NumberRange.fromString(value);

                commonMap.put("value", range.toPrettyString());

                if (game.getGameState() != GameState.WAITING) {
                    MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_FAIL), commonMap);
                    return true;
                }

                game.setGameCapacity(range);

                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_GAME_SETTING_SET), commonMap);

            }
            catch (RuntimeException e) {
                commonMap.put("input", value);
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_TEAM_CAPACITY), commonMap);
            }
        }


        else {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_GAME_SETTING), Map.of("input", key));
        }

        return true;
    }

}
