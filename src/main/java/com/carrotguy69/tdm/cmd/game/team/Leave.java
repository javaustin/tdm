package com.carrotguy69.tdm.cmd.game.team;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.GamePlayer;
import com.carrotguy69.tdm.game.GameState;
import com.carrotguy69.tdm.game.GameTeam;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Leave implements CommandExecutor {
    public static CommandExecutor executor = new Leave();


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        /*
        SYNTAX:
            /team leave
        */

        String node = "tdm.team.leave";

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
            MessageUtils.sendParsedMessage(
                    sender,
                    MessageGrabber.grab(TDMMessageKey.ERROR_NOT_IN_GAME),
                    Map.of()
            );
            return true;
        }

        if (game.getGameState() != GameState.WAITING) {
            MessageUtils.sendParsedMessage(
                    sender,
                    MessageGrabber.grab(TDMMessageKey.ERROR_TEAM_NO_SWITCHING),
                    Map.of()
            );
            return true;
        }

        GamePlayer gp = game.getPlayer(p);

        if (gp == null) {
            MessageUtils.sendParsedMessage(
                    sender,
                    MessageGrabber.grab(TDMMessageKey.ERROR_NOT_IN_GAME),
                    Map.of()
            );

            return true;
        }

        GameTeam team = gp.getTeam();

        if (team == null) {
            MessageUtils.sendParsedMessage(
                    sender,
                    MessageGrabber.grab(TDMMessageKey.ERROR_TEAM_NOT_IN_TEAM),
                    Map.of()
            );

            return true;
        }

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(gp);
        commonMap.putAll(MapFormatters.teamFormatter(gp.getTeam()));

        team.removePlayer(gp);
        gp.setTeam(null);

        MessageUtils.sendParsedMessage(
                sender,
                MessageGrabber.grab(TDMMessageKey.TEAM_LEAVE),
                commonMap
        );

        team.sendTeamMessage(
                MessageGrabber.grab(TDMMessageKey.TEAM_LEAVE_ANNOUNCEMENT),
                commonMap,
                List.of(gp)
        );

        game.updateScoreboard();

        return true;
    }
}
