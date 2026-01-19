package com.github.yajatkaul.mega_showdown.datapack;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.data.JsonDataRegistry;
import com.cobblemon.mod.common.api.pokemon.status.Statuses;
import com.cobblemon.mod.common.api.reactive.SimpleObservable;
import com.cobblemon.mod.common.pokemon.status.PersistentStatus;
import com.cobblemon.mod.common.util.MiscUtilsKt;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.cobblemon.status.DamageStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kotlin.ranges.IntRange;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CustomTypeStatusRegistry implements JsonDataRegistry<CustomTypeStatusRegistry.CustomStatusData> {
    public static final CustomTypeStatusRegistry INSTANCE = new CustomTypeStatusRegistry();

    private final SimpleObservable<CustomTypeStatusRegistry> observable = new SimpleObservable<>();

    private CustomTypeStatusRegistry() {
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "mega_showdown/custom_status");
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
    public @NotNull TypeToken<CustomStatusData> getTypeToken() {
        return TypeToken.get(CustomStatusData.class);
    }

    @Override
    public @NotNull String getResourcePath() {
        return "mega_showdown/custom_status";
    }

    @Override
    public @NotNull SimpleObservable<CustomTypeStatusRegistry> getObservable() {
        return observable;
    }

    @Override
    public void sync(@NotNull ServerPlayer player) {
        // Custom types are synced as part of ElementalTypes, so no additional sync needed
    }

    @Override
    public void reload(@NotNull Map<ResourceLocation, ? extends CustomStatusData> data) {
        data.forEach((identifier, typeData) -> {
            try {

                if (typeData.damageStatusData() != null) {
                    DamageStatus damageStatus = new DamageStatus(
                            MiscUtilsKt.cobblemonResource(typeData.name()),
                            typeData.id(),
                            typeData.applyMsg(),
                            typeData.removeMsg(),
                            new IntRange(typeData.minDur(), typeData.maxDur()),
                            typeData.damageStatusData().chance(),
                            typeData.damageStatusData().damagePercent(),
                            typeData.damageStatusData().healingAbility()
                    );

                    Statuses.registerStatus(damageStatus);
                } else {
                    PersistentStatus status = new PersistentStatus(
                            MiscUtilsKt.cobblemonResource(typeData.name()),
                            typeData.id(),
                            typeData.applyMsg(),
                            typeData.removeMsg(),
                            new IntRange(typeData.minDur(), typeData.maxDur())
                    );

                    Statuses.registerStatus(status);
                }

//                Cobblemon.LOGGER.info("Loaded custom statuses: {} ({})", identifier, typeData.name);
            } catch (Exception e) {
                Cobblemon.LOGGER.error("Error loading custom statuses {}: {}", identifier, e.getMessage());
            }
        });

        Cobblemon.LOGGER.info("Loaded {} custom elemental statuses", CustomTypeRegistry.customTypes.size());
        observable.emit(this);
    }

    public record CustomStatusData(
            String name,
            String id,
            String applyMsg,
            String removeMsg,
            int minDur,
            int maxDur,
            DamageStatusData damageStatusData
    ) {
    }

    public record DamageStatusData(int chance, double damagePercent, String healingAbility) {
    }
}