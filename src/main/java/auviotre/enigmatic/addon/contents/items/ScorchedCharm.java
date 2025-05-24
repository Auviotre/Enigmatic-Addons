package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.api.items.IBlessed;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ScorchedCharm extends ItemBaseCurio implements ICursed, IBlessed {
    public static Omniconfig.DoubleParameter lavaHealAmount;
    public static Omniconfig.PerhapsParameter lifestealModifier;
    public static Omniconfig.PerhapsParameter resistanceProbability;
    public List<ResourceKey<DamageType>> immunityList = new ArrayList<>();

    public ScorchedCharm() {
        super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).fireResistant());
        this.immunityList.add(DamageTypes.IN_FIRE);
        this.immunityList.add(DamageTypes.ON_FIRE);
        this.immunityList.add(DamageTypes.HOT_FLOOR);
        this.immunityList.add(DamageTypes.LAVA);
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("CharmofScorchedSun");
        lavaHealAmount = builder.comment("The heal amount per second when you're in lava with this Charm.").max(100).min(0).getDouble("LavaHealAmount", 2);
        lifestealModifier = builder.comment("The lifesteal modifier provided by this charm when you attack a target on fire. Measured in percentage.").max(100).getPerhaps("LifestealModifier", 20);
        resistanceProbability = builder.comment("The probability to resist damage when you are attacked with this charm. Measured in percentage.").max(100).getPerhaps("ResistanceProbability", 10);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.scorchedCharm1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.scorchedCharm2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.scorchedCharm3");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.scorchedCharm4");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.scorchedCharm5");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.isOnFire()) entity.clearFire();
        if (entity.isInLava()) {
            if (entity.tickCount % 20 == 0) entity.heal((float) lavaHealAmount.getValue());
            if (entity instanceof Player player && !player.isAffectedByFluids()) return;
            CollisionContext collisionContext = CollisionContext.of(entity);
            if (collisionContext.isAbove(LiquidBlock.STABLE_SHAPE, entity.blockPosition(), true) && !entity.level().getFluidState(entity.blockPosition().above()).is(FluidTags.LAVA)) {
                entity.setOnGround(true);
            } else {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, entity.isCrouching() ? -0.01 : 0.07, 0.0));
            }
        }
    }

    public boolean canEquip(SlotContext context, ItemStack stack) {
        if (super.canEquip(context, stack)) {
            LivingEntity entity = context.entity();
            return entity instanceof Player player && SuperAddonHandler.isOKOne(player);
        }
        return false;
    }
}
