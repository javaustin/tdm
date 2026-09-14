package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.game.items.GenericItem;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Kit implements CommandExecutor {

    public static CommandExecutor executor = new Kit();


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String node = "tdm.kit";

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

        if (args.length == 0) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_KIT), Map.of("input", "null"));
            return true;
        }

        String input = args[0];

        Map<Integer, GenericItem> kit = GenericItemRegistry.getKit(input);

        if (kit == null) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_KIT), Map.of("input", input));
            return true;
        }

        if (game.getGameState() != GameState.WAITING && gp.isAlive()) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_KIT_FAIL), Map.of());
            return true;
        }

        gp.kit = input;

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(gp);

        MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_KIT), commonMap);

        return true;
    }
}
