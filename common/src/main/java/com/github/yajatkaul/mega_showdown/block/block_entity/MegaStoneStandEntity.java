package com.github.yajatkaul.mega_showdown.block.block_entity;

import com.github.yajatkaul.mega_showdown.block.MegaShowdownBlockEntities;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.MegaStoneStandState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MegaStoneStandEntity extends BlockEntity {
    public final MegaStoneStandState state = new MegaStoneStandState();

    public MegaStoneStandEntity(BlockPos blockPos, BlockState blockState) {
        super(MegaShowdownBlockEntities.MEGA_STONE_STAND_BLOCK_ENTITY.get(), blockPos, blockState);
    }
}
