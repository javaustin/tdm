package com.carrotguy69.tdm.cmd.game;

import com.carrotguy69.cxyz.messages.MessageUtils;
import com.carrotguy69.cxyz.utils.NumberRange;
import com.carrotguy69.tdm.TDM;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.game.items.GenericItemRegistry;
import com.carrotguy69.tdm.game.map.GameMap;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.carrotguy69.tdm.TDM.gameMaps;

public class Create implements CommandExecutor {
    public static CommandExecutor executor = new Create();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        /*
        SYNTAX:
            /tdm create [id] [map] [defaultKit] [maxCapacity]
        */

        String node = "tdm.create";

        if (!sender.hasPermission(node)) {
            MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.COMMAND_NO_ACCESS), Map.of("permission", node));
            return true;
        }

        String gameId = generateValidGameID();
        GameMap gameMap = gameMaps.size() - 1 > 0 ? new ArrayList<>(gameMaps.values()).get(new Random().nextInt(0, gameMaps.size() - 1)) : new ArrayList<>(gameMaps.values()).getFirst();
        String defaultKit = null;
        NumberRange capacity = new NumberRange(2, 32);


        if (args.length >= 1) {
            gameId = args[0];
        }

        if (args.length >= 2) {
            String input = args[1];

            gameMap = GameMap.getByID(input);

            if (gameMap == null || gameMap.getID().equalsIgnoreCase("lobby")) {
                MessageUtils.sendParsedMessage(
                        sender,
                        MessageGrabber.grab(TDMMessageKey.INVALID_MAP),
                        Map.of("input", input)
                );
                return true;
            }
        }

        if (args.length >= 3) {
            String input = args[2];

            if (GenericItemRegistry.getKit(input) == null) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_KIT), Map.of("input", input));
                return true;
            }

            defaultKit = input;
        }

        if (args.length >= 4) {
            String input = args[3];

            try {
                capacity = NumberRange.fromString(input);
            }
            catch (RuntimeException ex) {
                MessageUtils.sendParsedMessage(sender, MessageGrabber.grab(TDMMessageKey.INVALID_RANGE), Map.of("input", input));
                return true;
            }

        }

        Game game = Game.getByID(gameId);
        if (game != null) {
            MessageUtils.sendParsedMessage(
                    sender,
                    MessageGrabber.grab(TDMMessageKey.ERROR_DUPLICATE_GAME),
                    Map.of("input", gameId)
            );
            return true;
        }

        game = new Game(gameId.toLowerCase(), gameMap, capacity, defaultKit);
        TDM.gameIDMap.put(game.getGameID().toLowerCase(), game);

        MessageUtils.sendParsedMessage(
                sender,
                MessageGrabber.grab(TDMMessageKey.COMMAND_CREATE_GAME),
                MapFormatters.gameFormatter(game)
        );

        return true;
    }

    private String generateValidGameID() {
        for (int i = 1; i < 100; i++) {
            Game game = Game.getByID("tdm-" + i);

            if (game == null) {
                return "tdm-" + i;
            }
        }

        // If there are literally 100 games that already exist we are going to return a random uuid.
        return UUID.randomUUID().toString();
    }
}
