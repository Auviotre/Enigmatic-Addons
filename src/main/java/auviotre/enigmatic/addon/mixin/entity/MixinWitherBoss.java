package auviotre.enigmatic.addon.mixin.entity;

import auviotre.enigmatic.addon.handlers.SuperAddonHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WitherBoss.class)
public abstract class MixinWitherBoss extends Monster {

    @Unique
    private boolean enigmaticAddons$lastPower = false;

    @Shadow
    public abstract boolean isPowered();

    protected MixinWitherBoss(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    @Inject(method = "customServerAiStep", at = @At(value = "HEAD"))
    public void customServerAiStep(CallbackInfo ci) {
        if (SuperAddonHandler.isCurseBoosted(this)) {
            if (this.isPowered() && !this.enigmaticAddons$lastPower)
                enigmaticAddons$randomSkeleton(2 + this.random.nextInt(2));
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(8.0F));
            for (Entity entity : entities) {
                if (entity instanceof Monster living && living != this && living.getMobType().equals(MobType.UNDEAD)) {
                    if (living.getTarget() == this) {
                        living.setTarget(null);
                    }
                }
            }
            this.enigmaticAddons$lastPower = this.isPowered();
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    public void hurtMix(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (SuperAddonHandler.isCurseBoosted(this) && this.isPowered() && cir.getReturnValue() && this.random.nextInt(6) == 0) {
            enigmaticAddons$randomSkeleton(1 + this.random.nextInt(Mth.ceil(amount / 7.5F)));
        }
    }

    @Unique
    private void enigmaticAddons$randomSkeleton(int count) {
        if (this.level().isClientSide()) return;
        this.level().playSound(null, this.blockPosition(), SoundEvents.WITHER_SKELETON_STEP, SoundSource.NEUTRAL, 5.0F, 0.0F);
        for (int i = 0; i < count; i++) {
            WitherSkeleton skeleton = new WitherSkeleton(EntityType.WITHER_SKELETON, this.level());
            skeleton.setPos(this.position());
            if (this.random.nextFloat() < 0.4F) {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(this.random, new ItemStack(Items.STONE_SWORD), (int) (5.0F + 1.6F * (float) this.random.nextInt(18)), false));
            } else {
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
            }
            skeleton.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.STONE_SWORD));
            double dx;
            double dy;
            double dz;
            do {
                dx = skeleton.getX() + (this.random.nextDouble() - 0.5) * 8.0;
                dy = skeleton.getY() + (double) (this.random.nextInt(16) - 8);
                dz = skeleton.getZ() + (this.random.nextDouble() - 0.5) * 8.0;
            } while (!skeleton.randomTeleport(dx, dy, dz, true));
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.SQUID_INK, skeleton.getX(), skeleton.getY(0.5F), skeleton.getZ(), 20, this.getBbWidth(), this.getBbHeight(), this.getBbWidth(), 0.0D);
            this.level().addFreshEntity(skeleton);
        }
    }
}
