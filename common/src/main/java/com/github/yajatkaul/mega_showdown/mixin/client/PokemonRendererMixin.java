package com.github.yajatkaul.mega_showdown.mixin.client;

import com.cobblemon.mod.common.client.entity.PokemonClientDelegate;
import com.cobblemon.mod.common.client.render.MatrixWrapper;
import com.cobblemon.mod.common.client.render.models.blockbench.PosableModel;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockActiveAnimation;
import com.cobblemon.mod.common.client.render.models.blockbench.bedrock.animation.BedrockAnimationRepository;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.RenderContext;
import com.cobblemon.mod.common.client.render.models.blockbench.repository.VaryingModelRepository;
import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.DmaxHatState;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.TeraCrystalState;
import com.github.yajatkaul.mega_showdown.block.block_entity.renderer.state.TeraHatState;
import com.github.yajatkaul.mega_showdown.codec.teraHat.HatCodec;
import com.github.yajatkaul.mega_showdown.config.MegaShowdownConfig;
import com.github.yajatkaul.mega_showdown.render.HatsDataLoader;
import com.github.yajatkaul.mega_showdown.render.renderTypes.MSDRenderTypes;
import com.github.yajatkaul.mega_showdown.utils.PokemonBehaviourHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import kotlin.Unit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(value = PokemonRenderer.class)
public class PokemonRendererMixin {
    @Unique
    private final RenderContext mega_showdown$context = new RenderContext();
    @Unique
    private final ResourceLocation mega_showdown$teraPoserId = ResourceLocation.fromNamespaceAndPath("cobblemon", "tera_hat");
    @Unique
    private final TeraHatState mega_showdown$teraHatState = new TeraHatState();
    @Unique
    private final Set<String> mega_showdown$teraAspects = new HashSet<>();

    @Unique
    private final ResourceLocation mega_showdown$dmaxPoserId = ResourceLocation.fromNamespaceAndPath("cobblemon", "dmax_clouds");
    @Unique
    public final DmaxHatState mega_showdown$dmaxHatState = new DmaxHatState();
    @Unique
    private final Set<String> mega_showdown$dmaxAspects = new HashSet<>();

    @Unique
    private final ResourceLocation mega_showdown$teraCrystalPoserId = ResourceLocation.fromNamespaceAndPath("cobblemon", "terastal_transformation");
    @Unique
    public final TeraCrystalState mega_showdown$teraCrystalState = new TeraCrystalState();
    @Unique
    private final Set<String> mega_showdown$teraCrystalAspects = new HashSet<>();

    @Unique
    private boolean mega_showdown$teraCrystalPlayed = false;
    @Unique
    private boolean mega_showdown$teraCrystalPass = false;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.mega_showdown$context.put(RenderContext.Companion.getRENDER_STATE(), RenderContext.RenderState.WORLD);
        this.mega_showdown$context.put(RenderContext.Companion.getDO_QUIRKS(), true);
    }

    @Inject(method = "render*", at = @At(value = "TAIL"), cancellable = true)
    public void render(PokemonEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        PokemonClientDelegate clientDelegate = (PokemonClientDelegate) entity.getDelegate();

        Pokemon pokemon = entity.getPokemon();
        boolean tera_play = pokemon.getAspects().contains("play_tera");
        Optional<String> aspect = pokemon.getAspects().stream()
                .filter(a -> a.startsWith("msd:tera_")).findFirst();

        if (tera_play && (!mega_showdown$teraCrystalPlayed || mega_showdown$teraCrystalPass) && aspect.isPresent()) {
            mega_showdown$renderTeraCrystals(
                    entity,
                    pokemon,
                    clientDelegate,
                    partialTicks,
                    poseStack,
                    buffer,
                    packedLight
            );
            if (!mega_showdown$teraCrystalPass) {
                ci.cancel();
                return;
            }
        }

        boolean dmax_aspect = pokemon.getAspects().contains("msd:dmax");

        if (aspect.isPresent()) {
            if (MegaShowdownConfig.teraHats) {
                mega_showdown$renderTeraHats(aspect.get(), pokemon, clientDelegate, partialTicks, poseStack, buffer, packedLight);
            }
            PokemonBehaviourHelper.Companion.snowStormPartileSpawner(
                    entity,
                    ResourceLocation.fromNamespaceAndPath("cobblemon", "tera_glitter"),
                    List.of("root"),
                    null,
                    null
            );
        }

        if (dmax_aspect) {
            mega_showdown$renderDmaxClouds(pokemon, clientDelegate, partialTicks, poseStack, buffer, packedLight);
        }
    }

    @Unique
    private void mega_showdown$renderTeraCrystals(PokemonEntity entity, Pokemon pokemon, PokemonClientDelegate clientDelegate, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float mega_showdown$teraCrystalDuration = new BedrockActiveAnimation(
                BedrockAnimationRepository.INSTANCE.getAnimation("terastal_transformation", "animation.terastal_transformation.transform")
        ).getDuration();

        if (mega_showdown$teraCrystalState.getAnimationSeconds() >= mega_showdown$teraCrystalDuration) {
            mega_showdown$teraCrystalPlayed = true;
            mega_showdown$teraCrystalPass = false;
            mega_showdown$teraCrystalState.resetAnimation();
            entity.after(3f, () -> {
                mega_showdown$teraCrystalPlayed = false;
                return Unit.INSTANCE;
            });
            return;
        } else if (mega_showdown$teraCrystalState.getAnimationSeconds() >= mega_showdown$teraCrystalDuration - 0.3) {
            mega_showdown$teraCrystalPass = true;
        }

        mega_showdown$teraCrystalState.setCurrentAspects(mega_showdown$teraCrystalAspects);
        mega_showdown$teraCrystalState.updatePartialTicks(partialTicks);

        Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
        MatrixWrapper rootLocator = locatorStates.get("root");

        if (rootLocator == null) return;

        HatCodec crystalSize = HatsDataLoader.REGISTRY.get(ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, pokemon.getSpecies().getName().toLowerCase(Locale.ROOT)));

        // Get model and texture
        PosableModel model = VaryingModelRepository.INSTANCE.getPoser(mega_showdown$teraCrystalPoserId, mega_showdown$teraCrystalState);
        model.context = mega_showdown$context;
        ResourceLocation texture = VaryingModelRepository.INSTANCE.getTexture(mega_showdown$teraCrystalPoserId, mega_showdown$teraCrystalState);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(texture));

        model.setBufferProvider(buffer);
        mega_showdown$teraCrystalState.setCurrentModel(model);

        // Setup context
        mega_showdown$context.put(RenderContext.Companion.getASPECTS(), mega_showdown$teraCrystalAspects);
        mega_showdown$context.put(RenderContext.Companion.getTEXTURE(), texture);
        mega_showdown$context.put(RenderContext.Companion.getSPECIES(), mega_showdown$teraCrystalPoserId);
        mega_showdown$context.put(RenderContext.Companion.getPOSABLE_STATE(), mega_showdown$teraCrystalState);

        poseStack.pushPose();

        poseStack.mulPose(rootLocator.getMatrix());
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(0.08, 0.0, 0.0);

        poseStack.scale(1.5f, 1.5f, 1.5f);

        if (crystalSize != null) {
            List<Float> scale = HatCodec.getScaleForHat(pokemon, "msd:tera_crystal", crystalSize);
            poseStack.scale(scale.get(0), scale.get(1), scale.get(2));
        }

        // Apply animations
        model.applyAnimations(
                null,
                mega_showdown$teraCrystalState,
                0F, 0F, 0F, 0F,
                mega_showdown$teraCrystalState.getAnimationSeconds() * 20
        );

        // Render
        model.render(mega_showdown$context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);

        model.withLayerContext(
                buffer,
                mega_showdown$teraCrystalState,
                VaryingModelRepository.INSTANCE.getLayers(mega_showdown$teraCrystalPoserId, mega_showdown$teraCrystalState),
                () -> {
                    model.render(mega_showdown$context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);
                    return Unit.INSTANCE;
                }
        );
        model.setDefault();
        poseStack.popPose();
    }

    @Unique
    private void mega_showdown$renderDmaxClouds(Pokemon pokemon, PokemonClientDelegate clientDelegate, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        mega_showdown$dmaxHatState.setCurrentAspects(mega_showdown$dmaxAspects);
        mega_showdown$dmaxHatState.updatePartialTicks(partialTicks);

        Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
        MatrixWrapper headLocator = locatorStates.get("head");

        if (headLocator == null) return;

        HatCodec dynamaxHatCodec = HatsDataLoader.REGISTRY.get(ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, pokemon.getSpecies().getName().toLowerCase(Locale.ROOT)));

        // Get model and texture
        PosableModel model = VaryingModelRepository.INSTANCE.getPoser(mega_showdown$dmaxPoserId, mega_showdown$dmaxHatState);
        model.context = mega_showdown$context;
        ResourceLocation texture = VaryingModelRepository.INSTANCE.getTexture(mega_showdown$dmaxPoserId, mega_showdown$dmaxHatState);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(texture));

        model.setBufferProvider(buffer);
        mega_showdown$dmaxHatState.setCurrentModel(model);

        // Setup context
        mega_showdown$context.put(RenderContext.Companion.getASPECTS(), mega_showdown$dmaxAspects);
        mega_showdown$context.put(RenderContext.Companion.getTEXTURE(), texture);
        mega_showdown$context.put(RenderContext.Companion.getSPECIES(), mega_showdown$dmaxPoserId);
        mega_showdown$context.put(RenderContext.Companion.getPOSABLE_STATE(), mega_showdown$dmaxHatState);

        poseStack.pushPose();

        poseStack.mulPose(headLocator.getMatrix());
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(0.08, 0.0, 0.0);

        if (dynamaxHatCodec != null) {
            List<Float> scale = HatCodec.getScaleForHat(pokemon, "msd:dmax", dynamaxHatCodec);
            poseStack.scale(scale.get(0), scale.get(1), scale.get(2));
        }

        // Apply animations
        model.applyAnimations(
                null,
                mega_showdown$dmaxHatState,
                0F, 0F, 0F, 0F,
                mega_showdown$dmaxHatState.getAnimationSeconds() * 20
        );

        // Render
        model.render(mega_showdown$context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);

        model.withLayerContext(
                buffer,
                mega_showdown$dmaxHatState,
                VaryingModelRepository.INSTANCE.getLayers(mega_showdown$dmaxPoserId, mega_showdown$dmaxHatState),
                () -> {
                    model.render(mega_showdown$context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);
                    return Unit.INSTANCE;
                }
        );
        model.setDefault();
        poseStack.popPose();
    }

    @Unique
    private void mega_showdown$renderTeraHats(String aspect, Pokemon pokemon, PokemonClientDelegate clientDelegate, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (pokemon.getSpecies().getName().equals("Terapagos")) return;

        Map<String, MatrixWrapper> locatorStates = clientDelegate.getLocatorStates();
        MatrixWrapper headLocator = locatorStates.get("head");
        if (headLocator == null) return;

        poseStack.pushPose();

        HatCodec teraHatCodec = HatsDataLoader.REGISTRY.get(ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, pokemon.getSpecies().getName().toLowerCase(Locale.ROOT)));

        poseStack.mulPose(headLocator.getMatrix());
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(0.08, 0.0, 0.0);

        if (teraHatCodec != null) {
            List<Float> scale = HatCodec.getScaleForHat(pokemon, aspect, teraHatCodec);
            poseStack.scale(scale.get(0), scale.get(1), scale.get(2));
        }

        // Update state BEFORE getting model
        mega_showdown$teraAspects.clear(); // Clear and re-add
        mega_showdown$teraAspects.add(aspect);
        mega_showdown$teraHatState.setCurrentAspects(mega_showdown$teraAspects);
        mega_showdown$teraHatState.updatePartialTicks(partialTicks);

        // Get model and texture
        PosableModel model = VaryingModelRepository.INSTANCE.getPoser(mega_showdown$teraPoserId, mega_showdown$teraHatState);
        ResourceLocation texture = VaryingModelRepository.INSTANCE.getTexture(mega_showdown$teraPoserId, mega_showdown$teraHatState);

        model.context = mega_showdown$context;
        model.setBufferProvider(buffer);
        mega_showdown$teraHatState.setCurrentModel(model);

        // Setup context
        mega_showdown$context.put(RenderContext.Companion.getASPECTS(), mega_showdown$teraAspects);
        mega_showdown$context.put(RenderContext.Companion.getTEXTURE(), texture);
        mega_showdown$context.put(RenderContext.Companion.getSPECIES(), mega_showdown$teraPoserId);
        mega_showdown$context.put(RenderContext.Companion.getPOSABLE_STATE(), mega_showdown$teraHatState);

        // Apply animations
        model.applyAnimations(
                null,
                mega_showdown$teraHatState,
                0F,
                0F,
                0F,
                0F,
                mega_showdown$teraHatState.getAnimationSeconds() * 20
        );

        // Render
        VertexConsumer vertexConsumer;
        if (MegaShowdownConfig.legacyTeraEffect) {
            vertexConsumer = buffer.getBuffer(RenderType.entityCutout(texture));
        } else {
            vertexConsumer = buffer.getBuffer(MSDRenderTypes.pokemonShader(texture));
        }

        model.render(mega_showdown$context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);

        model.withLayerContext(
                buffer,
                mega_showdown$teraHatState,
                VaryingModelRepository.INSTANCE.getLayers(mega_showdown$teraPoserId, mega_showdown$teraHatState),
                () -> {
                    model.render(mega_showdown$context, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -0x1);
                    return Unit.INSTANCE;
                }
        );
        model.setDefault();

        poseStack.popPose();
    }
}
