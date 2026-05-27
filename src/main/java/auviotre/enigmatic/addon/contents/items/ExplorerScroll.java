package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.entities.ExplorerMarker;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ExplorerScroll extends ItemBaseCurio {
    public static Omniconfig.IntParameter distance;
    public static Omniconfig.IntParameter cooldown;

    public ExplorerScroll() {
        super(ItemBaseCurio.getDefaultProperties());
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("explorerScroll");
        distance = builder.min(1).max(10).getInt("effectiveRange", 5);
        cooldown = builder.min(160).max(800).getInt("cooldown", 320);
        builder.popPrefix();
    }

    public void trigger(Level level, ServerPlayer player) {
        if (player.getCooldowns().isOnCooldown(EnigmaticAddonItems.EXPLORER_SCROLL)) return;
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getCooldowns().addCooldown(EnigmaticAddonItems.EXPLORER_SCROLL, cooldown.getValue());
        BlockPos blockPos = player.blockPosition();
        int d = distance.getValue();
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(blockPos.offset(d, d, d), blockPos.offset(-d, -d, -d));
        for (BlockPos pos : iterable) {
            if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity blockEntity) {
                if (blockEntity.lootTable != null) {
                    ExplorerMarker explorerMarker = new ExplorerMarker(level, pos, player);
                    level.addFreshEntity(explorerMarker);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.explorerScroll");
            try {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.currentKeybind", ChatFormatting.LIGHT_PURPLE, KeyMapping.createNameSupplier("key.xpScroll").get().getString().toUpperCase());
            } catch (NullPointerException ignored) {
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        List<Component> list = super.getAttributesTooltip(tooltips, stack);
        list.add(Component.translatable("attribute.modifier.plus.1", 12, Component.translatable("tooltip.enigmaticaddons.explorerScrollAttr1")).withStyle(ChatFormatting.BLUE));
        list.add(Component.translatable("attribute.modifier.take.0", 1.2, Component.translatable("tooltip.enigmaticaddons.explorerScrollAttr2")).withStyle(ChatFormatting.BLUE));
        return list;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(uuid, "Explorer Bonus", 0.08, AttributeModifier.Operation.MULTIPLY_BASE));
        return builder.build();
    }

    @Mod.EventBusSubscriber(modid = EnigmaticAddons.MODID)
    public static class Events {
        @SubscribeEvent(priority = EventPriority.LOW)
        public void onFoodEat(@NotNull LivingFallEvent event) {
            LivingEntity entity = event.getEntity();
            if (SuperpositionHandler.hasCurio(entity, EnigmaticAddonItems.EXPLORER_SCROLL)) {
                event.setDistance(event.getDistance() - 1.2F);
            }
        }
    }
}
