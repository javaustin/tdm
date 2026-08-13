package com.carrotguy69.tdm.eventHandler;

import com.carrotguy69.cxyz.events.custom.PublicChatEvent;
import com.carrotguy69.cxyz.events.custom.base.EventHandler;
import com.carrotguy69.cxyz.models.db.NetworkPlayer;
import com.carrotguy69.tdm.game.Game;
import com.carrotguy69.tdm.messages.MessageGrabber;
import com.carrotguy69.tdm.messages.TDMMessageKey;
import com.carrotguy69.tdm.messages.utils.MapFormatters;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class CoreChatHandler implements EventHandler<PublicChatEvent> {

    @Override
    public boolean handle(PublicChatEvent e) {

        String content = e.getContent();
        NetworkPlayer np = e.getSender();

        Player p = np.getPlayer();
        Game game = Game.getByPlayer(p);


        if (game == null) {
            return false;
        }

        // handle lobby chat, game chat

        Map<String, Object> commonMap = MapFormatters.gamePlayerFormatter(game.getPlayer(p));

        commonMap.putAll(MapFormatters.gameFormatter(game));
        commonMap.put("message", content);
        commonMap.put("content", content);

        switch (game.getGameState()) {
            case WAITING:
            case RESET:
                game.announce(MessageGrabber.grab(TDMMessageKey.LOBBY_CHAT), commonMap, List.of(), np);
                break;

            case ACTIVE:
            case STARTING:
            case ENDING:
                game.announce(MessageGrabber.grab(TDMMessageKey.GAME_CHAT), commonMap, List.of(), np);
                break;
        }

        return true;
    }
}
