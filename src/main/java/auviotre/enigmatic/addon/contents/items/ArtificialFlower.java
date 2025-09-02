package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.client.screens.ArtificialFlowerScreen;
import auviotre.enigmatic.addon.contents.gui.ArtificialFlowerMenu;
import auviotre.enigmatic.addon.contents.objects.bookbag.AntiqueBagCapability;
import auviotre.enigmatic.addon.contents.objects.bookbag.IAntiqueBagHandler;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import auviotre.enigmatic.addon.registries.EnigmaticAddonPotions;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class ArtificialFlower extends ItemBase {
    public static final List<ResourceLocation> attributeBlackList = new ArrayList<>();
    public static final List<ResourceLocation> effectBlackList = new ArrayList<>();
    public static final Map<ServerPlayer, Map<ImmutableMultimap<Attribute, AttributeModifier>, Integer>> PLAYER_ATTRIBUTE_MAP = new HashMap<>();
    private static final String[] defaultAttributeBlackList = new String[]{
            "caelus:fall_flying",
            "goety_revelation:resistance"
    };
    private static final String[] defaultEffectBlackList = new String[]{
            "alexscaves:darkness_incarnate",
            "alexsmobs:oiled",
            "aquamirae:crystallization",
            "born_in_chaos_v1:light_rampage",
            "born_in_chaos_v1:medium_rampage",
            "born_in_chaos_v1:strong_rampage",
            "born_in_chaos_v1:furious_rampage",
            "born_in_chaos_v1:rampant_rampage",
            "cataclysm:ghost_form",
            "cataclysm:ghost_sickness",
            "enigmaticdelicacy:astral_drunkenness",
            "enigmaticdelicacy:abyss_corruption",
            "enigmaticdelicacy:fading",
            "enigmaticdelicacy:health_curse",
            "enigmaticlegacy:blazing_strength",
            "goety:shadow_walk",
            "irons_spellbooks:abyssal_shroud",
            "irons_spellbooks:evasion",
            "irons_spellbooks:heartstop",
            "irons_spellbooks:true_invisibility",
            "ltc2:undying_benediction",
            "more_potion_effects:extension",
            "more_potion_effects:immortal",
            "more_potion_effects:static_life",
            "supernatural:supernatural",
            "unusual_delight:crystal_aspect",
            "unusual_delight:ember_aspect",
            "unusual_delight:vitality",
    };
    public static Omniconfig.PerhapsParameter randomInstantaneousEffectModifier;
    public static Omniconfig.PerhapsParameter randomAttributeMaxModifier;

    public ArtificialFlower() {
        super(ItemBase.getDefaultProperties().stacksTo(1).rarity(Rarity.RARE));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("ArtificialFlower");
        randomAttributeMaxModifier = builder.comment("The max modifier of the Magic Quartz Flower. Measures in percentage.").max(100).getPerhaps("RandomAttributeMaxModifier", 16);
        randomInstantaneousEffectModifier = builder.comment("The modifier of the instantaneous effect provided by Magic Quartz Flower. Measures in percentage.").max(100).getPerhaps("RandomInstantaneousEffectModifier", 80);
        attributeBlackList.clear();
        builder.forceSynchronized(true);
        String[] list = builder.config.getStringList("ArtificialFlowerAttributeBlackList", "Balance Options", defaultAttributeBlackList, "List of Attributes that will never appear in Magic Quartz Flower. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> attributeBlackList.add(new ResourceLocation(entry)));
        effectBlackList.clear();
        list = builder.config.getStringList("ArtificialFlowerEffectBlackList", "Balance Options", defaultEffectBlackList, "List of Effects that will never appear in Magic Quartz Flower. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> effectBlackList.add(new ResourceLocation(entry)));
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        list.add(Component.translatable("tooltip.enigmaticaddons.artificialFlowerAttribute"));
        int count = 0;
        for (int id = 1; id <= 3; ++id)
            if (Helper.getAttribute(stack, id) != null) {
                list.add(ArtificialFlowerScreen.getComponent(Helper.getAttribute(stack, id)));
                count++;
            }
        if (count == 0) list.add(Component.translatable("tooltip.enigmaticaddons.artificialFlowerNone"));
        list.add(Component.translatable("tooltip.enigmaticaddons.artificialFlowerEffect"));
        count = 0;
        for (int id = 0; id < 2; id++) {
            MobEffect effect = Helper.getEffect(stack, id);
            Component name;
            int suffix;
            if (effect == null) continue;
            count++;
            suffix = stack.getOrCreateTag().contains("MagicRing") ? 1 : 0;
            name = Component.translatable(effect.getDescriptionId()).withStyle(effect.isBeneficial() ? ChatFormatting.GREEN : ChatFormatting.RED);
            Component immunity = Component.translatable("gui.enigmaticaddons.artificial_flower_immunity", name);
            Component providing = Component.translatable("gui.enigmaticaddons.artificial_flower_provide" + suffix, name);
            list.add(id == 0 ? providing : immunity);
        }
        if (count == 0) list.add(Component.translatable("tooltip.enigmaticaddons.artificialFlowerNone"));
    }

    public boolean isFoil(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("FlowerEnable");
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int id, boolean selected) {
        if (!(entity instanceof Player player)) return;
        CompoundTag tag = stack.getOrCreateTag();
        if (player instanceof ServerPlayer) {
            UUID flowerEnableUUID = Helper.getPlayerEnableUUID(player);
            UUID flowerUUID = Helper.getFlowerUUID(stack);
            if ((flowerUUID == null || !flowerUUID.equals(flowerEnableUUID)) && tag.getBoolean("FlowerEnable")) {
                tag.putBoolean("FlowerEnable", false);
                for (int i = 1; i <= 3; i++) {
                    Pair<Attribute, AttributeModifier> attribute = Helper.getAttribute(stack, i);
                    if (attribute != null) {
                        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                        builder.put(attribute.getFirst(), attribute.getSecond());
                        player.getAttributes().removeAttributeModifiers(builder.build());
                    }
                }
            }
        }
        boolean bagFlag = false;
        LazyOptional<IAntiqueBagHandler> capability = SuperAddonHandler.getCapability(player, AntiqueBagCapability.INVENTORY);
        if (capability != null && capability.isPresent()) {
            IAntiqueBagHandler bagHandler = capability.orElseThrow(() -> new IllegalArgumentException("Lazy optional must not be empty"));
            if (bagHandler.hasFlower()) {
                ItemStack bagStack = bagHandler.getBook(stack.getOrCreateTag().getInt("FlowerBagEnable"));
                bagFlag = Helper.getFlowerUUID(bagStack) != null && Helper.getFlowerUUID(bagStack).equals(Helper.getFlowerUUID(stack));
            }
        }
        if (tag.getBoolean("FlowerEnable") || bagFlag) {
            for (int i = 1; i <= 3; i++) {
                Pair<Attribute, AttributeModifier> attribute = Helper.getAttribute(stack, i);
                if (attribute != null && attributeBlackList.contains(ForgeRegistries.ATTRIBUTES.getKey(attribute.getFirst())))
                    Helper.removeAttribute(stack, i);
            }
            for (int i = 0; i < 2; i++) {
                MobEffect effect = Helper.getEffect(stack, i);
                if (effect != null && effectBlackList.contains(ForgeRegistries.MOB_EFFECTS.getKey(effect)))
                    Helper.removeEffect(stack, i);
            }

            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            for (int i = 1; i <= 3; i++)
                if (Helper.getAttribute(stack, i) != null) {
                    Pair<Attribute, AttributeModifier> attribute = Helper.getAttribute(stack, i);
                    builder.put(attribute.getFirst(), attribute.getSecond());
                }
            ImmutableMultimap<Attribute, AttributeModifier> attributeMap = builder.build();
            if (player instanceof ServerPlayer server && !attributeMap.isEmpty()) {
                Map<ImmutableMultimap<Attribute, AttributeModifier>, Integer> attributeTickMap;
                if (PLAYER_ATTRIBUTE_MAP.containsKey(server)) attributeTickMap = PLAYER_ATTRIBUTE_MAP.get(player);
                else attributeTickMap = new HashMap<>();
                if (attributeTickMap.containsKey(attributeMap)) attributeTickMap.replace(attributeMap, 3);
                else attributeTickMap.put(attributeMap, 3);
                PLAYER_ATTRIBUTE_MAP.put(server, attributeTickMap);
            }
            MobEffect effectImmuneTo = Helper.getEffect(stack, 1);
            if (effectImmuneTo != null && player.hasEffect(effectImmuneTo)) player.removeEffect(effectImmuneTo);
            MobEffect effectProvided = Helper.getEffect(stack, 0);
            int amplifier = 0;
            if (tag.contains("MagicRing")) amplifier++;
            if (effectProvided != null) {
                if (effectProvided.isInstantenous()) {
                    if (player.tickCount % 100 == 0)
                        effectProvided.applyInstantenousEffect(player, player, player, amplifier, randomInstantaneousEffectModifier.getValue().asModifier());
                } else {
                    MobEffectInstance newInstance = new MobEffectInstance(effectProvided, 36, amplifier, true, true);
                    if (player.hasEffect(effectProvided)) {
                        MobEffectInstance instance = player.getEffect(effectProvided);
                        if (instance.getAmplifier() == amplifier && instance.getDuration() <= 4) instance.duration = 36;
                        else if (instance.getAmplifier() < amplifier) instance.update(newInstance);
                    } else player.addEffect(newInstance);
                }
            }
        }
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        player.getCooldowns().addCooldown(this, 30);
        player.getItemInHand(hand).getOrCreateTag().putBoolean("FlowerEnable", false);
        if (!world.isClientSide) player.openMenu(new ArtificialFlowerMenu.Provider());
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @SubscribeEvent
    public void onEffectApply(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance effect = event.getEffectInstance();
            if (effect == null) return;
            List<ItemStack> flowers = SuperAddonHandler.getAllItem(player, this);
            for (ItemStack flower : flowers) {
                if (effect.getEffect().equals(Helper.getEffect(flower, 1))) {
                    event.setResult(Event.Result.DENY);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ArtificialFlower.PLAYER_ATTRIBUTE_MAP.forEach((player, attributeTickMap) -> {
            if (attributeTickMap.isEmpty()) return;
            List<ImmutableMultimap<Attribute, AttributeModifier>> multiMapSet = attributeTickMap.keySet().stream().toList();
            for (ImmutableMultimap<Attribute, AttributeModifier> attribute : multiMapSet) {
                Integer tick = attributeTickMap.get(attribute);
                if (tick <= 1) {
                    player.getAttributes().removeAttributeModifiers(attribute);
                    attributeTickMap.remove(attribute);
                } else {
                    player.getAttributes().addTransientAttributeModifiers(attribute);
                    attributeTickMap.replace(attribute, tick - 1);
                }
            }
        });
    }

    public static class Helper {
        public static List<Attribute> attributePool;
        public static List<MobEffect> potionEffectPool;
        public static List<MobEffect> allEffectPool;

        public static void initRandomPool() {
            ImmutableList.Builder<Attribute> attributeBuilder = new ImmutableList.Builder<>();
            ForgeRegistries.ATTRIBUTES.getValues().forEach(attribute -> {
                if (!attributeBlackList.contains(ForgeRegistries.ATTRIBUTES.getKey(attribute))) {
                    attributeBuilder.add(attribute);
                }
            });
            attributePool = attributeBuilder.build();

            ImmutableList.Builder<MobEffect> effectImmuneBuilder = new ImmutableList.Builder<>();
            ForgeRegistries.MOB_EFFECTS.getValues().forEach(effect -> {
                if (effectBlackList.contains(ForgeRegistries.MOB_EFFECTS.getKey(effect))) return;
                if (ForgeRegistries.MOB_EFFECTS.getKey(effect).toString().contains("flight")) return;
                if (ForgeRegistries.MOB_EFFECTS.getKey(effect).toString().contains("fly")) return;
                effectImmuneBuilder.add(effect);
            });
            allEffectPool = effectImmuneBuilder.build();

            ImmutableList.Builder<MobEffect> effectProvidingBuilder = new ImmutableList.Builder<>();
            List<MobEffect> effects = new ArrayList<>();
            for (Potion potion : ForgeRegistries.POTIONS.getValues()) {
                for (MobEffectInstance instance : potion.getEffects()) {
                    if (!effects.contains(instance.getEffect())) effects.add(instance.getEffect());
                }
            }
            EnigmaticAddonPotions.COMMON_POTIONS.forEach(potion -> potion.getEffects().forEach(effect -> {
                if (!effects.contains(effect.getEffect())) effects.add(effect.getEffect());
            }));
            EnigmaticPotions.COMMON_POTIONS.forEach(potion -> potion.getEffects().forEach(effect -> {
                if (!effects.contains(effect.getEffect())) effects.add(effect.getEffect());
            }));
            effects.forEach(effect -> {
                if (effectBlackList.contains(ForgeRegistries.MOB_EFFECTS.getKey(effect))) return;
                if (ForgeRegistries.MOB_EFFECTS.getKey(effect).toString().contains("flight")) return;
                if (ForgeRegistries.MOB_EFFECTS.getKey(effect).toString().contains("fly")) return;
                effectProvidingBuilder.add(effect);
            });
            potionEffectPool = effectProvidingBuilder.build();
        }

        public static void setAttribute(ItemStack stack, int index, Attribute attribute, AttributeModifier attributeModifier) {
            String id = "AttributeId" + index;
            String modifier = "AttributeModifier" + index;
            CompoundTag modifierTag = attributeModifier.save();
            stack.getOrCreateTag().put(modifier, modifierTag);
            stack.getOrCreateTag().putString(id, ForgeRegistries.ATTRIBUTES.getKey(attribute).toString());
        }

        public static void removeAttribute(ItemStack stack, int index) {
            stack.getOrCreateTag().remove("AttributeModifier" + index);
            stack.getOrCreateTag().remove("AttributeId" + index);
        }

        public static void setEffect(ItemStack stack, int index, MobEffect effect) {
            String id = "PotionEffect" + index;
            stack.getOrCreateTag().putString(id, ForgeRegistries.MOB_EFFECTS.getKey(effect).toString());
        }

        public static void removeEffect(ItemStack stack, int index) {
            stack.getOrCreateTag().remove("PotionEffect" + index);
        }

        public static @Nullable Pair<Attribute, AttributeModifier> getAttribute(ItemStack stack, int index) {
            String id = "AttributeId" + index;
            if (!stack.hasTag() || !stack.getTag().contains(id, 8)) return null;
            String modifier = "AttributeModifier" + index;
            if (!stack.hasTag() || !stack.getTag().contains(modifier, 10)) return null;
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(stack.getTag().getString(id)));
            if (attribute == null) {
                removeAttribute(stack, index);
                return null;
            }
            AttributeModifier attributeModifier = AttributeModifier.load(stack.getTag().getCompound(modifier));
            return Pair.of(attribute, attributeModifier);
        }

        public static @Nullable MobEffect getEffect(ItemStack stack, int index) {
            String id = "PotionEffect" + index;
            if (!stack.hasTag() || !stack.getTag().contains(id, 8)) return null;
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(stack.getTag().getString(id)));
            if (effect == null) {
                removeEffect(stack, index);
                return null;
            }
            return effect;
        }

        public static void randomAttribute(Player player, ItemStack stack, int index, int cost, boolean boost) {
            Attribute attribute = attributePool.get(player.getRandom().nextInt(attributePool.size()));
            double defaultValue = attribute.getDefaultValue();
            AttributeModifier.Operation operation;
            double offset = (cost == 0 ? 0 : cost == 1 ? 0.3 : 0.6) - (boost ? 0 : 0.125);
            double value = 0.01 * (int) (Mth.clamp(player.getRandom().nextGaussian() + offset, -2.5, 2.5) / 2.5 * randomAttributeMaxModifier.getValue().asPercentage());
            AttributeModifier modifier = new AttributeModifier("ArtificialFlower" + index, value, AttributeModifier.Operation.MULTIPLY_BASE);
            Pair<Attribute, AttributeModifier> oldAttribute = getAttribute(stack, index);
            if (oldAttribute != null) {
                ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(oldAttribute.getFirst(), oldAttribute.getSecond());
                player.getAttributes().removeAttributeModifiers(builder.build());
            }
            if (value == 0) removeAttribute(stack, index);
            else setAttribute(stack, index, attribute, modifier);
        }

        public static void randomEffect(Player player, ItemStack stack, int index) {
            MobEffect effect;
            int count = stack.getOrCreateTag().contains("AllEffectCount") ? stack.getOrCreateTag().getInt("AllEffectCount") : 1;
            RandomSource playerRandom = player.getRandom();
            do {
                if (index == 1 || playerRandom.nextInt((count + 1) / 2) == 0) {
                    effect = allEffectPool.get(playerRandom.nextInt(allEffectPool.size()));
                    System.out.print("FlowerCount: " + count + "\n");
                    stack.getOrCreateTag().putInt("AllEffectCount", count + 1);
                } else effect = potionEffectPool.get(playerRandom.nextInt(potionEffectPool.size()));
            } while (effect == getEffect(stack, 1 - index));
            MobEffect oldEffect = getEffect(stack, index);
            if (oldEffect != null && player.hasEffect(oldEffect)) player.removeEffect(oldEffect);
            setEffect(stack, index, effect);
        }

        public static ItemStack getFlowerStack(Player player, boolean copy) {
            ItemStack flower;
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.is(EnigmaticAddonItems.ARTIFICIAL_FLOWER)) flower = mainHandItem;
            else flower = player.getOffhandItem();
            return copy ? flower.copy() : flower;
        }

        public static @Nullable UUID getFlowerUUID(ItemStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            return tag.hasUUID("FlowerUUID") ? tag.getUUID("FlowerUUID") : null;
        }

        public static @Nullable UUID getPlayerEnableUUID(Player player) {
            CompoundTag tag = player.getPersistentData();
            return tag.hasUUID("FlowerEnableUUID") ? tag.getUUID("FlowerEnableUUID") : null;
        }
    }
}
