package com.carrotguy69.tdm.game.other;

import static com.carrotguy69.tdm.TDM.configYML;

public class Durations {

    public Durations() {}

    public int lobbyCountdown = configYML.getInt("game.timers.lobby-countdown", 10);
    public int gameStartCountdown = configYML.getInt("game.timers.game-countdown", 10);
    public int gameEndCountdown = configYML.getInt("game.timers.game-end", 360);
}
