package com.github.yajatkaul.mega_showdown.block.block_entity.renderer;

import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository;
import com.cobblemon.mod.common.client.render.models.blockbench.blockentity.BlockEntityModel;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.block.block_entity.MegaStoneStandEntity;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.MegaStoneStandState;
import com.github.yajatkaul.mega_showdown.codec.teraHat.LayerCodec;
import com.github.yajatkaul.mega_showdown.render.LayerDataLoader;
import com.github.yajatkaul.mega_showdown.render.layerEntities.states.TeraHatState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import kotlin.Unit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Collections;
import java.util.Set;

public class MegaStoneStandRenderer implements BlockEntityRenderer<MegaStoneStandEntity> {
    private final RenderContext context = new RenderContext();
    private final ResourceLocation poserId = ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "mega_stone_stand");

    public MegaStoneStandRenderer(BlockEntityRendererProvider.Context context) {
        this.context.put(RenderContext.Companion.getRENDER_STATE(), RenderContext.RenderState.BLOCK);
        this.context.put(RenderContext.Companion.getDO_QUIRKS(), true);
    }

    @Override
    public void render(MegaStoneStandEntity blockEntity, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int overlay) {
        Set<String> aspects = Collections.emptySet();

        MegaStoneStandState state = blockEntity.state;
        state.setCurrentAspects(aspects);
        state.updatePartialTicks(tickDelta);

        PosableModel model = VaryingModelRepository.INSTANCE.getPoser(poserId, state);
        model.context = context;
        var texture = VaryingModelRepository.INSTANCE.getTexture(poserId, state);
        var vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(texture));
        model.setBufferProvider(multiBufferSource);
        state.setCurrentModel(model);

        context.put(RenderContext.Companion.getASPECTS(), aspects);
        context.put(RenderContext.Companion.getTEXTURE(), texture);
        context.put(RenderContext.Companion.getSPECIES(), poserId);
        context.put(RenderContext.Companion.getPOSABLE_STATE(), state);

        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);

        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()));

        poseStack.translate(-0.5, 0, -0.5);

        poseStack.translate(1.0, 0, 0);

        model.applyAnimations(
                null,
                state,
                0f,
                0f,
                state.getAnimationSeconds() * 20,
                0f,
                0f
        );
        model.render(context, poseStack, vertexConsumer, packedLight, overlay, -0x1);
        model.withLayerContext(
                multiBufferSource, state, VaryingModelRepository.INSTANCE.getLayers(poserId, state),
                () -> {
                    model.render(context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);
                    return Unit.INSTANCE;
                }
        );

        model.setDefault();
        poseStack.popPose();
    }
}
