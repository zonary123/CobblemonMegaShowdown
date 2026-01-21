package com.github.yajatkaul.mega_showdown.render;

import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.render.renderTypes.MSDRenderTypes;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TeraMapLoader implements ResourceManagerReloadListener {
    public static final HashMap<String, String> REGISTRY = new HashMap<>();
    private static final String DIRECTORY = "mega_showdown/tera_map";

    public record TeraMap(Map<String, String> colorMap) {
        public static Codec<TeraMap> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("aspectShaderMap").forGetter(TeraMap::colorMap)
        ).apply(instance, TeraMap::new));
    }

    public static void load() {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        REGISTRY.clear();

        Collection<ResourceLocation> resources =
                rm.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).keySet();

        for (ResourceLocation id : resources) {
            try (var stream = rm.getResource(id).get().open()) {
                TeraMap codec = TeraMap.CODEC.parse(
                        JsonOps.INSTANCE,
                        JsonParser.parseReader(new InputStreamReader(stream))
                ).result().orElseThrow();

                REGISTRY.putAll(codec.colorMap);
            } catch (Exception e) {
                MegaShowdown.LOGGER.error("Failed loading tera map JSON: {}", id, e);
            }
        }

        MegaShowdown.LOGGER.info("Loaded {} custom tera map", REGISTRY.size());
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        load();
        return ResourceManagerReloadListener.super.reload(preparationBarrier, resourceManager, profilerFiller, profilerFiller2, executor, executor2);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public @NotNull String getName() {
        return "mega_showdown";
    }

    public static ShaderInstance getColorShaderMap(String color) {
        return switch (color) {
            case "red" -> MSDRenderTypes.teraFire;
            case "blue" -> MSDRenderTypes.teraWater;
            case "green" -> MSDRenderTypes.teraGrass;
            case "yellow" -> MSDRenderTypes.teraElectric;
            case "brown" -> MSDRenderTypes.teraGround;
            case "light_blue" -> MSDRenderTypes.teraFlying;
            case "purple" -> MSDRenderTypes.teraDragon;
            case "pink" -> MSDRenderTypes.teraFairy;
            case "black" -> MSDRenderTypes.teraDark;
            case "gray" -> MSDRenderTypes.teraSteel;
            case "light_grey" -> MSDRenderTypes.teraIce;
            case "orange" -> MSDRenderTypes.teraFighting;
            case "lime" -> MSDRenderTypes.teraBug;
            case "teal" -> MSDRenderTypes.teraPoison;
            case "indigo" -> MSDRenderTypes.teraGhost;
            case "magenta" -> MSDRenderTypes.teraPsychic;
            case "tan" -> MSDRenderTypes.teraRock;
            case "navy" -> MSDRenderTypes.teraNormal;
            case "white" -> MSDRenderTypes.teraStellar;
            default -> {
                MegaShowdown.LOGGER.error("Unknown tera shader color '{}'", color);
                yield MSDRenderTypes.teraStellar;
            }
        };
    }
}