package com.carrotguy69.tdm.game.map.sources;

import com.carrotguy69.tdm.game.map.MapSource;

public class StaticSource implements MapSource {
    @Override
    public Type getType() {
        return Type.STATIC;
    }

    @Override
    public String toString() {
        return "StaticSource{}";
    }
}
