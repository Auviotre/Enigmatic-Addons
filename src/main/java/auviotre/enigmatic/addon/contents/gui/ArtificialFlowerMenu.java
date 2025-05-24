package auviotre.enigmatic.addon.contents.gui;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.items.ArtificialFlower;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonMenus;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ArtificialFlowerMenu extends AbstractContainerMenu {
    public final Player player;
    private final ItemStackHandler enchantSlot;
    private final ItemStackHandler ringSlot;
    private final ContainerLevelAccess access;
    public int costMode;

    public ArtificialFlowerMenu(int syncID, Inventory playerInv) {
        this(syncID, playerInv, ContainerLevelAccess.create(playerInv.player.level(), playerInv.player.blockPosition()));
    }

    public ArtificialFlowerMenu(int syncID, Inventory playerInv, FriendlyByteBuf extras) {
        this(syncID, playerInv, ContainerLevelAccess.create(playerInv.player.level(), playerInv.player.blockPosition()));
    }

    private ArtificialFlowerMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        this(EnigmaticAddonMenus.ARTIFICIAL_FLOWER_MENU, id, inventory, access);
    }

    protected ArtificialFlowerMenu(@Nullable MenuType<?> menuType, int id, @NotNull Inventory inventory, ContainerLevelAccess access) {
        super(menuType, id);
        this.costMode = 0;
        this.player = inventory.player;
        this.enchantSlot = new ItemStackHandler(2);
        this.ringSlot = new ItemStackHandler(1);
        this.access = access;
        ItemStack flowerStack = ArtificialFlower.Helper.getFlowerStack(player, true);
        if (flowerStack.getOrCreateTag().contains("MagicRing")) {
            CompoundTag magicRing = flowerStack.getOrCreateTag().getCompound("MagicRing");
            ringSlot.setStackInSlot(0, ItemStack.of(magicRing));
        }

        this.addSlot(new ExactSlot(this.enchantSlot, Items.LAPIS_LAZULI, 0, 17, 31));
        this.addSlot(new ExactSlot(this.enchantSlot, Items.QUARTZ, 1, 106, 31));
        this.addSlot(new RingSlot(this.ringSlot, 0, 80, 27));

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

    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 3) {
            ItemStack lapis = this.enchantSlot.getStackInSlot(0);
            if (lapis.isEmpty() && !player.getAbilities().instabuild) return false;
            int cost = this.costMode == 0 ? 2 : this.costMode == 1 ? 4 : 8;
            if (lapis.getCount() < cost && !player.getAbilities().instabuild) return false;
            this.access.execute((level, pos) -> {
                if (!level.isClientSide()) {
                    ArtificialFlower.Helper.randomAttribute(player, ArtificialFlower.Helper.getFlowerStack(player, false), id + 1, this.costMode, !this.ringSlot.getStackInSlot(0).isEmpty());
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                    if (!player.getAbilities().instabuild) {
                        lapis.shrink(cost);
                        if (lapis.isEmpty()) this.enchantSlot.setStackInSlot(0, ItemStack.EMPTY);
                    }
                }
            });
            return true;
        } else if (id == 3) {
            this.costMode = this.costMode == 2 ? 0 : this.costMode + 1;
            return true;
        } else if (id == 4 || id == 5) {
            ItemStack quartz = this.enchantSlot.getStackInSlot(1);
            if (quartz.isEmpty() && !player.getAbilities().instabuild) return false;
            if (quartz.getCount() < 4 && !player.getAbilities().instabuild) return false;
            this.access.execute((level, pos) -> {
                if (!level.isClientSide()) {
                    ArtificialFlower.Helper.randomEffect(player, ArtificialFlower.Helper.getFlowerStack(player, false), id - 4);
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
                    if (!player.getAbilities().instabuild) {
                        quartz.shrink(4);
                        if (quartz.isEmpty()) this.enchantSlot.setStackInSlot(1, ItemStack.EMPTY);
                    }
                }
            });
            return true;
        }
        return false;
    }

    public ItemStack quickMoveStack(Player player, int id) {
        ItemStack stack = ItemStack.EMPTY;
        int slotsCount = 3;
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

    public boolean valid(int index) {
        if (index > 1) return false;
        boolean[] flag = {
                this.enchantSlot.getStackInSlot(0).getCount() >= (this.costMode == 0 ? 2 : this.costMode == 1 ? 4 : 8),
                this.enchantSlot.getStackInSlot(1).getCount() >= 4
        };
        return !this.enchantSlot.getStackInSlot(index).isEmpty() && flag[index];
    }

    public boolean hasRing() {
        return !this.ringSlot.getStackInSlot(0).isEmpty();
    }

    public void removed(Player player) {
        if (player instanceof ServerPlayer) {
            for (int i = 0; i < 2; i++) {
                ItemStack stack = this.enchantSlot.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    if (player.isAlive() && !((ServerPlayer) player).hasDisconnected())
                        player.getInventory().placeItemBackInInventory(stack);
                    else player.drop(stack, false);
                    this.enchantSlot.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            ItemStack flowerStack = ArtificialFlower.Helper.getFlowerStack(player, false);
            UUID uuid = UUID.randomUUID();
            player.getPersistentData().putUUID("FlowerEnableUUID", uuid);
            flowerStack.getOrCreateTag().putUUID("FlowerUUID", uuid);
            flowerStack.getOrCreateTag().putBoolean("FlowerEnable", true);
            ItemStack ring = this.ringSlot.getStackInSlot(0);
            if (!ring.isEmpty()) {
                CompoundTag save = ring.save(new CompoundTag());
                flowerStack.getOrCreateTag().put("MagicRing", save);
            } else flowerStack.getOrCreateTag().remove("MagicRing");
        }
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public static class Provider implements MenuProvider {
        public Provider() {
        }

        public AbstractContainerMenu createMenu(int syncId, Inventory playerInv, Player player) {
            return new ArtificialFlowerMenu(syncId, playerInv);
        }

        public Component getDisplayName() {
            return Component.empty();
        }
    }

    public static class ExactSlot extends SlotItemHandler {
        private final Item item;

        public ExactSlot(IItemHandler itemHandler, Item item, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.item = item;
        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.is(item) && super.mayPlace(stack);
        }

        @Nullable
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            String location = item.equals(Items.LAPIS_LAZULI) ? "slots/empty_lapis_slot" : "slots/empty_quartz_slot";
            return Pair.of(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(EnigmaticAddons.MODID, location));
        }
    }

    public static class RingSlot extends SlotItemHandler {
        public RingSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.is(EnigmaticAddonItems.QUARTZ_RING) && super.mayPlace(stack);
        }

        public void setChanged() {
            super.setChanged();
        }

        public boolean isHighlightable() {
            return false;
        }
    }
}
