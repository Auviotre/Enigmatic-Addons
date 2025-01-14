package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.HunterGuidebook;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdeToLiving extends ItemBase implements Vanishable {
    public static Omniconfig.IntParameter cooldown;
    public static Omniconfig.PerhapsParameter synergyDamageReduction;

    public OdeToLiving() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.RARE));
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("OdetoLivingBeings");
        cooldown = builder.comment("Active ability cooldown for Ode to Living Beings. Measured in ticks. 20 ticks equal to 1 second.").max(32768).getInt("Cooldown", 1200);
        synergyDamageReduction = builder.comment("The percentage subtracted from damage redirected by Guide to Feral Hunt, if this is also possessed.").max(100.0).getPerhaps("SynergyDamageReduction", 75);
        builder.popPrefix();
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof NeutralMob neutral && neutral.isAngry() && !player.getCooldowns().isOnCooldown(this)) {
            if (!player.level().isClientSide) {
                boolean flag = target instanceof Animal && neutral.getTarget() == player;
                if (flag) neutral.stopBeingAngry();
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.HEART, target.getX(), target.getEyeY(), target.getZ(), 5, target.getBbWidth(), 0.1D, target.getBbWidth(), 0.1D);
            }
            if (!player.getAbilities().instabuild) player.getCooldowns().addCooldown(this, cooldown.getValue());
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.livingOde1");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.livingOde2");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.livingOde3");
            if (Minecraft.getInstance().player != null && SuperpositionHandler.isTheCursedOne(Minecraft.getInstance().player)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.animalGuidebook4");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.animalGuidebook5");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.livingOde6");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    @Mod.EventBusSubscriber(
            modid = EnigmaticAddons.MODID,
            bus = Mod.EventBusSubscriber.Bus.FORGE
    )
    public static class PetEvent {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onPetHurt(@NotNull LivingHurtEvent event) {
            if (event.getEntity() instanceof TamableAnimal pet) {
                if (pet.isTame()) {
                    LivingEntity owner = pet.getOwner();
                    if (owner instanceof Player player && SuperpositionHandler.hasItem(player, EnigmaticItems.HUNTER_GUIDEBOOK) && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.LIVING_ODE)) {
                        if (owner.level() == pet.level() && owner.distanceTo(pet) <= HunterGuidebook.effectiveDistance.getValue()) {
                            event.setCanceled(true);
                            owner.hurt(event.getSource(), event.getAmount() * synergyDamageReduction.getValue().asModifierInverted());
                        }
                    }
                }
            }
        }
    }
}
