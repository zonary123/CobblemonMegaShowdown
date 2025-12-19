package com.github.yajatkaul.mega_showdown.screen.custom.slot;

import com.github.yajatkaul.mega_showdown.item.MegaShowdownItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TeraShardSlots extends Slot {
    public TeraShardSlots(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return switch (index) {
            case 0 -> stack.is(MegaShowdownItems.BUG_TERA_SHARD.get());
            case 1 -> stack.is(MegaShowdownItems.FIGHTING_TERA_SHARD.get());
            case 2 -> stack.is(MegaShowdownItems.FAIRY_TERA_SHARD.get());
            case 3 -> stack.is(MegaShowdownItems.ELECTRIC_TERA_SHARD.get());
            case 4 -> stack.is(MegaShowdownItems.DRAGON_TERA_SHARD.get());
            case 5 -> stack.is(MegaShowdownItems.DARK_TERA_SHARD.get());
            case 6 -> stack.is(MegaShowdownItems.WATER_TERA_SHARD.get());
            case 7 -> stack.is(MegaShowdownItems.FLYING_TERA_SHARD.get());
            case 8 -> stack.is(MegaShowdownItems.FIRE_TERA_SHARD.get());
            case 9 -> stack.is(MegaShowdownItems.GHOST_TERA_SHARD.get());
            case 10 -> stack.is(MegaShowdownItems.GRASS_TERA_SHARD.get());
            case 11 -> stack.is(MegaShowdownItems.GROUND_TERA_SHARD.get());
            case 12 -> stack.is(MegaShowdownItems.ICE_TERA_SHARD.get());
            case 13 -> stack.is(MegaShowdownItems.NORMAL_TERA_SHARD.get());
            case 14 -> stack.is(MegaShowdownItems.POISON_TERA_SHARD.get());
            case 15 -> stack.is(MegaShowdownItems.PSYCHIC_TERA_SHARD.get());
            case 16 -> stack.is(MegaShowdownItems.ROCK_TERA_SHARD.get());
            case 17 -> stack.is(MegaShowdownItems.STEEL_TERA_SHARD.get());
            case 18 -> stack.is(MegaShowdownItems.STELLAR_TERA_SHARD.get());
            default -> false;
        };
    }
}