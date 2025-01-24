package net.jakeccz.hrm.mixin;

import net.jakeccz.hrm.util.HardcoreReviveModConfig;
import net.jakeccz.hrm.util.HardcoreReviveModUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void ServerPlayerEntity_Death(DamageSource damageSource, CallbackInfo info) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ServerWorld world = (ServerWorld)player.getWorld();
        MinecraftServer server = world.getServer();
        if (server.isHardcore() && !(player.isCreative() || player.isSpectator()) || HardcoreReviveModConfig.CREATIVE_PLAYERS_DROP_HEADS) {
            if (HardcoreReviveModConfig.LOSE_INVENTORY)
                player.getInventory().clear();

            ItemStack playerHead = Items.PLAYER_HEAD.getDefaultStack();
            playerHead.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
            playerHead.set(DataComponentTypes.LORE, new LoreComponent(HardcoreReviveModUtil.LORE));

            ItemEntity playerHeadItemEntity = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), playerHead);
            playerHeadItemEntity.setToDefaultPickupDelay();
            world.spawnEntity(playerHeadItemEntity);
        }
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void ServerPlayerEntity_AfterDeath(DamageSource damageSource, CallbackInfo info) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        ServerWorld world = (ServerWorld)player.getWorld();
        MinecraftServer server = world.getServer();

        if (server.isHardcore() && !(player.isCreative() || player.isSpectator()) || HardcoreReviveModConfig.CREATIVE_PLAYERS_DROP_HEADS) {
            BlockPos deathPos = player.getBlockPos();
            String dimension = HardcoreReviveModUtil.capitalize(world.getDimensionEntry().getIdAsString());
            Style textStyle = Style.EMPTY.withItalic(true).withColor(Formatting.GRAY);
            player.sendMessage(Text.translatable("death.hardcorerevivemod.location.message", deathPos.getX(), deathPos.getY(), deathPos.getZ(), dimension).setStyle(textStyle));
        }
    }
}