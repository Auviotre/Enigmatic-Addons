package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ViolenceScroll extends ItemBaseCurio implements IEldritch {
    public static Omniconfig.PerhapsParameter attackSpeed;
    public static Omniconfig.PerhapsParameter entityReach;

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("ViolenceScroll");
        attackSpeed = builder.comment("Attack speed increase provided by *** for absorbed curse. Measured as percentage.").min(1).max(10).getPerhaps("AttackSpeedBoost", 4);
        entityReach = builder.comment("Entity reach range increase provided by *** for absorbed curse. Measured as percentage.").min(1).max(10).getPerhaps("EntityReachBoost", 3);
        builder.popPrefix();
    }
    public ViolenceScroll() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributesDefault = HashMultimap.create();
        int curseCount = Helper.getCurseCount(stack);
        attributesDefault.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("ff757a7d-89ca-4c86-ba12-2c6bc64670ab"), "attack_speed_modifier", curseCount * attackSpeed.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_BASE));
        attributesDefault.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(UUID.fromString("2bbb5508-d8d7-45fc-8158-6206c24f48c9"), "entity_reach_modifier", curseCount * entityReach.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_BASE));
        return attributesDefault;
    }


    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll1", ChatFormatting.GOLD, Helper.getStoreModifier(stack) + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll6");
            int curseCount = Helper.getCurseCount(stack);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollCount", ChatFormatting.GOLD, curseCount);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            if (curseCount > 0) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll7", ChatFormatting.GOLD, "+" + curseCount * attackSpeed.getValue().asPercentage() + "%");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll8", ChatFormatting.GOLD, "+" + curseCount * entityReach.getValue().asPercentage() + "%");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
             ItemLoreHelper.indicateWorthyOnesOnly(list);
            if (Minecraft.getInstance().player != null && SuperAddonHandler.isAbyssBoost(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.indicateCursedOnesOnly(list);
        }
    }


    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            player.getAttributes().addTransientAttributeModifiers(this.createAttributeMap(stack));
        }

    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            player.getAttributes().removeAttributeModifiers(this.createAttributeMap(stack));
        }

    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack) && context.entity() instanceof Player player) {
            return SuperpositionHandler.isTheWorthyOne(player);
        }
        return false;
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this)) {
            ItemStack curio = SuperpositionHandler.getCurioStack(entity, this);
            UUID uuid = event.getEntity().getUUID();
            UUID storeUUID = Helper.getStoreUUID(curio);
            if (uuid.equals(storeUUID)) {
                double addon = Helper.getStoreDamage(curio) * Helper.getStoreModifier(curio) * 0.01F;
                event.setAmount(event.getAmount() + (float) addon);
            }
            System.out.print(entity.invulnerableTime + "\n");
            if (entity.invulnerableTime > 0) {
                event.setAmount(event.getAmount() * 3);
                entity.invulnerableTime = 0;
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttackLast(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this)) {
            ItemStack curio = SuperpositionHandler.getCurioStack(entity, this);
            UUID uuid = event.getEntity().getUUID();
            UUID storeUUID = Helper.getStoreUUID(curio);
            if (!uuid.equals(storeUUID)) Helper.setUUID(curio, uuid);
            else {
                Helper.setModifier(curio, Helper.getStoreModifier(curio) + 1 + Helper.getCurseCount(curio) * 0.8);
                Helper.setDamage(curio, event.getAmount());
            }
        }
    }

    public static class Helper {
        public static double getStoreModifier(ItemStack stack) {
            return stack.hasTag() ? stack.getTag().getDouble("VModifier") : 2.5;
        }
        public static float getStoreDamage(ItemStack stack) {
            return stack.hasTag() ? stack.getTag().getFloat("VDamage") : 0.0F;
        }
        public static UUID getStoreUUID(ItemStack stack) {
            return stack.hasTag() && stack.getTag().hasUUID("VTarget") ? stack.getTag().getUUID("VTarget") : null;
        }
        public static void setModifier(ItemStack stack, double modifier) {
            stack.getOrCreateTag().putDouble("VModifier", modifier);
        }
        public static void setDamage(ItemStack stack, float damage) {
            stack.getOrCreateTag().putDouble("VDamage", damage);
        }
        public static void setUUID(ItemStack stack, UUID uuid) {
            stack.getOrCreateTag().putUUID("VTarget", uuid);
            setModifier(stack, 2.5);
            setDamage(stack, 0.0F);
        }
        public static void addCurse(ItemStack stack, Set<Enchantment> enchantmentSet) {
            ListTag tag = stack.getOrCreateTag().getList("AbsorbedCurses", 10);
            for (Enchantment enchantment : enchantmentSet) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putString("id", enchantment.getDescriptionId());
                if (!tag.contains(compoundtag)) tag.add(compoundtag);
            }
            stack.addTagElement("AbsorbedCurses", tag);
        }
        public static int getCurseCount(ItemStack stack) {
            if (!stack.hasTag()) return 0;
            ListTag tag = stack.getOrCreateTag().getList("AbsorbedCurses", 10);
            return Math.min(tag.size(), 30);
        }
    }
}
