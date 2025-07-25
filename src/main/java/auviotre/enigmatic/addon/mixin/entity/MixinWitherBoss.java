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
    @Unique
    private int enigmaticAddons$lcount = 0;

    protected MixinWitherBoss(EntityType<? extends Monster> type, Level world) {
        super(type, world);
    }

    @Shadow
    public abstract boolean isPowered();

    @Shadow
    public abstract void setAlternativeTarget(int p_31455_, int p_31456_);

    @Inject(method = "customServerAiStep", at = @At(value = "HEAD"))
    public void customServerAiStep(CallbackInfo ci) {
        if (SuperAddonHandler.isCurseBoosted(this)) {
            if (this.isPowered() && !this.enigmaticAddons$lastPower) {
                enigmaticAddons$randomSkeleton(2 + this.random.nextInt(2));
                this.enigmaticAddons$lastPower = this.isPowered();
            }
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(8.0F));
            for (Entity entity : entities) {
                if (entity instanceof Monster living && living != this && living.getMobType().equals(MobType.UNDEAD)) {
                    if (living.getTarget() == this) {
                        living.setTarget(null);
                    }
                }
            }
            if (this.tickCount % 40 == 0) this.enigmaticAddons$lastPower = this.isPowered();
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    public void hurtMix(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (SuperAddonHandler.isCurseBoosted(this) && this.isPowered() && cir.getReturnValue() && this.random.nextInt(10) == 0) {
            int temp = Math.max(1, Mth.ceil(amount / 7.5F));
            temp = this.random.nextInt(temp);
            enigmaticAddons$lcount += temp;
            if (enigmaticAddons$lcount > 10) return;
            enigmaticAddons$randomSkeleton(temp);
        }
    }

    @Unique
    private void enigmaticAddons$randomSkeleton(int count) {
        if (count <= 0 || this.level().isClientSide()) return;
        count = Math.min(count, 3);
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
            int randomCount = 0;
            do {
                if (randomCount++ > 12) return;
                dx = skeleton.getX() + (this.random.nextDouble() - 0.5) * 8.0;
                dy = skeleton.getY() + (double) (this.random.nextInt(16) - 8);
                dz = skeleton.getZ() + (this.random.nextDouble() - 0.5) * 8.0;
            } while (!skeleton.randomTeleport(dx, dy, dz, true));
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.SQUID_INK, skeleton.getX(), skeleton.getY(0.5F), skeleton.getZ(), 20, this.getBbWidth(), this.getBbHeight(), this.getBbWidth(), 0.0D);
            if (this.getTarget() != null) skeleton.setTarget(this.getTarget());
            this.level().addFreshEntity(skeleton);
        }
    }
}
