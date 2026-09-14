package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Freeze implements CommandExecutor {

    public static CommandExecutor executor = new Freeze();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String node = "tdm.freeze";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("permission", node));
            return true;
        }

        Game game;

        if (!(sender instanceof Player p)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_PLAYER_ONLY), Map.of());
            return true;
        }

        game = Game.getByPlayer(p);

        if (game == null) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.ERROR_NOT_IN_GAME), Map.of());
            return true;
        }


        game.freeze(game.getPlayer(p));

        return true;
    }
}
