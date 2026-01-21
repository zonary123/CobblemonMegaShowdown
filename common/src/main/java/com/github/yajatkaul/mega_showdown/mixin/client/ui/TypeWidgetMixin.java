package com.github.yajatkaul.mega_showdown.mixin.client.ui;

import com.cobblemon.mod.common.api.gui.GuiUtilsKt;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.client.gui.summary.widgets.type.TypeWidget;
import com.github.yajatkaul.mega_showdown.MegaShowdown;
import com.github.yajatkaul.mega_showdown.datapack.CustomTypeRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.*;

@Mixin(value = TypeWidget.class, remap = false)
public class TypeWidgetMixin {

    @Shadow
    @Final
    private static ResourceLocation typeResource;

    @Unique
    private static double OFFSET = 0.5;

    /**
     * @author Yajat Kaul
     * @reason no reason xp
     */
    @Overwrite(remap = true)
    public final void renderType(ElementalType type, PoseStack poseStack, int pX, int pY) {
        TypeWidget self = (TypeWidget) (Object) this;
        float diameter = 36;

        if (CustomTypeRegistry.customTypes.containsKey(type.getShowdownId())) {
            GuiUtilsKt.blitk(
                    poseStack,
                    ResourceLocation.fromNamespaceAndPath(MegaShowdown.MOD_ID, "textures/gui/types/" + type.getShowdownId().toLowerCase() + ".png"),
                    (pX * 2) + OFFSET,
                    pY * 2,
                    diameter, diameter,
                    diameter,
                    0,
                    diameter, diameter,
                    0,
                    1, 1, 1, 1,
                    true, 0.5f
            );
        } else {
            GuiUtilsKt.blitk(
                    poseStack,
                    typeResource,
                    pX + OFFSET,
                    pY,
                    self.getHeight(), self.getWidth(),
                    self.getWidth() * type.getTextureXMultiplier() + 0.1,
                    0,
                    self.getWidth() * 18, self.getHeight(),
                    0,
                    1, 1, 1, 1,
                    true, 1
            );
        }
    }
}
