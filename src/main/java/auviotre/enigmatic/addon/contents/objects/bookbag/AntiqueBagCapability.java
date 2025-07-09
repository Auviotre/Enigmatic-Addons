package auviotre.enigmatic.addon.contents.objects.bookbag;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AntiqueBagCapability {

    public static final Capability<IAntiqueBagHandler> INVENTORY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final ResourceLocation ID_INVENTORY = new ResourceLocation(EnigmaticAddons.MODID, "antique_bag");

    public static ICapabilityProvider createProvider(final LivingEntity livingEntity) {
        return new Provider(livingEntity);
    }

    public static class Wrapper implements IAntiqueBagHandler {
        public static final int SLOTS_COUNT = 12;
        IItemHandlerModifiable inventory = new ItemStackHandler(SLOTS_COUNT);
        LivingEntity owner;

        public Wrapper(final LivingEntity livingEntity) {
            this.owner = livingEntity;
            this.reset();
        }

        public void reset() {
            this.inventory = new ItemStackHandler(SLOTS_COUNT);
        }

        public int getSlots() {
            return SLOTS_COUNT;
        }

        public ItemStack getBook(int index) {
            return this.inventory.getStackInSlot(index);
        }

        public void setBook(int index, ItemStack book) {
            this.inventory.setStackInSlot(index, book);
        }

        public ItemStack findBook(Item book) {
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (stack.is(book)) return stack;
            }
            return ItemStack.EMPTY;
        }

        public boolean hasFlower() {
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (stack.is(EnigmaticAddonItems.ARTIFICIAL_FLOWER)) return true;
            }
            return false;
        }

        public void tickFlowers() {
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                ItemStack stack = this.inventory.getStackInSlot(i);
                if (stack.is(EnigmaticAddonItems.ARTIFICIAL_FLOWER)) {
                    stack.inventoryTick(owner.level(), owner, i, false);
                    stack.getOrCreateTag().putBoolean("FlowerEnable", false);
                    stack.getOrCreateTag().putInt("FlowerBagEnable", i);
                }
            }
        }

        public LivingEntity getOwner() {
            return owner;
        }

        public IItemHandlerModifiable getInventory() {
            return this.inventory;
        }

        public void setInventory(IItemHandlerModifiable list) {
            this.inventory = list;
        }

        public Tag writeTag() {
            CompoundTag compound = new CompoundTag();
            ListTag tagList = new ListTag();
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                CompoundTag tag = new CompoundTag();
                tag.put("Item", this.inventory.getStackInSlot(i).serializeNBT());
                tag.putInt("Slot", i);
                tagList.add(tag);
            }
            compound.put("AntiqueBag", tagList);
            return compound;
        }

        public void readTag(Tag nbt) {
            ListTag tagList = ((CompoundTag) nbt).getList("AntiqueBag", Tag.TAG_COMPOUND);
            if (!tagList.isEmpty()) {
                IItemHandlerModifiable itemStackHandler = new ItemStackHandler(SLOTS_COUNT);
                for (int i = 0; i < tagList.size(); i++) {
                    CompoundTag tag = tagList.getCompound(i);
                    int slotId = tag.getInt("Slot");
                    ItemStack stack = ItemStack.of(tag.getCompound("Item"));
                    itemStackHandler.setStackInSlot(slotId, stack);
                }
                this.setInventory(itemStackHandler);
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<Tag> {

        final LazyOptional<IAntiqueBagHandler> optional;
        final IAntiqueBagHandler handler;
        final LivingEntity owner;

        Provider(final LivingEntity livingEntity) {
            this.owner = livingEntity;
            this.handler = new Wrapper(this.owner);
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
            return INVENTORY.orEmpty(capability, this.optional);
        }

        public Tag serializeNBT() {
            return this.handler.writeTag();
        }

        public void deserializeNBT(Tag nbt) {
            this.handler.readTag(nbt);
        }
    }

}
