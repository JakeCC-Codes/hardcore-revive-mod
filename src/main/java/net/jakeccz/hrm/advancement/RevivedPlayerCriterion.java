package net.jakeccz.hrm.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class RevivedPlayerCriterion extends AbstractCriterion<RevivedPlayerCriterion.Conditions> {
    static final Identifier ID = HardcoreReviveModUtil.createId("player_revived");

    public Identifier getId() { return ID; }

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return RevivedPlayerCriterion.Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, Conditions::getConditionOutput);
    }

    public static record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<Conditions> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(Conditions::player)).apply(instance, Conditions::new);
        });
        public Conditions(Optional<LootContextPredicate> player) {
            this.player = player;
        }

        public boolean getConditionOutput() {
            return true;
        }

        @Override
        public Optional<LootContextPredicate> player() {
            return this.player;
        }
    }
}
