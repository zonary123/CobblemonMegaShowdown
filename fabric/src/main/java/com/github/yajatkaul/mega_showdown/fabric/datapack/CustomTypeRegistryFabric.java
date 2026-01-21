package com.github.yajatkaul.mega_showdown.fabric.datapack;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.data.JsonDataRegistry;
import com.cobblemon.mod.common.api.reactive.SimpleObservable;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.elemental.ElementalTypeTeraType;
import com.cobblemon.mod.common.battles.runner.graal.GraalShowdownService;
import com.cobblemon.mod.common.util.MiscUtilsKt;
import com.cobblemon.mod.relocations.graalvm.polyglot.Value;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.datapack.CustomTypeRegistry;
import com.github.yajatkaul.mega_showdown.mixin.TeraTypesAccessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomTypeRegistryFabric implements JsonDataRegistry<CustomTypeRegistryFabric.CustomTypeData> {
    public static final CustomTypeRegistryFabric INSTANCE = new CustomTypeRegistryFabric();

    private final SimpleObservable<CustomTypeRegistryFabric> observable = new SimpleObservable<>();

    private CustomTypeRegistryFabric() {}

    @Override
    public @NotNull ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "mega_showdown/custom_type");
    }

    @Override
    public @NotNull PackType getType() {
        return PackType.SERVER_DATA;
    }

    @Override
    public @NotNull Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    @Override
    public @NotNull TypeToken<CustomTypeData> getTypeToken() {
        return TypeToken.get(CustomTypeData.class);
    }

    @Override
    public @NotNull String getResourcePath() {
        return "mega_showdown/custom_type";
    }

    @Override
    public @NotNull SimpleObservable<CustomTypeRegistryFabric> getObservable() {
        return observable;
    }

    @Override
    public void sync(@NotNull ServerPlayer player) {
        // Custom types are synced as part of ElementalTypes, so no additional sync needed
    }

    @Override
    public void reload(@NotNull Map<ResourceLocation, ? extends CustomTypeData> data) {
        CustomTypeRegistry.customTypes.clear();

        data.forEach((identifier, typeData) -> {
            try {
                ElementalType newType = ElementalTypes.register(new ElementalType(
                        typeData.name,
                        Component.translatable(typeData.lang),
                        typeData.hue,
                        0,
                        ResourceLocation.fromNamespaceAndPath(Cobblemon.MODID, "ui/types.png"),
                        typeData.id
                ));

                CustomTypeRegistry.customTypes.put(newType.getShowdownId(), newType);

                // Create and register TeraType
                TeraType newTeraType = new ElementalTypeTeraType(newType);
                TeraTypesAccessor.getTypes().put(MiscUtilsKt.cobblemonResource(typeData.id), newTeraType);

                Cobblemon.INSTANCE.getShowdownThread().queue(showdownService -> {
                    if (showdownService instanceof GraalShowdownService service) {
                        Value receiveNewTypeDataFn = service.context.getBindings("js").getMember("receiveNewTypeData");
                        receiveNewTypeDataFn.execute(typeData.name, typeData.maxTypeMove, typeData.zTypeMove);
                    }
                    return Unit.INSTANCE;
                });
//                Cobblemon.LOGGER.info("Loaded custom type: {} ({})", identifier, typeData.name);
            } catch (Exception e) {
                Cobblemon.LOGGER.error("Error loading custom type {}: {}", identifier, e.getMessage());
            }
        });

        Cobblemon.LOGGER.info("Loaded {} custom elemental types", CustomTypeRegistry.customTypes.size());
        observable.emit(this);
    }

    public static class CustomTypeData {
        public String name;
        public String id;
        public int hue;
        public String lang;
        public String zTypeMove;
        public String maxTypeMove;

        public CustomTypeData(String name, String id, int hue, String text, String zTypeMove, String maxTypeMove) {
            this.name = name;
            this.id = id;
            this.hue = hue;
            this.lang = text;
            this.zTypeMove = zTypeMove;
            this.maxTypeMove = maxTypeMove;
        }
    }
}