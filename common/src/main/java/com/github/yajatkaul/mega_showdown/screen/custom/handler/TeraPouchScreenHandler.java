package com.github.yajatkaul.mega_showdown.screen.custom.handler;

import com.github.yajatkaul.mega_showdown.components.InventoryStorage;
import com.github.yajatkaul.mega_showdown.components.MegaShowdownDataComponents;
import com.github.yajatkaul.mega_showdown.screen.MegaShowdownMenuTypes;
import com.github.yajatkaul.mega_showdown.screen.custom.slot.TeraShardSlots;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TeraPouchScreenHandler extends AbstractContainerMenu {
    private final ItemStack pouch;
    private final InventoryStorage inventoryStorage;
    private final SimpleContainer teraInv;
    private final Level level;
    private final int columns = 9;
    private final int rows = 2;

    public TeraPouchScreenHandler(int id, Inventory inv) {
        this(id, inv, ItemStack.EMPTY, inv.player.level());
    }

    public TeraPouchScreenHandler(int id, Inventory inv, ItemStack pouch, Level level) {
        super(MegaShowdownMenuTypes.TERA_POUCH_MENU.get(), id);
        this.pouch = pouch;
        this.level = level;

        this.inventoryStorage = pouch.getOrDefault(MegaShowdownDataComponents.INVENTORY.get(), InventoryStorage.defaultStorage(this.rows * this.columns + 1));

        if (level != null) {
            this.teraInv = inventoryStorage.getInventory(level.registryAccess());
        } else {
            this.teraInv = new SimpleContainer(rows * columns + 1);
        }

        addStorageInventory();
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player playerIn, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (invSlot < this.teraInv.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.teraInv.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.teraInv.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return newStack;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (slot.hasItem() && slot.getItem() == this.pouch) {
                return;
            }
        }

        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (level != null) {
            this.pouch.set(MegaShowdownDataComponents.INVENTORY.get(), this.inventoryStorage.save(level.registryAccess(), this.teraInv));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private void addStorageInventory() {
        int startX = 8;
        int startY = 18;

        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.columns; col++) {
                int index = col + row * 9;
                int x = startX + col * 18;
                int y = startY + row * 18;

                this.addSlot(new TeraShardSlots(this.teraInv, index, x, y));
            }
        }

        this.addSlot(new TeraShardSlots(this.teraInv, rows * columns, 80, 54));
    }
}
