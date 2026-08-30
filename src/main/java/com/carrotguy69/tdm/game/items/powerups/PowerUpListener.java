package com.carrotguy69.tdm.game.items.powerups;

import org.bukkit.event.Event;

public interface PowerUpListener<T extends Event> {
    void handle(T event, PowerUp powerUp);
}
