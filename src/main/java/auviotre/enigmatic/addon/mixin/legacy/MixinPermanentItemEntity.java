package auviotre.enigmatic.addon.mixin.legacy;

import auviotre.enigmatic.addon.EnigmaticAddons;
import auviotre.enigmatic.addon.contents.items.ExtradimensionalScepter;
import auviotre.enigmatic.addon.packets.clients.PacketExtradimensionParticles;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(PermanentItemEntity.class)
public abstract class MixinPermanentItemEntity extends Entity {
    @Unique
    private int enigmaticAddons$extradimensionalTick = 0;

    public MixinPermanentItemEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Shadow(remap = false)
    public abstract ItemStack getItem();

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickMix(CallbackInfo ci) {
        Level world = this.level();
        ItemStack stack = this.getItem();
        if (stack.is(EnigmaticItems.EXTRADIMENSIONAL_EYE)) {
            MutableComponent component = Component.literal("ExtradimensionalLockSpace");
            if (this.getCustomName().equals(component)) {
                this.enigmaticAddons$extradimensionalTick++;
                if (world.isClientSide()) {
                    for (int i = 0; i < 8; i++) {
                        float arc = (float) (this.random.nextFloat() * Math.PI);
                        float y = (this.random.nextFloat() - 0.8F) * 0.1F;
                        world.addParticle(ParticleTypes.WITCH, this.getX(), this.getY() + 0.4 + y * 4, this.getZ(), Math.sin(arc) * 0.6, y, Math.cos(arc) * 0.6);
                    }
                } else {
                    if (this.enigmaticAddons$extradimensionalTick > this.getPersistentData().getInt("ExtradimensionalLockTimer")) {
                        EntityType<?> type = ExtradimensionalScepter.Helper.getType(stack);
                        if (type == null) {
                            this.discard();
                            return;
                        }
                        Entity storedEntity = type.create(world);
                        storedEntity.load(ExtradimensionalScepter.Helper.getInfo(stack));
                        world.addFreshEntity(storedEntity);
                        storedEntity.hurt(storedEntity.damageSources().fellOutOfWorld(), 0.5F);
                        if (storedEntity instanceof LivingEntity living)
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 4));
                        world.playSound(null, this.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5F, 0.8F + 0.4F * this.random.nextFloat());
                        EnigmaticAddons.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(this.getX(), this.getY(), this.getZ(), 16.0, world.dimension())),
                                new PacketExtradimensionParticles(storedEntity.getX(), storedEntity.getY(), storedEntity.getZ(), storedEntity.getBbWidth(), storedEntity.getBbHeight(), 1));
                        this.discard();
                    }
                }
            }
        }
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    public void glowing(CallbackInfoReturnable<Boolean> cir) {
        if (this.getItem().is(EnigmaticItems.EXTRADIMENSIONAL_EYE) && this.hasCustomName() && this.getCustomName().equals(Component.literal("ExtradimensionalLockSpace"))) {
            cir.setReturnValue(false);
        }
    }
}
