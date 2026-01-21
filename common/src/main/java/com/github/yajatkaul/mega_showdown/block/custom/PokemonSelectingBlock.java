package com.github.yajatkaul.mega_showdown.block.custom;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.callback.PartySelectCallbacks;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.github.yajatkaul.mega_showdown.item.custom.PokemonSelectingBlockItem;
import kotlin.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PokemonSelectingBlock extends Block {
    private final ResourceLocation id;
    private final boolean canUseInBattle;

    public PokemonSelectingBlock(Properties properties, ResourceLocation id, boolean canUseInBattle) {
        super(properties);
        this.id = id;
        this.canUseInBattle = canUseInBattle;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Item item = BuiltInRegistries.ITEM.get(this.id);
        PokemonBattle battle = BattleRegistry.getBattleByParticipatingPlayer((ServerPlayer) player);

        if (!canUseInBattle && battle != null) return InteractionResult.FAIL;

        if (item instanceof PokemonSelectingBlockItem pokemonSelectingItem) {
            if (player instanceof ServerPlayer serverPlayer) {
                PartySelectCallbacks.INSTANCE.createFromPokemon(
                        serverPlayer,
                        PlayerExtensionsKt.party(serverPlayer).toGappyList().stream().filter(Objects::nonNull).toList(),
                        pokemon -> pokemonSelectingItem.canUseOnPokemon(pokemonSelectingItem.getDefaultInstance(), pokemon),
                        pokemon -> {
                            pokemonSelectingItem.applyToPokemon(serverPlayer, pokemonSelectingItem.getDefaultInstance(), pokemon);
                            return Unit.INSTANCE;
                        }
                );
            }
        }

        return InteractionResult.SUCCESS;
    }
}
