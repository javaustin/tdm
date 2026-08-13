package com.carrotguy69.tdm.game.map;

public interface MapSource {
    enum Type {
        SCHEMATIC,
        WORLD_COPY,
        STATIC
    }

    Type getType();
}
