package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdeToLiving extends ItemBase implements Vanishable {
    public static Omniconfig.IntParameter cooldown;
    public static Omniconfig.PerhapsParameter synergyDamageReduction;

    public OdeToLiving() {
        super(getDefaultProperties().stacksTo(1).rarity(Rarity.RARE));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeConfig
    public static void onConfig(@NotNull OmniconfigWrapper builder) {
        builder.pushPrefix("OdetoLivingBeings");
        cooldown = builder.comment("Active ability cooldown for Ode to Living Beings. Measured in ticks. 20 ticks equal to 1 second.").max(32768).getInt("Cooldown", 1200);
        synergyDamageReduction = builder.comment("The percentage subtracted from damage redirected by Guide to Feral Hunt, if this is also possessed.").max(100.0).getPerhaps("SynergyDamageReduction", 75);
        builder.popPrefix();
    }

    public static boolean isProtectedAnimal(Player player, Animal animal) {
        ItemStack book = SuperAddonHandler.getItem(player, EnigmaticAddonItems.LIVING_ODE);
        if (book.isEmpty()) book = SuperAddonHandler.findBookInBag(player, EnigmaticAddonItems.LIVING_ODE);
        return !ItemNBTHelper.getUUID(book, "odeTarget", player.getUUID()).equals(animal.getUUID()) && EnigmaticItems.ANIMAL_GUIDEBOOK.isProtectedAnimal(animal);
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
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.livingOde7");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.livingOde8");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPetHurt(@NotNull LivingHurtEvent event) {
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


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDamage(@NotNull LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.getSource().getEntity() instanceof Player player && player.getMainHandItem().is(EnigmaticAddonItems.LIVING_ODE)) {
            ItemStack livingOde = player.getMainHandItem();
            ItemNBTHelper.setUUID(livingOde, "odeTarget", entity.getUUID());
            List<Animal> animals = player.level().getEntitiesOfClass(Animal.class, entity.getBoundingBox().inflate(10.0D));
            for (Animal animal : animals) {
                if (animal instanceof NeutralMob neutralMob && animal.getClass() != entity.getClass() && animal.getTarget() == null)
                    neutralMob.setTarget(entity);
            }
        }
    }
}
