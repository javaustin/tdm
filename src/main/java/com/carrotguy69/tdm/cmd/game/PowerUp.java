package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PowerUp implements CommandExecutor {

    public static CommandExecutor executor = new PowerUp();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        String node = "tdm.powerup";

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

        List<com.carrotguy69.tdm.game.items.powerups.PowerUp> powerUps = GenericItemRegistry.powerUps.values().stream().toList();

        com.carrotguy69.tdm.game.items.powerups.PowerUp powerUp = powerUps.get(new Random().nextInt(powerUps.size()));

        if (args.length > 0) {
            String key = args[0].toLowerCase();

            powerUp = GenericItemRegistry.powerUps.get(key);

            if (powerUp == null) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_POWER_UP), Map.of());
                return true;
            }
        }

        GamePlayer gp = game.getPlayer(p);

        powerUp.applyTo(gp);

        return true;
    }
}
