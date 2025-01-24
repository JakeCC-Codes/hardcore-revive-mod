package net.jakeccz.hrm.mixin;

import com.mojang.authlib.GameProfile;
import net.jakeccz.hrm.advancement.CriteriaRegistry;
import net.jakeccz.hrm.util.HardcoreReviveModConfig;
import net.jakeccz.hrm.util.HardcoreReviveModConfig.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;

@Mixin(PlayerSkullBlock.class)
public class PlayerSkullBlockMixin extends SkullBlock {
    @Nullable
    private static BlockPattern revivalPattern;
    @Nullable
    private static BlockPattern failedRevivalPattern;

    public PlayerSkullBlockMixin(SkullType skullType, Settings settings) {
        super(skullType, settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity) {
            ProfileComponent playerHead = itemStack.get(DataComponentTypes.PROFILE);
            if (playerHead == null) { return; }
            tryRevivePlayer(world, pos, playerHead.gameProfile());
        }
    }

    private static void tryRevivePlayer(World world, BlockPos pos, GameProfile gameProfile) {
        if (world.isClient || pos.getY() < world.getBottomY()) { return; }
        BlockPattern.Result result = getRitualPattern().searchAround(world, pos);
        if (result != null) {
            MinecraftServer server = world.getServer();
            if (server == null) { return; }

            BlockPos spawnPos = result.translate(1, 1, 1).getBlockPos();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(gameProfile.getId());
            PlayerEntity summoningPlayer = world.getClosestPlayer(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 12, false);
            if (player != null && player.isSpectator()) {
                ServerWorld serverWorld = server.getWorld(world.getRegistryKey());
                if (serverWorld != null) {
                    spawnLightning(serverWorld, spawnPos);
                    spawnPlayer(serverWorld, result, player, summoningPlayer, spawnPos);
                    if (summoningPlayer == null) { return; }
                    CriteriaRegistry.REVIVE_PLAYER.trigger(server.getPlayerManager().getPlayer(summoningPlayer.getUuid()));
                } else {
                    spawnError(Text.translatable("text.actionbar.summoner.error.offline"), world, spawnPos, summoningPlayer);
                }
            } else if (player != null && !player.isSpectator()) {
                spawnError(Text.translatable("text.actionbar.summoner.error.alive"), world, spawnPos, summoningPlayer);
            } else {
                spawnError(Text.translatable("text.actionbar.summoner.error.offline"), world, spawnPos, summoningPlayer);
            }

        } else {
            BlockPattern.Result incompleteResult = getFailedRitualPattern().searchAround(world, pos);
            if (incompleteResult != null) {
                BlockPos spawnPos = incompleteResult.translate(1, 1, 1).getBlockPos();
                PlayerEntity summoningPlayer = world.getClosestPlayer(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 6, false);
                spawnError(Text.translatable("text.actionbar.summoner.error.missing"), world, spawnPos, summoningPlayer);
            }
        }

    }

    private static void spawnLightning(World world, BlockPos pos) {
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.STRUCTURE);
        if (lightning != null) {
            lightning.refreshPositionAndAngles((double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5, 0.0F, 0.0F);
            world.spawnEntity(lightning);
        }
    }

    private static void spawnPlayer(ServerWorld world, BlockPattern.Result patternResult, ServerPlayerEntity player, @Nullable PlayerEntity summoningPlayer, BlockPos pos) {
        breakRitualPatternBlocks(world, patternResult);
        player.teleport(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.05, (double)pos.getZ() + 0.5, Set.of(),  0f, 0f, true);
        if (summoningPlayer != null) { player.lookAtEntity(EntityAnchorArgumentType.EntityAnchor.FEET, summoningPlayer, EntityAnchorArgumentType.EntityAnchor.FEET); }
        player.changeGameMode(GameMode.SURVIVAL);
        updateRitualPatternBlocks(world, patternResult);

        int durationTicks = 100; // 5 secs
        player.clearStatusEffects();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, durationTicks, 70, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, durationTicks));
        world.sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING);
        CriteriaRegistry.REVIVE_PLAYER.trigger(player);
    }

    private static void spawnError(Text errorMessage, World world, BlockPos pos, PlayerEntity summoningPlayer) {
        if (summoningPlayer != null) {
            summoningPlayer.sendMessage(errorMessage, true);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.4f, 20f);
        world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX(), pos.getY(), pos.getZ(), 0.0D, 0.2D, 0.0D);
    }

    private static void breakRitualPatternBlocks(World world, BlockPattern.Result patternResult) {
        int deadzone = HardcoreReviveModConfig.LEAVE_STRUCTURE_BASE ? 1 : 0;
        for (int i = 0; i < patternResult.getWidth(); i++) {
            for (int j = 0; j < patternResult.getHeight() -deadzone; j++) {
                for (int k = 0; k < patternResult.getDepth(); k++) {
                    CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, k);
                    world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
                }
            }
        }
    }

    private static void updateRitualPatternBlocks(World world, BlockPattern.Result patternResult) {
        int deadzone = HardcoreReviveModConfig.LEAVE_STRUCTURE_BASE ? 1 : 0;
        for (int i = 0; i < patternResult.getWidth(); i++) {
            for (int j = 0; j < patternResult.getHeight() -deadzone; j++) {
                for (int k = 0; k < patternResult.getDepth(); k++) {
                    CachedBlockPosition cachedBlockPosition = patternResult.translate(i, j, k);
                    world.updateNeighbors(cachedBlockPosition.getBlockPos(), Blocks.AIR);
                }
            }
        }
    }

    private static BlockPattern getRitualPattern() {
        if (revivalPattern == null) {
            revivalPattern = BlockPatternBuilder.start()
                    .aisle("~~~", "!~!", "#@#")
                    .aisle("~^~", "~%~", "@*@")
                    .aisle("~~~", "!~!", "#@#")
                    .where('~', pos -> pos.getBlockState().isAir())
                    .where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.PLAYER_HEAD).or(BlockStatePredicate.forBlock(Blocks.PLAYER_WALL_HEAD))))
                    .where('!', pos -> CustomBlockTags.FLOWER_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('#', pos -> CustomBlockTags.SOUL_SAND_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('@', pos -> CustomBlockTags.STAIR_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('%', pos -> CustomBlockTags.FENCE_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('*', pos -> CustomBlockTags.ORE_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .build();
        }

        return revivalPattern;
    }

    private static BlockPattern getFailedRitualPattern() {
        if (failedRevivalPattern == null) {
            failedRevivalPattern = BlockPatternBuilder.start()
                    .aisle("~~~", "~~~", "#@#")
                    .aisle("~^~", "~%~", "@*@")
                    .aisle("~~~", "~~~", "#@#")
                    .where('~', pos -> pos.getBlockState().isAir())
                    .where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.PLAYER_HEAD).or(BlockStatePredicate.forBlock(Blocks.PLAYER_WALL_HEAD))))
                    .where('#', pos -> CustomBlockTags.SOUL_SAND_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('@', pos -> CustomBlockTags.STAIR_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('%', pos -> CustomBlockTags.FENCE_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .where('*', pos -> CustomBlockTags.ORE_BLOCKS.contains(pos.getBlockState().getBlock()))
                    .build();
        }

        return failedRevivalPattern;
    }
}
