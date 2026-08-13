package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.utils.ObjectUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Ready implements CommandExecutor {

    public static CommandExecutor executor = new Ready();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String node = "tdm.ready";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("permission", node));
            return true;
        }


        if (!(sender instanceof Player p)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_PLAYER_ONLY), Map.of());
            return true;
        }

        Game game = Game.getByPlayer(p);

        if (game == null) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.ERROR_NOT_IN_GAME), Map.of());
            return true;
        }

        GamePlayer gp = game.getPlayer(p);

        boolean value = !gp.isReady();

        if (args.length >= 1) {
            value = ObjectUtils.parseCasualBoolean(args[0]);
        }

        if (game.getGameState() != GameState.WAITING) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.GAME_TOGGLE_READY_FAIL), Map.of());
            return true;
        }

        gp.setReady(value);

        TDMMessageKey key = TDMMessageKey.valueOf("GAME_TOGGLE_" + (value ? "" : "NOT_") + "READY");
        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(gp);
        commonMap.putAll(MapFormatters.gameFormatter(game));

        MessageUtils.sendActionBar(p, MessageUtils.formatPlaceholders(MessageGrabber.grab(key), commonMap));

        return true;
    }
}
