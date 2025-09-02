package auviotre.enigmatic.addon.contents.items;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AvariceRing extends ItemBaseCurio implements ICursed {
    public static final List<ResourceLocation> merchantList = new ArrayList<>();
    public static Omniconfig.DoubleParameter damageBoostMultiplier;
    public AvariceRing() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).fireResistant());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(OmniconfigWrapper builder) {
        builder.pushPrefix("RingofUltimateLuxury");
        damageBoostMultiplier = builder.comment("The damage boost multiplier of the Ring of Ultimate Luxury .").max(2).min(0.0).getDouble("DamageBoostMultiplier", 0.5);
        merchantList.clear();
        builder.forceSynchronized(true);
        String[] list = builder.config.getStringList("RingofUltimateLuxuryExtraMerchantList", "Balance Options", new String[0], "List of entities that will be affected as Golem by the Ring of Ultimate Luxury. Examples: minecraft:iron_golem. Changing this option required game restart to take effect.");
        Arrays.stream(list).forEach((entry) -> merchantList.add(new ResourceLocation(entry)));
        builder.popPrefix();
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        LocalPlayer player = Minecraft.getInstance().player;
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll1", ChatFormatting.GOLD, 1);
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing3");
            if (SuperpositionHandler.hasCurio(player, EnigmaticItems.AVARICE_SCROLL))
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing4_alt");
            else ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing4");
            if (SuperpositionHandler.hasCurio(player, EnigmaticItems.AVARICE_SCROLL)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing5");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing6", ChatFormatting.GOLD, String.format("+%.01f%%", getDamageBoost(player) * 100));
            }
        } else {
            if (SuperpositionHandler.hasCurio(player, EnigmaticItems.AVARICE_SCROLL)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.avariceRing6", ChatFormatting.GOLD, String.format("+%.01f%%", getDamageBoost(player) * 100));
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    private float getDamageBoost(Player player) {
        float count = 0;
        for (NonNullList<ItemStack> compartment : player.getInventory().compartments) {
            for (ItemStack stack : compartment) if (stack.is(Tags.Items.GEMS)) count += stack.getCount();
        }
        return (float) (damageBoostMultiplier.getValue() * Math.log(1 + count) / Math.log(64));
    }

    public void curioTick(SlotContext context, ItemStack stack) {
        LivingEntity entity = context.entity();
        List<Raider> raiders = entity.level().getEntitiesOfClass(Raider.class, SuperpositionHandler.getBoundingBoxAroundEntity(entity, 8));
        if (entity instanceof Player player && player.getAbilities().instabuild) return;
        for (Raider raider : raiders) {
            double visibility = entity.getVisibilityPercent(raider);
            double angerDistance = Math.max(8 * visibility, 5);
            if (!entity.hasLineOfSight(raider) && !(entity.distanceTo(raider) <= 5)) continue;
            if (raider.distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) <= angerDistance * angerDistance) {
                if (raider.getTarget() != null && raider.getTarget().isAlive()) {
                    if (!SuperpositionHandler.hasCurio(raider.getTarget(), this)) {
                        raider.setLastHurtByMob(entity);
                        raider.setTarget(entity);
                    }
                } else {
                    raider.setLastHurtByMob(entity);
                    raider.setTarget(entity);
                }
            }
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack) && context.entity() instanceof Player player)
            return SuperpositionHandler.isTheCursedOne(player);
        return false;
    }

    public int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack curio) {
        return super.getFortuneLevel(slotContext, lootContext, curio) + 1;
    }

    @SubscribeEvent
    public void onDamage(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, this) && SuperpositionHandler.hasCurio(player, EnigmaticItems.AVARICE_SCROLL)) {
                event.setAmount(event.getAmount() * (1 + getDamageBoost(player)));
            }
        }
        if (event.getSource().getEntity() instanceof Raider raider && event.getEntity() instanceof Player player) {
            if (SuperpositionHandler.hasCurio(player, this)) {
                event.setAmount(event.getAmount() * (1 + getDamageBoost(player) / 2.0F));
            }
        }
    }

    @SubscribeEvent
    public void onTrade(TradeWithVillagerEvent event) {
        Player player = event.getEntity();
        if (SuperpositionHandler.hasCurio(player, this)) {
            MerchantOffer merchantOffer = event.getMerchantOffer();
            merchantOffer.resetUses();
            if (merchantOffer.getPriceMultiplier() < 0) merchantOffer.resetSpecialPriceDiff();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && SuperpositionHandler.hasCurio(player, this)) {
            if (SuperpositionHandler.hasCurio(player, EnigmaticItems.AVARICE_SCROLL)) return;
            for (NonNullList<ItemStack> compartment : player.getInventory().compartments) {
                for (ItemStack itemStack : compartment) {
                    clearEmerald(player.level(), itemStack);
                }
            }
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                int slots = handler.getEquippedCurios().getSlots();
                for (int i = 0; i < slots; i++) {
                    clearEmerald(player.level(), handler.getEquippedCurios().getStackInSlot(i));
                }
            });
        }
    }

    private void clearEmerald(Level level, ItemStack stack) {
        if (stack.getDescriptionId().contains("emerald") || stack.is(Tags.Items.GEMS_EMERALD)) {
            stack.setCount(0);
        } else {
            RecipeManager manager = level.getRecipeManager();
            Collection<Recipe<?>> recipes = manager.getRecipes();
            for (Recipe<?> recipe : recipes) {
                if (!recipe.getResultItem(level.registryAccess()).is(stack.getItem())) continue;
                for (Ingredient ingredient : recipe.getIngredients()) {
                    for (ItemStack item : ingredient.getItems()) {
                        if (item.getDescriptionId().contains("emerald") || stack.is(Tags.Items.GEMS_EMERALD)) {
                            stack.setCount(0);
                            return;
                        }
                    }
                }
            }
        }
    }
}
