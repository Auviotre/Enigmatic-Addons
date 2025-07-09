package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBetrayed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.triggers.BlessRingEquippedTrigger;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BlessRing extends ItemBaseCurio {
    public static final List<String> blessBetrayalList = new ArrayList<>();
    public static final String CURSED_SPAWN = "CursedNextSpawn";
    public static final String BLESS_SPAWN = "BlessNextSpawn";
    public static final String WORTHY_SPAWN = "WorthyNextSpawn";
    public static final String BETRAYAL = "BlessBetrayal";
    public static final String BLESS_DURATION = "BlessDuration";
    public static final String CURSE_TIME_LEVEL = "SavedSevenCurseLevel";
    public static final int MAX_DURATION = 5;
    public static Omniconfig.PerhapsParameter damageResistance;
    public static Omniconfig.PerhapsParameter damageBoost;
    public static Omniconfig.IntParameter regenerationSpeed;

    public BlessRing() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant());
        blessBetrayalList.add("enigmaticlegacy:astral_fruit");
        blessBetrayalList.add("enigmaticlegacy:twisted_mirror");
        blessBetrayalList.add("enigmaticlegacy:infernal_shield");
        blessBetrayalList.add("enigmaticlegacy:berserk_charm");
        blessBetrayalList.add("enigmaticlegacy:enchanter_pearl");
        blessBetrayalList.add("enigmaticlegacy:twisted_heart");
        blessBetrayalList.add("enigmaticlegacy:curse_transposer");
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("RingofRedemption");
        damageResistance = builder.comment("The damage resistance of the Ring of Redemption. Measured in percentage.").min(0).max(100).getPerhaps("DamageResistance", 25);
        damageBoost = builder.comment("The damage boost of the Ring of Redemption. Measured in percentage.").min(0).max(500).getPerhaps("DamageBoost", 20);
        regenerationSpeed = builder.comment("The time required for each regeneration of Ring of Redemption. Measured in ticks.").min(5).getInt("RegenerationTick", 20);
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            int level = Helper.getBlessAttribute(stack, CURSE_TIME_LEVEL);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing3", ChatFormatting.GOLD, (100 - damageResistance.getValue().asPercentage() * Helper.getResistanceModifier(stack)) + "%");
            if (level > 1)
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing4", ChatFormatting.GOLD, damageBoost.getValue().asPercentage() * Helper.getDamageModifier(stack) + "%");
            if (level > 1) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing5");
            if (level > 2)
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing6", ChatFormatting.GOLD, Helper.getBonusLevel(stack, CursedRing.lootingBonus.getValue()));
            if (level > 2)
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing7", ChatFormatting.GOLD, Helper.getBonusLevel(stack, CursedRing.fortuneBonus.getValue()));
            if (level > 3) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing8");
            if (level > 3) ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRing9");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            if (Helper.getBlessAttribute(stack, BLESS_DURATION) > 0)
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingDuration", ChatFormatting.GOLD, Helper.getBlessAttribute(stack, BLESS_DURATION));
            if (level > 4 && flagIn.isAdvanced())
                list.add(Component.literal(" [ " + Helper.getBlessAttribute(stack, BETRAYAL) + " / " + Helper.getMaxBetrayal(stack) + " ]").withStyle(ChatFormatting.GOLD));
        } else {
            if (CursedRing.enableLore.getValue()) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore1");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore2");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore3");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessRingLore4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }

            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound1");
            if (Minecraft.getInstance().player != null && SuperpositionHandler.canUnequipBoundRelics(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound2_creative");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.eternallyBound2");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        tooltips.clear();
        return tooltips;
    }

    public boolean canUnequip(SlotContext context, ItemStack stack) {
        if (context.entity() instanceof Player player) {
            if (SuperpositionHandler.canUnequipBoundRelics(player)) {
                return super.canUnequip(context, stack);
            }
        }
        return false;
    }

    public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
        if (context.entity() instanceof ServerPlayer player) {
            BlessRingEquippedTrigger.INSTANCE.trigger(player);
        }
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        // InitialCurseLevel
        if (Helper.getBlessAttribute(stack, CURSE_TIME_LEVEL) == 0) {
            if (entity instanceof Player player) Helper.setBlessLevel(player, stack);
            Helper.setBlessAttribute(stack, CURSE_TIME_LEVEL, Math.min(6, Helper.getBlessAttribute(stack, CURSE_TIME_LEVEL) + 1));
        }
        // Punishment
        if (entity instanceof Player player && SuperAddonHandler.isPunishedOne(player)) {
            SuperpositionHandler.setPersistentInteger(player, "Punishment", 1 +
                    SuperpositionHandler.getPersistentInteger(player, "Punishment", 0));
            if (SuperpositionHandler.getPersistentInteger(player, "Punishment", 0) % 20 == 0) {
                player.playSound(SoundEvents.TOTEM_USE, 0.6F, 0.0F);
                Level level = player.level();
                if (!level.isClientSide()) {
                    LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(player.level());
                    lightningBolt.moveTo(Vec3.atBottomCenterOf(player.blockPosition()));
                    lightningBolt.setCause(player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                    player.level().addFreshEntity(lightningBolt);
                }
            }
            if (SuperpositionHandler.getPersistentInteger(player, "Punishment", 0) > 100) {
                SuperpositionHandler.removePersistentTag(player, "Punishment");
                CuriosApi.getCuriosInventory(entity).ifPresent(curiosItemHandler -> {
                    IItemHandlerModifiable equippedCurios = curiosItemHandler.getEquippedCurios();
                    for (int i = 0; i < equippedCurios.getSlots(); i++)
                        equippedCurios.setStackInSlot(i, ItemStack.EMPTY);
                });
                player.hurt(player.damageSources().fellOutOfWorld(), player.getMaxHealth() * 1000F);
                player.getInventory().clearContent();
                player.experienceProgress = 0;
                player.experienceLevel = 0;
                player.kill();
            }
        }
        // Betrayal
        if (entity instanceof Player player && player.tickCount % 10 == 0 && !player.level().isClientSide) {
            AtomicInteger betrayal = new AtomicInteger(Helper.getBlessAttribute(stack, BETRAYAL));
            if (player.tickCount % 200 == 0 && betrayal.get() > 0) {
                if (betrayal.get() > Helper.getMaxBetrayal(stack)) {
                    player.level().playLocalSound(player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.6F, 0.8F, false);
                    player.sendSystemMessage(Component.translatable("message.enigmaticaddons.blessRingBreak").withStyle(ChatFormatting.GOLD));
                    Helper.setBlessAttribute(stack, BLESS_DURATION, Helper.getBlessAttribute(stack, BLESS_DURATION) + 1);
                    betrayal.set(0);
                } else {
                    if (Helper.getBlessAttribute(stack, CURSE_TIME_LEVEL) == 6)
                        betrayal.addAndGet(-player.getRandom().nextInt(2) - 1);
                    betrayal.addAndGet(-player.getRandom().nextInt(3) - 1);
                }
            }
            if (Helper.isBetrayedItem(player.getMainHandItem())) betrayal.addAndGet(1);
            if (Helper.isBetrayedItem(player.getOffhandItem())) betrayal.addAndGet(1);
            CuriosApi.getCuriosInventory(entity).ifPresent(curiosItemHandler -> {
                IItemHandlerModifiable equippedCurios = curiosItemHandler.getEquippedCurios();
                for (int i = 0; i < equippedCurios.getSlots(); i++) {
                    ItemStack stackInSlot = equippedCurios.getStackInSlot(i);
                    if (Helper.isBetrayedItem(stackInSlot)) betrayal.addAndGet(1);
                }
            });
            Helper.setBlessAttribute(stack, BETRAYAL, Math.max(0, betrayal.get()));
        }
        // Break
        if (entity instanceof Player player && Helper.getBlessAttribute(stack, BLESS_DURATION) >= MAX_DURATION) {
            player.playSound(SoundEvents.TOTEM_USE, 0.6F, 0.0F);
            SuperpositionHandler.destroyCurio(player, EnigmaticAddonItems.BLESS_RING);
        }
        // Regeneration
        if (Helper.getBlessAttribute(stack, CURSE_TIME_LEVEL) > 1 && entity.tickCount % regenerationSpeed.getValue() == 0 && entity.getHealth() < entity.getMaxHealth() * 0.9F) {
            float delta = entity.getMaxHealth() * 0.9F - entity.getHealth();
            entity.heal(Math.max(delta / 20.0F * Helper.getRegenerationModifier(stack), 0.5F));
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            return context.entity() instanceof Player player && !SuperpositionHandler.isTheCursedOne(player);
        }
        return false;
    }

    public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
        return false;
    }

    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }

    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        Map<Enchantment, Integer> list = EnchantmentHelper.getEnchantments(book);
        return !list.containsKey(Enchantments.VANISHING_CURSE) && super.isBookEnchantable(stack, book);
    }

    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack curio) {
        return super.getFortuneLevel(slotContext, lootContext, curio) + Helper.getBonusLevel(curio, CursedRing.fortuneBonus.getValue());
    }

    public int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target, int baseLooting, ItemStack curio) {
        return super.getLootingLevel(slotContext, source, target, baseLooting, curio) + Helper.getBonusLevel(curio, CursedRing.lootingBonus.getValue());
    }

    public boolean isBarVisible(ItemStack stack) {
        return Helper.getBlessAttribute(stack, BETRAYAL) > 0;
    }

    public int getBarWidth(ItemStack stack) {
        int max = Helper.getMaxBetrayal(stack);
        return Math.round(Math.max((max - Helper.getBlessAttribute(stack, BETRAYAL)) * 13.0F / max, 0.0F));
    }

    public int getBarColor(ItemStack stack) {
        return ChatFormatting.GOLD.getColor();
    }

    public static class Helper {
        public static final float[] BLESS_PROGRESSES = {0.0F, 10.0F, 25.0F, 45.0F, 75.0F, 99.5F, 200F};

        public static void setBlessLevel(Player player, ItemStack stack) {
            double suffering = SuperpositionHandler.getSufferingFraction(player) * 100;
            int level = 0;
            while (suffering >= BLESS_PROGRESSES[level]) level++;
            setBlessAttribute(stack, CURSE_TIME_LEVEL, level);
        }

        public static boolean isBetrayedItem(ItemStack item) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item.getItem());
            return item.getItem() instanceof IBetrayed || blessBetrayalList.contains(key.getNamespace() + ":" + key.getPath());
        }

        public static void addBetrayal(Player player, int count) {
            ItemStack stack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.BLESS_RING);
            if (stack != null && !stack.isEmpty())
                setBlessAttribute(stack, BETRAYAL, count + getBlessAttribute(stack, BETRAYAL));
        }

        public static int getMaxBetrayal(ItemStack stack) {
            int extra = 0;
            if (getBlessAttribute(stack, CURSE_TIME_LEVEL) == 6) extra += 350;
            return 1250 - getBlessAttribute(stack, BLESS_DURATION) * 100 + extra;
        }

        public static int getBlessAttribute(ItemStack stack, String id) {
            if (stack == null || stack.isEmpty()) return 0;
            return !stack.hasTag() ? 0 : stack.getTag().getInt(id);
        }

        public static void setBlessAttribute(ItemStack stack, String id, int value) {
            stack.getOrCreateTag().putInt(id, value);
        }

        // 1st Blessing
        public static float getResistanceModifier(ItemStack stack) {
            int level = getBlessAttribute(stack, CURSE_TIME_LEVEL);
            return Math.min(0.2F * level, 1.0F);
        }

        // 2nd Blessing
        public static float getDamageModifier(ItemStack stack) {
            int level = getBlessAttribute(stack, CURSE_TIME_LEVEL);
            if (level > 1) return Math.min(0.25F * (level - 1), 1.0F);
            return 0;
        }

        // 3rd & 4th Blessing
        public static int getBonusLevel(ItemStack stack, int origin) {
            int level = getBlessAttribute(stack, CURSE_TIME_LEVEL);
            if (level == 6) return origin;
            else if (level > 2) return (origin + 1) / 2;
            else return 0;
        }

        // 5th Blessing
        public static float getRegenerationModifier(ItemStack stack) {
            int level = getBlessAttribute(stack, CURSE_TIME_LEVEL);
            if (level > 1) return Math.min(0.25F * (level - 1), 1.25F);
            return 0;
        }

        // 6th Blessing
        public static boolean specialLooting(Player player) {
            if (!SuperAddonHandler.isTheBlessedOne(player)) return false;
            ItemStack stack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.BLESS_RING);
            if (stack != null && !stack.isEmpty()) return getBlessAttribute(stack, CURSE_TIME_LEVEL) > 3;
            return false;
        }

        public static boolean blessAvailable(Player player) {
            if (!SuperAddonHandler.isTheBlessedOne(player)) return false;
            ItemStack blessRing = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.BLESS_RING);
            return getBlessAttribute(blessRing, CURSE_TIME_LEVEL) > 3;
        }

        public static boolean betrayalAvailable(Player player) {
            if (!SuperAddonHandler.isTheBlessedOne(player)) return false;
            ItemStack blessRing = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.BLESS_RING);
            return getBlessAttribute(blessRing, CURSE_TIME_LEVEL) > 4;
        }
    }
}
