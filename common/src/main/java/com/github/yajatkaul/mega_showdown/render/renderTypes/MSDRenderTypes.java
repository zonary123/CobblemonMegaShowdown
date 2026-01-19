package com.github.yajatkaul.mega_showdown.render.renderTypes;

import com.github.yajatkaul.mega_showdown.render.TeraMapLoader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class MSDRenderTypes {
    public static ShaderInstance teraFire;
    public static ShaderInstance teraWater;
    public static ShaderInstance teraGrass;
    public static ShaderInstance teraElectric;
    public static ShaderInstance teraIce;
    public static ShaderInstance teraFighting;
    public static ShaderInstance teraPoison;
    public static ShaderInstance teraGround;
    public static ShaderInstance teraFlying;
    public static ShaderInstance teraPsychic;
    public static ShaderInstance teraBug;
    public static ShaderInstance teraRock;
    public static ShaderInstance teraGhost;
    public static ShaderInstance teraDragon;
    public static ShaderInstance teraDark;
    public static ShaderInstance teraSteel;
    public static ShaderInstance teraFairy;
    public static ShaderInstance teraNormal;
    public static ShaderInstance teraStellar;

    public static RenderType pokemonShader(ResourceLocation texture, String tera_aspect) {
        return RenderType.create(
                "tera_crystal_" + tera_aspect,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(
                                () -> TeraMapLoader.getColorShaderMap(TeraMapLoader.REGISTRY.get(tera_aspect)))
                        )
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(RenderType.LIGHTMAP)
                        .setOverlayState(RenderType.OVERLAY)
                        .createCompositeState(true)
        );
    }
}
