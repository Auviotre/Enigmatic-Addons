package auviotre.enigmatic.addon.contents.gui;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.items.AntiqueBag;
import auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability;
import auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonMenus;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AntiqueBagContainerMenu extends AbstractContainerMenu {
    protected final Player player;
    public final LazyOptional<IAntiqueBagHandler> bagHandler;

    public AntiqueBagContainerMenu(int syncID, Inventory playerInv) {
        this(syncID, playerInv, ContainerLevelAccess.create(playerInv.player.level(), playerInv.player.blockPosition()));
    }

    public AntiqueBagContainerMenu(int syncID, Inventory playerInv, FriendlyByteBuf extras) {
        this(syncID, playerInv, ContainerLevelAccess.create(playerInv.player.level(), playerInv.player.blockPosition()));
    }

    private AntiqueBagContainerMenu(int id, Inventory Inventory, ContainerLevelAccess worldPosCallable) {
        this(EnigmaticAddonMenus.ANTIQUE_BAG_MENU, id, Inventory);
    }

    protected AntiqueBagContainerMenu(@Nullable MenuType<?> menuType, int id, @NotNull Inventory inventory) {
        super(menuType, id);
        this.player = inventory.player;
        this.bagHandler = SuperAddonHandler.getCapability(this.player, AntiqueBagCapability.INVENTORY);

        this.bagHandler.ifPresent(bagHandler -> {
            IItemHandlerModifiable bagInventory = bagHandler.getInventory();
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 6; x++) {
                    this.addSlot(new BookSlot(bagInventory, x + y * 6, x * 18 + 35, y * 18 + 24));
                }
            }
        });

        for (int k = 0; k < 3; ++k) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + k * 9 + 9, 8 + j * 18, 84 + k * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            if (k == this.player.getInventory().selected) {
                this.addSlot(new Slot(inventory, k, 8 + k * 18, 142) {
                    public boolean mayPickup(Player playerIn) {
                        return false;
                    }

                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    public int getMaxStackSize() {
                        return 0;
                    }
                });
            } else {
                this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
            }
        }
    }

    public ItemStack quickMoveStack(Player player, int id) {
        ItemStack stack = ItemStack.EMPTY;
        int slotsCount = AntiqueBagCapability.Wrapper.SLOTS_COUNT;
        Slot slot = this.slots.get(id);
        if (slot != null && slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            stack = slotItem.copy();
            if (id < slotsCount) {
                if (!this.moveItemStackTo(slotItem, slotsCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, 0, slotsCount, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            if (slotItem.getCount() == stack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, slotItem);
        }

        return stack;
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public static class Provider implements MenuProvider {
        public Provider() {
        }

        public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
            return new AntiqueBagContainerMenu(syncId, playerInv);
        }

        public Component getDisplayName() {
            return Component.translatable("gui.enigmaticaddons.antique_bag");
        }
    }

    public static class BookSlot extends SlotItemHandler {
        public BookSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return AntiqueBag.bookList.contains(ForgeRegistries.ITEMS.getKey(stack.getItem())) && super.mayPlace(stack);
        }

        @Nullable
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(EnigmaticAddons.MODID, "item/empty_book"));
        }
    }
}
