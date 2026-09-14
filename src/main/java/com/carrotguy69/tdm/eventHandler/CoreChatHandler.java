package com.carrotguy69.tdm.eventHandler;

import com.carrotguy69.cxyz.events.custom.PublicChatEvent;
import com.carrotguy69.cxyz.events.custom.base.EventHandler;
import com.carrotguy69.cxyz.messages.MessageParser;
import com.carrotguy69.cxyz.models.db.NetworkPlayer;
import com.carrotguy69.cxyz.webhook.DiscordWebhook;
import com.carrotguy69.tdm.TDM;
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

        TDMMessageKey key = switch (game.getGameState()) {
            case WAITING, RESET -> TDMMessageKey.LOBBY_CHAT;
            default -> TDMMessageKey.GAME_CHAT;
        };

        String webhookText = MessageGrabber.grab(TDMMessageKey.valueOf(key.name() + "_WEBHOOK"));

        game.announce(MessageGrabber.grab(key), commonMap, List.of(), np);

        if (TDM.WebhookSettings.enabled && TDM.WebhookSettings.eventsLogged.contains(TDM.WebhookSettings.Event.CHAT)) {

            String webhookContent = MessageParser.getStrippedText(new MessageParser(webhookText, commonMap).parse());

            DiscordWebhook webhook = new DiscordWebhook(TDM.WebhookSettings.url, webhookContent, List.of());

            webhook.send();
        }

        return true;
    }
}
