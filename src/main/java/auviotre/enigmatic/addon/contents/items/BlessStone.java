package auviotre.enigmatic.addon.contents.items;

import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

public class BlessStone extends ItemBase implements ICursed {
    public BlessStone() {
        super(ItemBase.getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
    }

    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> list, TooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            if (ItemNBTHelper.getBoolean(stack, "Hardcore", false)) {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone1_hardcore");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone2_hardcore");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone_hardcore_info");
            } else {
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone1");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone2");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone3");
                ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticaddons.blessStone_info");
            }
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cursedStone6");
        } else {
            ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
        }

        ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
        ItemLoreHelper.indicateCursedOnesOnly(list);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (level.getLevelData().isHardcore() && SuperpositionHandler.isTheCursedOne(player) && ItemNBTHelper.getBoolean(item, "Hardcore", false)) {
            CuriosApi.getCuriosInventory(player).ifPresent((handler) -> {
                IItemHandlerModifiable curios = handler.getEquippedCurios();
                for (int i = 0; i < handler.getSlots() - 1; ++i) {
                    if (curios.getStackInSlot(i) != null && curios.getStackInSlot(i).getItem() == EnigmaticItems.CURSED_RING) {
                        curios.setStackInSlot(i, EnigmaticAddonItems.BLESS_RING.getDefaultInstance());
                    }
                }
            });
            SuperpositionHandler.destroyCurio(player, EnigmaticItems.DESOLATION_RING);
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
            SuperpositionHandler.setPersistentBoolean(player, "DestroyedCursedRing", true);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
            player.swing(hand);
            player.getInventory().removeItem(item);
            LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(player.level());
            if (lightningbolt != null) {
                lightningbolt.moveTo(Vec3.atBottomCenterOf(player.blockPosition()));
                lightningbolt.setCause(player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                player.level().addFreshEntity(lightningbolt);
            }
            List<Entity> entities = player.level().getEntities(player, player.getBoundingBox().inflate(10));
            for (Entity entity : entities) {
                lightningbolt = EntityType.LIGHTNING_BOLT.create(player.level());
                if (lightningbolt != null) {
                    lightningbolt.moveTo(Vec3.atBottomCenterOf(entity.blockPosition()));
                    lightningbolt.setCause(player instanceof ServerPlayer serverPlayer ? serverPlayer : null);
                    player.level().addFreshEntity(lightningbolt);
                }
            }
            if (!level.isClientSide()) {
                ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.75), player.getZ(), 24, 0.0, 0.0, 0.0, 0.1D);
                ((ServerLevel) level).sendParticles(ParticleTypes.WITCH, player.getX(), player.getY(0.75), player.getZ(), 24, 0.0, 0.0, 0.0, 0.05D);
            }
            return InteractionResultHolder.success(item);
        }
        return InteractionResultHolder.pass(item);
    }
}
