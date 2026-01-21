package com.github.yajatkaul.mega_showdown.mixin.client.ui;

import com.cobblemon.mod.common.api.gui.GuiUtilsKt;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.gui.TypeIcon;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.datapack.CustomTypeRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(value = TypeIcon.class, remap = false)
public class TypeIconMixin {
    @Unique
    private final int TYPE_ICON_DIAMETER = 36;
    @Unique
    private final float SCALE = 0.5F;

    @Shadow
    @Final
    private static ResourceLocation typesResource;
    @Shadow
    @Final
    private static ResourceLocation smallTypesResource;

    @Shadow
    @Final
    private Number x;
    @Shadow
    @Final
    private Number y;
    @Shadow
    @Final
    private ElementalType type;
    @Shadow
    @Final
    @Nullable
    private ElementalType secondaryType;
    @Shadow
    @Final
    private boolean centeredX;
    @Shadow
    @Final
    private boolean small;
    @Shadow
    @Final
    private float secondaryOffset;
    @Shadow
    @Final
    private float doubleCenteredOffset;
    @Shadow
    @Final
    private float opacity;

    /**
     * @author YajatKaul
     * @reason Please dont judge me i cant do this big of a project alone now
     */
    @Overwrite(remap = true)
    public final void render(GuiGraphics context) {
        float diameter = small ? (TYPE_ICON_DIAMETER / 2f) : TYPE_ICON_DIAMETER;
        float offsetX = centeredX
                ? (((diameter / 2f) * SCALE) + (secondaryType != null ? doubleCenteredOffset : 0f))
                : 0f;

        if (secondaryType != null) {
            if (CustomTypeRegistry.customTypes.containsKey(secondaryType.getShowdownId())) {
                GuiUtilsKt.blitk(
                        context.pose(),
                        small ? ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "textures/gui/types/" + secondaryType.getShowdownId().toLowerCase() + "_small.png")
                                : ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "textures/gui/types/" + secondaryType.getShowdownId().toLowerCase() + ".png"),
                        (x.floatValue() + secondaryOffset - offsetX) / SCALE,
                        y.floatValue() / SCALE,
                        diameter,
                        diameter,
                        diameter,
                        0,
                        diameter,
                        diameter,
                        0,
                        1,
                        1,
                        1,
                        opacity,
                        true,
                        SCALE
                );
            } else {
                GuiUtilsKt.blitk(
                        context.pose(),
                        small ? smallTypesResource : typesResource,
                        (x.floatValue() + secondaryOffset - offsetX) / SCALE,
                        y.floatValue() / SCALE,
                        diameter,
                        diameter,
                        diameter * secondaryType.getTextureXMultiplier() + 0.1,
                        0,
                        diameter * 18,
                        diameter,
                        0,
                        1,
                        1,
                        1,
                        opacity,
                        true,
                        SCALE
                );
            }
        }

        if (CustomTypeRegistry.customTypes.containsKey(type.getShowdownId())) {
            GuiUtilsKt.blitk(
                    context.pose(),
                    small ? ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "textures/gui/types/" + type.getShowdownId().toLowerCase() + "_small.png")
                            : ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "textures/gui/types/" + type.getShowdownId().toLowerCase() + ".png"),
                    (x.floatValue() - offsetX) / SCALE,
                    y.floatValue() / SCALE,
                    diameter,
                    diameter,
                    diameter,
                    0,
                    diameter,
                    diameter,
                    0,
                    1,
                    1,
                    1,
                    opacity,
                    true,
                    SCALE
            );
        } else {
            GuiUtilsKt.blitk(
                    context.pose(),
                    small ? smallTypesResource : typesResource,
                    (x.floatValue() - offsetX) / SCALE,
                    y.floatValue() / SCALE,
                    diameter,
                    diameter,
                    (diameter * type.getTextureXMultiplier()) + 0.1f,
                    0f,
                    diameter * 18f,
                    diameter,
                    0f,
                    1f,
                    1f,
                    1f,
                    opacity,
                    true,
                    SCALE
            );
        }
    }
}