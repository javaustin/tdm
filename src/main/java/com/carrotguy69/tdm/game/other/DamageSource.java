package com.carrotguy69.tdm.game.other;

import com.carrotguy69.cxyz.models.db.NetworkPlayer;
import com.carrotguy69.tdm.game.GamePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public record DamageSource(GamePlayer attacker, Reason reason) {
    public enum Reason {
        MELEE,
        PROJECTILE,
        EXPLOSIVE,
        NATURAL
    }

    public DamageSource(GamePlayer attacker, @Nullable Reason reason) {
        if (reason == null) {
            reason = Reason.NATURAL;
        }

        this.attacker = attacker;
        this.reason = reason;
    }

    public boolean isAttackerSelf(GamePlayer player) {
        return Objects.equals(attacker, player);
    }

    @Override
    public @NonNull String toString() {
        return "DamageSource{"
                + "attacker=" + NetworkPlayer.resolvePlayer(attacker.getUUID()).getDisplayName() + ","
                + "reason=" + reason +
                "}";
    }
}
