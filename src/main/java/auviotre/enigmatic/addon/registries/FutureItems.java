package auviotre.enigmatic.addon.registries;

import com.aizistral.enigmaticlegacy.items.DarkArmor;
import com.aizistral.enigmaticlegacy.items.GemOfBinding;
import com.aizistral.enigmaticlegacy.items.LivingFlame;
import com.aizistral.enigmaticlegacy.items.WormholePotion;
import com.aizistral.enigmaticlegacy.registries.AbstractRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FutureItems extends AbstractRegistry<Item> {
    @ObjectHolder(value = "enigmaticlegacy:dark_helmet", registryName = "item")
    public static final DarkArmor DARK_HELMET = null;
    @ObjectHolder(value = "enigmaticlegacy:dark_chestplate", registryName = "item")
    public static final DarkArmor DARK_CHESTPLATE = null;
    @ObjectHolder(value = "enigmaticlegacy:dark_leggings", registryName = "item")
    public static final DarkArmor DARK_LEGGINGS = null;
    @ObjectHolder(value = "enigmaticlegacy:dark_boots", registryName = "item")
    public static final DarkArmor DARK_BOOTS = null;
    @ObjectHolder(value = "enigmaticlegacy:living_flame", registryName = "item")
    public static final LivingFlame LIVING_FLAME = null;
    @ObjectHolder(value = "enigmaticlegacy:gem_of_binding", registryName = "item")
    public static final GemOfBinding GEM_OF_BINDING = null;
    @ObjectHolder(value = "enigmaticlegacy:wormhole_potion", registryName = "item")
    public static final WormholePotion WORMHOLE_POTION = null;
    private static final FutureItems INSTANCE = new FutureItems();

    private FutureItems() {
        super(ForgeRegistries.ITEMS);
        this.register("living_flame", () -> new LivingFlame() {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
        this.register("gem_of_binding", () -> new GemOfBinding() {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
        this.register("wormhole_potion", () -> new WormholePotion() {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
        this.register("dark_helmet", () -> new DarkArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.HELMET) {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
        this.register("dark_chestplate", () -> new DarkArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.CHESTPLATE) {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
        this.register("dark_leggings", () -> new DarkArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.LEGGINGS) {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
        this.register("dark_boots", () -> new DarkArmor(ArmorMaterials.NETHERITE, ArmorItem.Type.BOOTS) {
            public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
                super.appendHoverText(stack, world, list, flag);
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future1"));
                list.add(Component.translatable("tooltip.enigmaticaddons.legacy_future2"));
            }
        });
    }
}
