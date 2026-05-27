package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class HunterScroll extends ItemBaseCurio {
    public HunterScroll() {
        super(ItemBaseCurio.getDefaultProperties());
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    public static float getCountModifier(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        float count = stack.getOrCreateTag().getInt("HunterCount");
        return count / 100.0F;
    }

    public static float getSpecModifier(ItemStack stack) {
        if (!stack.hasTag()) return 0;
        ListTag tag = stack.getOrCreateTag().getList("HunterList", 10);
        double size = tag.size();
        size = Math.pow(size, 0.7);
        return Mth.floor(0.5 + size);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hunterScroll1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.hunterScroll2");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        List<Component> list = super.getAttributesTooltip(tooltips, stack);
        MutableComponent component = Component.translatable("tooltip.enigmaticaddons.hunterScrollAttribute");
        float count = getCountModifier(stack);
        int size = Mth.floor(getSpecModifier(stack));
        if (count >= 0.1)
            list.add(Component.translatable("attribute.modifier.plus.0", String.format("%.01f", count), component).withStyle(ChatFormatting.BLUE));
        if (size > 0)
            list.add(Component.translatable("attribute.modifier.plus.1", size, component).withStyle(ChatFormatting.BLUE));
        return list;
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = new ImmutableMultimap.Builder<>();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "Hunter Bonus", 0.5, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Mod.EventBusSubscriber(modid = EnigmaticLegacy.MODID)
    public static class Events {
        @SubscribeEvent(priority = EventPriority.LOW)
        public void onAttack(@NotNull LivingHurtEvent event) {
            LivingEntity victim = event.getEntity();
            if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return;
            if (SuperpositionHandler.hasCurio(victim, EnigmaticAddonItems.HUNTER_SCROLL)) {
                ItemStack curio = SuperpositionHandler.getCurioStack(victim, EnigmaticAddonItems.HUNTER_SCROLL);
                event.setAmount(event.getAmount() - getCountModifier(curio));
            }
        }

        @SubscribeEvent
        public void onDamage(@NotNull LivingDamageEvent event) {
            if (event.getAmount() >= Float.MAX_VALUE) return;
            LivingEntity victim = event.getEntity();
            if (event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) return;
            if (SuperpositionHandler.hasCurio(victim, EnigmaticAddonItems.HUNTER_SCROLL)) {
                ItemStack curio = SuperpositionHandler.getCurioStack(victim, EnigmaticAddonItems.HUNTER_SCROLL);
                event.setAmount(event.getAmount() * (1 - 0.01F * getSpecModifier(curio)));
            }
        }

        @SubscribeEvent
        public void onDeath(@NotNull LivingDeathEvent event) {
            LivingEntity victim = event.getEntity();
            if (!(victim instanceof Monster)) return;
            String s = victim.getType().getDescriptionId();
            if (event.getSource().getEntity() instanceof LivingEntity attacker && SuperpositionHandler.hasCurio(attacker, EnigmaticAddonItems.HUNTER_SCROLL)) {
                ItemStack curio = SuperpositionHandler.getCurioStack(attacker, EnigmaticAddonItems.HUNTER_SCROLL);
                int count = curio.hasTag() ? curio.getTag().getInt("HunterCount") : 0;
                curio.getOrCreateTag().putInt("HunterCount", Math.min(200, count + 1));
                ListTag list = curio.getOrCreateTag().getList("HunterList", 10);
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putString("id", s);
                if (!list.contains(compoundtag)) list.add(compoundtag);
                curio.addTagElement("HunterList", list);
            }
        }
    }
}
