package auviotre.enigmatic.addon.mixin;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.handlers.OmniconfigAddonHandler;
import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import auviotre.enigmatic.addon.packets.clients.PacketEvilCage;
import auviotre.enigmatic.addon.packets.clients.PacketMaliceTotem;
import auviotre.enigmatic.addon.registries.EnigmaticAddonDamageTypes;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEffects;
import auviotre.enigmatic.addon.registries.EnigmaticAddonEnchantments;
import auviotre.enigmatic.addon.registries.EnigmaticAddonItems;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements Attackable, IForgeLivingEntity {
    @Shadow
    public abstract boolean canFreeze();

    @Shadow
    public abstract void setHealth(float p_21154_);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    public abstract boolean removeAllEffects();

    public MixinLivingEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickMix(CallbackInfo ci) {
        if (this.level().isClientSide && OmniconfigAddonHandler.frostParticle.getValue() && this.isFullyFrozen() && Math.random() > 0.25D) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE, this.getRandomX(0.6D), this.getRandomY() + 0.1D, this.getRandomZ(0.6D), 0.0D, -0.05D, 0.0D);
        }
        if (!this.canFreeze() && this.getTicksFrozen() > 0) this.setTicksFrozen(0);
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    public void canFreezeMix(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && !this.self().hasEffect(EnigmaticAddonEffects.FROZEN_HEART_EFFECT) && !SuperpositionHandler.hasCurio(this.self(), EnigmaticAddonItems.FORGOTTEN_ICE) && EnchantmentHelper.getEnchantmentLevel(EnigmaticAddonEnchantments.FROST_PROTECTION, this.self()) < 16);
    }

    @Inject(method = "onChangedBlock", at = @At("HEAD"))
    public void onChangedBlockMix(BlockPos pos, CallbackInfo ci) {
        if (SuperpositionHandler.hasCurio(this.self(), EnigmaticAddonItems.FORGOTTEN_ICE)) {
            FrostWalkerEnchantment.onEntityMoved(this.self(), this.level(), pos, 1);
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), index = 1, argsOnly = true)
    public DamageSource hurtMix(DamageSource source) {
        Entity entity = source.getEntity();
        if (entity instanceof Player player && SuperpositionHandler.isTheCursedOne(player) && SuperpositionHandler.hasItem(player, EnigmaticAddonItems.FALSE_JUSTICE)) {
            return player.damageSources().source(EnigmaticAddonDamageTypes.FALSE_JUSTICE, source.getDirectEntity(), player);
        }
        return source;
    }

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
    public void checkMix(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && this.self() instanceof Player player && SuperpositionHandler.isTheCursedOne(player)) {
            ItemStack stack = ItemStack.EMPTY;
            if (SuperpositionHandler.hasItem(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
                ItemStack itemStack = SuperAddonHandler.getItem(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
                stack = itemStack.copy();
                itemStack.hurtAndBreak(1, player, (consumer) -> consumer.invulnerableTime = 60);
            } else if (SuperpositionHandler.hasCurio(player, EnigmaticAddonItems.TOTEM_OF_MALICE)) {
                ItemStack curioStack = SuperpositionHandler.getCurioStack(player, EnigmaticAddonItems.TOTEM_OF_MALICE);
                stack = curioStack.copy();
                curioStack.hurtAndBreak(1, player, (consumer) -> consumer.invulnerableTime = 60);
            }
            if (!stack.isEmpty()) {
                PacketDistributor.PacketTarget packet = PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(this.getX(), this.getY(), this.getZ(), 64.0, level().dimension()));
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.awardStat(Stats.ITEM_USED.get(EnigmaticAddonItems.TOTEM_OF_MALICE), 1);
                    CriteriaTriggers.USED_TOTEM.trigger(serverPlayer, stack);
                    EnigmaticAddons.packetInstance.send(packet, new PacketMaliceTotem(this.getX(), this.getY(), this.getZ()));
                }
                float damage = this.getMaxHealth() * (1.5F + SuperpositionHandler.getCurseAmount(stack) * 0.5F);
                List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8));
                for (LivingEntity entity : entities) {
                    if (entity == player) continue;
                    Vec3 delta = entity.position().subtract(this.position()).normalize().scale(0.5);
                    float modifier = Math.min(1.0F, 0.8F / entity.distanceTo(this));
                    Vec3 vec = new Vec3(delta.x, 0, delta.z).normalize().scale(modifier);
                    entity.addDeltaMovement(new Vec3(vec.x, entity.onGround() ? 1.2F * modifier : 0.0F, vec.z));
                    entity.hurt(entity.damageSources().source(EnigmaticAddonDamageTypes.EVIL_CURSE, player), damage);
                    entity.invulnerableTime = 0;
                    EnigmaticAddons.packetInstance.send(packet, new PacketEvilCage(entity.getX(), entity.getY(), entity.getZ(), entity.getBbWidth() / 2, entity.getBbHeight(), 0));
                }
                this.setHealth(this.getMaxHealth());
                this.removeAllEffects();
                cir.setReturnValue(true);
            }
        }
    }
}
