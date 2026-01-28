package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.IEldritch;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

import static com.aizistral.enigmaticlegacy.items.CursedRing.*;

public class ViolenceScroll extends ItemBaseCurio implements IEldritch {
    public static Omniconfig.PerhapsParameter invulnerableModifier;
    public static Omniconfig.PerhapsParameter invulnerableHealMultiplier;
    public static Omniconfig.PerhapsParameter boostPerCurseModifier;
    public static Omniconfig.PerhapsParameter baseCurseModifier;
    public static Omniconfig.PerhapsParameter attackSpeed;
    public static Omniconfig.PerhapsParameter entityReach;
    public static Omniconfig.DoubleParameter knockbackResistance;
    public static Omniconfig.IntParameter maxDurability;

    public ViolenceScroll() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("CurseofViolence");
        baseCurseModifier = builder.comment("The base damage boost for curse absorbed. Measured as percentage.").getPerhaps("BaseCurseModifier", 10);
        boostPerCurseModifier = builder.comment("The damage boost per every curse absorbed. Measured as percentage.").max(100).getPerhaps("BoostPerCurseModifier", 5);
        invulnerableModifier = builder.comment("Attack Damage Modifier when attack during the invulnerable time provided by the Curse of Violence. Measured as percentage.").min(100).getPerhaps("InvulnerableAttackModifier", 200);
        invulnerableHealMultiplier = builder.comment("The heal multiplier when attack during the invulnerable time provided by the Curse of Violence. Measured as percentage.").max(100).getPerhaps("InvulnerableHealMultiplier", 40);
        attackSpeed = builder.comment("Attack speed increase provided by the Curse of Violence for absorbed curse. Measured as percentage.").min(1).max(10).getPerhaps("AttackSpeedBoost", 4);
        entityReach = builder.comment("Entity reach range increase provided by the Curse of Violence for absorbed curse. Measured as percentage.").min(1).max(10).getPerhaps("EntityReachBoost", 3);
        knockbackResistance = builder.comment("Knockback Resistance increase provided by the Curse of Violence for absorbed curse.").getDouble("KnockbackResistanceBoost", 0.025);
        maxDurability = builder.comment("The max energy count of the Curse of Violence.").min(100).getInt("MaxDurability", 200);
        builder.popPrefix();
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> attributesDefault = HashMultimap.create();
        int curseCount = Helper.getCurseCount(stack);
        attributesDefault.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("ff757a7d-89ca-4c86-ba12-2c6bc64670ab"), "attack_speed_modifier", curseCount * attackSpeed.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_BASE));
        attributesDefault.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.fromString("f1b26193-c0bb-40fe-91df-31b38f037643"), "knockback_resistance_modifier", curseCount * knockbackResistance.getValue(), AttributeModifier.Operation.ADDITION));
        attributesDefault.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(UUID.fromString("2bbb5508-d8d7-45fc-8158-6206c24f48c9"), "entity_reach_modifier", curseCount * entityReach.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_BASE));
        return attributesDefault;
    }


    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        int curseCount = Helper.getCurseCount(stack);
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll1", ChatFormatting.GOLD, String.format("%.01f%%", Helper.getStoreModifier(stack)));
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll3", ChatFormatting.GOLD, "+" + invulnerableModifier + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll4", ChatFormatting.GOLD, invulnerableHealMultiplier + "%");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll5");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll6");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollCount", ChatFormatting.GOLD, curseCount);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            if (curseCount > 0) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll7", ChatFormatting.GOLD, "+" + curseCount * attackSpeed.getValue().asPercentage() + "%");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll8", ChatFormatting.GOLD, "+" + curseCount * entityReach.getValue().asPercentage() + "%");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll9", ChatFormatting.GOLD, String.format("+%.02f", curseCount * knockbackResistance.getValue()));
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
            ItemLoreHelper.indicateWorthyOnesOnly(list);
            if (Minecraft.getInstance().player != null && SuperAddonHandler.isAbyssBoost(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.abyssBoost");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll10");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScroll11");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollLore1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollLore2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollLore3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollLore4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.violenceScrollCount", ChatFormatting.GOLD, curseCount);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.indicateCursedOnesOnly(list);
        }
    }

    public void inventoryTick(ItemStack stack, Level world, Entity entity, int i, boolean selected) {
        if (entity instanceof Player player && SuperpositionHandler.isTheWorthyOne(player)) {
            Helper.setOwner(stack, player);
            Map<Enchantment, Integer> curses = EnchantmentHelper.getEnchantments(stack);
            curses.keySet().removeIf(enchantment -> !enchantment.isCurse());
            if (!curses.isEmpty()) {
                Map<Enchantment, Integer> leftover = EnchantmentHelper.getEnchantments(stack);
                leftover.keySet().removeIf(Enchantment::isCurse);
                ViolenceScroll.Helper.addCurse(stack, curses.keySet());
                ViolenceScroll.Helper.addDurability(stack, 15 * curses.size());
                EnchantmentHelper.setEnchantments(leftover, stack);
            }
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            Helper.setOwner(stack, player);
            Map<Enchantment, Integer> curses = EnchantmentHelper.getEnchantments(stack);
            curses.keySet().removeIf(enchantment -> !enchantment.isCurse());
            if (!curses.isEmpty()) {
                Map<Enchantment, Integer> leftover = EnchantmentHelper.getEnchantments(stack);
                leftover.keySet().removeIf(Enchantment::isCurse);
                ViolenceScroll.Helper.addCurse(stack, curses.keySet());
                ViolenceScroll.Helper.addDurability(stack, 15 * curses.size());
                EnchantmentHelper.setEnchantments(leftover, stack);
            }
            player.getAttributes().addTransientAttributeModifiers(this.createAttributeMap(stack));
        }
    }

    public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            Helper.setDamage(stack, 0.0F);
            player.getAttributes().removeAttributeModifiers(this.createAttributeMap(stack));
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack) && context.entity() instanceof Player player)
            return SuperpositionHandler.isTheWorthyOne(player);
        return false;
    }

    public boolean isBarVisible(ItemStack stack) {
        return Helper.getOwnerUUID(stack) != null;
    }

    public int getBarWidth(ItemStack stack) {
        return Math.round((float) Helper.getDurability(stack) * 13.0F / maxDurability.getValue());
    }

    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, (float) Helper.getDurability(stack) / maxDurability.getValue());
        return 255 << 24 | (int) (f * 128.0F + 127) << 16;
    }

    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (!SuperpositionHandler.isTheWorthyOne(player)) return super.overrideStackedOnOther(stack, slot, action, player);
        if (action != ClickAction.PRIMARY && slot.mayPlace(stack) && slot.mayPickup(player) && slot.hasItem()) {
            ItemStack other = slot.getItem();
            if (Helper.canDisenchant(stack, other)) {
                slot.set(Helper.disenchant(stack, other));
                if (player.level().isClientSide)
                    player.level().playSound(player, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.2F + (float)Math.random() * 0.4F);
                return true;
            }
        }
        return super.overrideStackedOnOther(stack, slot, action, player);
    }

    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (!SuperpositionHandler.isTheWorthyOne(player)) return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
        if (action != ClickAction.PRIMARY && slot.mayPlace(stack) && slot.mayPickup(player) && !other.isEmpty()) {
            if (Helper.canDisenchant(stack, other)) {
                access.set(Helper.disenchant(stack, other));
                if (player.level().isClientSide)
                    player.level().playSound(player, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.2F + (float)Math.random() * 0.4F);
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(stack, other, slot, action, player, access);
    }

    @SubscribeEvent
    public void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isAlive() || entity.level().isClientSide) return;
        if (entity.getPersistentData().getBoolean("ViolenceSevenCurses") && !SuperpositionHandler.hasCurio(entity, EnigmaticItems.CURSED_RING)) {
            Multimap<Attribute, AttributeModifier> attributeMap = ArrayListMultimap.create();
            attributeMap.put(Attributes.ARMOR, new AttributeModifier(UUID.fromString("457d0ac3-69e4-482f-b636-22e0802da6bd"), "enigmaticlegacy:armor_modifier", -armorDebuff.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
            attributeMap.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(UUID.fromString("95e70d83-3d50-4241-a835-996e1ef039bb"), "enigmaticlegacy:armor_toughness_modifier", -armorDebuff.getValue().asModifier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
            entity.getAttributes().addTransientAttributeModifiers(attributeMap);

            List<LivingEntity> genericMobs = entity.level().getEntitiesOfClass(LivingEntity.class, SuperpositionHandler.getBoundingBoxAroundEntity(entity, neutralAngerRange.getValue()));
            for (LivingEntity mob : genericMobs) {
                double visibility = entity.getVisibilityPercent(mob);
                double angerDistance = Math.max(neutralAngerRange.getValue() * visibility, neutralXRayRange.getValue());
                if (!entity.hasLineOfSight(mob) && !(entity.distanceTo(mob) <= neutralXRayRange.getValue())) continue;
                if (mob.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) <= angerDistance * angerDistance) {
                    if (mob instanceof NeutralMob neutral) {
                        if (!neutralAngerBlacklist.contains(ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()))) {
                            if (neutral instanceof TamableAnimal tamable && tamable.isTame()) continue;
                            if (neutral == entity) continue;
                            if ((neutral.getTarget() == null || !neutral.getTarget().isAlive()) && (entity.hasLineOfSight(mob) || entity.distanceTo(mob) <= neutralXRayRange.getValue())) {
                                neutral.setTarget(entity);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (event.getSource().getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this)) {
            ItemStack curio = SuperpositionHandler.getCurioStack(entity, this);
            UUID uuid = victim.getUUID();
            UUID storeUUID = Helper.getStoreUUID(curio);
            if (uuid.equals(storeUUID)) {
                double addon = Helper.getStoreDamage(curio) * Helper.getStoreModifier(curio) * 0.01F;
                if (!(entity instanceof Player player) || !player.isCreative()) {
                    if (entity instanceof Player player && SuperAddonHandler.isAbyssBoost(player)) {
                        if (entity.getRandom().nextInt(4) == 0) Helper.addDurability(curio, -1);
                    } else if (entity.getRandom().nextInt(3) == 0) Helper.addDurability(curio, -1);
                }
                event.setAmount(event.getAmount() + (float) addon);
                if (Helper.getDurability(curio) == 0 && entity.getRandom().nextInt(8) == 0) {
                    CuriosApi.getCuriosInventory(entity).ifPresent(curiosItemHandler -> {
                        int slots = curiosItemHandler.getEquippedCurios().getSlots();
                        for (int i = 0; i < slots; i++) {
                            ItemStack stackInSlot = curiosItemHandler.getEquippedCurios().getStackInSlot(i);
                            if (entity.getRandom().nextInt(4) == 0 && EnchantmentHelper.getEnchantments(stackInSlot).keySet().stream().anyMatch(Enchantment::isCurse)) {
                                Map<Enchantment, Integer> leftover = EnchantmentHelper.getEnchantments(stackInSlot);
                                final boolean[] yummy = {true};
                                leftover.keySet().removeIf(enchant -> {
                                    if (yummy[0] && enchant.isCurse()) {
                                        yummy[0] = false;
                                        return true;
                                    }
                                    return false;
                                });
                                EnchantmentHelper.setEnchantments(leftover, stackInSlot);
                                Helper.addDurability(curio, 30);
                            }
                        }
                    });
                }
            } else Helper.setUUID(curio, uuid);
            if (entity.invulnerableTime > 0) {
                entity.heal(event.getAmount() * invulnerableHealMultiplier.getValue().asModifier());
                event.setAmount(event.getAmount() * invulnerableModifier.getValue().asModifier(true));
                entity.invulnerableTime = 0;
            }
        }
        if (event.getSource().getEntity() instanceof LivingEntity entity && entity.getPersistentData().getBoolean("ViolenceSevenCurses") && !SuperpositionHandler.hasCurio(victim, EnigmaticItems.CURSED_RING)) {
            event.setAmount(event.getAmount() * (1 - monsterDamageDebuff.getValue().asModifier() / 2));
        }
        if (victim.getPersistentData().getBoolean("ViolenceSevenCurses") && !SuperpositionHandler.hasCurio(victim, EnigmaticItems.CURSED_RING)) {
            event.setAmount(event.getAmount() * ((painMultiplier.getValue().asModifier() - 1) / 2 + 1));
        }
        if (event.getEntity() instanceof Monster || event.getEntity() instanceof EnderDragon) {
            Mob mob = (Mob) event.getEntity();
            if (event.getSource().getEntity() instanceof Player player && SuperpositionHandler.hasCurio(player, this) && SuperAddonHandler.isAbyssBoost(player)) {
                if (!player.getMainHandItem().is(EnigmaticItems.THE_TWIST) && !player.getMainHandItem().is(EnigmaticItems.THE_INFINITUM) && !player.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN)) {
                    float modifier = monsterDamageDebuff.getValue().asModifier();
                    event.setAmount(event.getAmount() / (1 - modifier) * (1 - modifier / 2));
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttackLast(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity entity && SuperpositionHandler.hasCurio(entity, this)) {
            ItemStack curio = SuperpositionHandler.getCurioStack(entity, this);
            UUID uuid = event.getEntity().getUUID();
            UUID storeUUID = Helper.getStoreUUID(curio);
            if (uuid.equals(storeUUID)) Helper.setDamage(curio, event.getAmount());
            if (entity instanceof Player player && SuperAddonHandler.isAbyssBoost(player))
                event.getEntity().getPersistentData().putBoolean("ViolenceSevenCurses", true);
        }
    }

    public static class Helper {
        public static void addDurability(ItemStack stack, int d) {
            int durability = getDurability(stack);
            stack.getOrCreateTag().putInt("Durability", Mth.clamp(durability + d, 0, maxDurability.getValue()));
        }

        public static int getDurability(ItemStack stack) {
            return stack.hasTag() ? stack.getTag().getInt("Durability") : 0;
        }
        public static UUID getOwnerUUID(ItemStack stack) {
            return stack.hasTag() && stack.getTag().hasUUID("LastHolder") ? stack.getTag().getUUID("LastHolder") : null;
        }

        public static void setOwner(ItemStack stack, Player player) {
            stack.getOrCreateTag().putUUID("LastHolder", player.getUUID());
        }

        public static double getStoreModifier(ItemStack stack) {
            int curseCount = getCurseCount(stack);
            int minus = 0;
            float apply = curseCount * boostPerCurseModifier.getValue().asPercentage();
            apply *= (float) getDurability(stack) / maxDurability.getValue();
            for (int i = 5; i <= curseCount; i += 5) minus += curseCount / i;
            return apply + baseCurseModifier.getValue().asPercentage() - minus;
        }

        public static float getStoreDamage(ItemStack stack) {
            return stack.hasTag() ? stack.getTag().getFloat("VDamage") : 0.0F;
        }

        public @Nullable static UUID getStoreUUID(ItemStack stack) {
            return stack.hasTag() && stack.getTag().hasUUID("VTarget") ? stack.getTag().getUUID("VTarget") : null;
        }

        public static void setDamage(ItemStack stack, float damage) {
            stack.getOrCreateTag().putDouble("VDamage", damage);
        }

        public static void setUUID(ItemStack stack, UUID uuid) {
            stack.getOrCreateTag().putUUID("VTarget", uuid);
            setDamage(stack, 0.0F);
        }

        public static void addCurse(ItemStack stack, Collection<Enchantment> enchantmentSet) {
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
            return tag.size();
        }

        private static ItemStack disenchant(ItemStack scroll, ItemStack target) {
            ItemStack item = target.copy();
            if (target.getItem() instanceof EnchantedBookItem bookItem) {
                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                boolean hasLeft = false;
                ListTag enchantments = EnchantedBookItem.getEnchantments(target);
                List<Enchantment> theCurse = new ArrayList<>();
                for (int i = 0; i < enchantments.size(); ++i) {
                    CompoundTag compoundtag = enchantments.getCompound(i);
                    int level = EnchantmentHelper.getEnchantmentLevel(compoundtag);
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(EnchantmentHelper.getEnchantmentId(compoundtag));
                    if (enchant.isCurse()) theCurse.add(enchant);
                    else {
                        hasLeft = true;
                        EnchantedBookItem.addEnchantment(book, new EnchantmentInstance(enchant, level));
                    }
                }
                ViolenceScroll.Helper.addCurse(scroll, theCurse);
                ViolenceScroll.Helper.addDurability(scroll, 20 + 30 * theCurse.size());
                return hasLeft ? book : Items.BOOK.getDefaultInstance();
            }
            Map<Enchantment, Integer> transposed = EnchantmentHelper.getEnchantments(target);
            Map<Enchantment, Integer> leftover = EnchantmentHelper.getEnchantments(target);
            transposed.keySet().removeIf(enchantment -> !enchantment.isCurse());
            leftover.keySet().removeIf(Enchantment::isCurse);
            ViolenceScroll.Helper.addCurse(scroll, transposed.keySet());
            ViolenceScroll.Helper.addDurability(scroll, 20 + 40 * transposed.size());
            EnchantmentHelper.setEnchantments(leftover, item);
            return item;
        }

        private static boolean canDisenchant(ItemStack scroll, ItemStack target) {
            Objects.requireNonNull(scroll.getItem());
            if (target.getItem() instanceof EnchantedBookItem bookItem) {
                ListTag enchantments = EnchantedBookItem.getEnchantments(target);
                for (int i = 0; i < enchantments.size(); ++i) {
                    CompoundTag compoundtag = enchantments.getCompound(i);
                    Enchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(EnchantmentHelper.getEnchantmentId(compoundtag));
                    if (enchant.isCurse()) return true;
                }
                return false;
            } else {
                Stream<Enchantment> stream = EnchantmentHelper.getEnchantments(target).keySet().stream();
                return stream.anyMatch(Enchantment::isCurse);
            }
        }
    }
}
